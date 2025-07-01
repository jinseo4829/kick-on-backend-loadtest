package kr.kickon.api.admin.partners;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import kr.kickon.api.admin.partners.dto.PartnersDetailDTO;
import kr.kickon.api.admin.partners.dto.PartnersListDTO;
import kr.kickon.api.admin.partners.request.CreatePartnersRequestDTO;
import kr.kickon.api.admin.partners.request.PartnersFilterRequest;
import kr.kickon.api.admin.partners.request.PatchPartnersRequestDTO;
import kr.kickon.api.admin.user.dto.UserListDTO;
import kr.kickon.api.domain.actualSeasonTeam.ActualSeasonTeamService;
import kr.kickon.api.domain.partners.PartnersRepository;
import kr.kickon.api.domain.team.TeamService;
import kr.kickon.api.domain.team.dto.TeamDTO;
import kr.kickon.api.domain.user.UserService;
import kr.kickon.api.global.common.entities.ActualSeasonTeam;
import kr.kickon.api.global.common.entities.League;
import kr.kickon.api.global.common.entities.Partners;
import kr.kickon.api.global.common.entities.QActualSeason;
import kr.kickon.api.global.common.entities.QActualSeasonTeam;
import kr.kickon.api.global.common.entities.QLeague;
import kr.kickon.api.global.common.entities.QPartners;
import kr.kickon.api.global.common.entities.QTeam;
import kr.kickon.api.global.common.entities.QUser;
import kr.kickon.api.global.common.entities.Team;
import kr.kickon.api.global.common.entities.User;
import kr.kickon.api.global.common.enums.ContractStatus;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminPartnersService {

  private final JPAQueryFactory queryFactory;
  private final ActualSeasonTeamService actualSeasonTeamService;
  private final PartnersRepository partnersRepository;
  private final UserService userService;
  private final TeamService teamService;

  public Partners findByPk(Long pk){
    BooleanExpression predicate = QPartners.partners.pk.eq(pk).and(QPartners.partners.status.eq(DataStatus.ACTIVATED));
    Optional<Partners> partners = partnersRepository.findOne(predicate);
    return partners.orElse(null);
  }

  public Page<PartnersListDTO> findPartnersByFilter(PartnersFilterRequest request,
      Pageable pageable) {
    QPartners partners = QPartners.partners;
    QUser user = QUser.user;
    QTeam qteam = QTeam.team;
    QActualSeasonTeam qactualSeasonTeam = QActualSeasonTeam.actualSeasonTeam;
    QActualSeason qactualSeason = QActualSeason.actualSeason;
    QLeague qleague = QLeague.league;

    BooleanBuilder builder = new BooleanBuilder()
        .and(user.status.eq(DataStatus.ACTIVATED))
        .and(partners.status.eq(DataStatus.ACTIVATED));

    if (request.getName() != null && !request.getName().isBlank()) {
      builder.and(partners.name.containsIgnoreCase(request.getName()));
    }

    if (request.getNickname() != null && !request.getNickname().isBlank()) {
      builder.and(user.nickname.containsIgnoreCase(request.getNickname()));
    }

    if (request.getTeamPk() != null) {
      builder.and(partners.team.pk.eq(request.getTeamPk()));
    }

    if (request.getLeaguePk() != null) {
      builder.and(qleague.pk.eq(request.getLeaguePk()));
    }

    // total count
    long total = Optional.ofNullable(queryFactory
        .select(partners.count())
        .from(partners)
        .join(partners.user, user)
        .leftJoin(qactualSeasonTeam).on(qactualSeasonTeam.team.eq(partners.team))
        .leftJoin(qactualSeason).on(qactualSeasonTeam.actualSeason.eq(qactualSeason))
        .leftJoin(qleague).on(qactualSeason.league.eq(qleague))
        .where(builder)
        .fetchOne()).orElse(0L);

    // content
    List<Partners> content = queryFactory
        .selectFrom(partners)
        .join(partners.user, user).fetchJoin()
        .leftJoin(qactualSeasonTeam).on(qactualSeasonTeam.team.eq(partners.team))
        .leftJoin(qactualSeason).on(qactualSeasonTeam.actualSeason.eq(qactualSeason))
        .leftJoin(qleague).on(qactualSeason.league.eq(qleague))
        .where(builder)
        .orderBy(partners.createdAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    List<PartnersListDTO> dtos = content.stream().map(partner -> {
      TeamDTO teamDTO = convertToTeamDTO(partner.getTeam());

      return PartnersListDTO.fromEntity(partner)
          .toBuilder()
          .team(teamDTO)
          .build();
    }).toList();


    return new PageImpl<>(dtos, pageable, total);
  }

  public PartnersDetailDTO getPartnersDetail(Partners partners) {

    User user = partners.getUser();
    if (user == null) throw new NotFoundException(ResponseCode.NOT_FOUND_USER);
    UserListDTO userDto = UserListDTO.fromEntity(user);

    // TeamDTO로 변환
    TeamDTO teamDTO = convertToTeamDTO(partners.getTeam());

    return PartnersDetailDTO.builder()
        .pk(partners.getPk())
        .name(partners.getName())
        .partnersEmail(partners.getPartnersEmail())
        .contractStartDate(partners.getContractStartDate())
        .contractEndDate(partners.getContractEndDate())
        .contractStatus(partners.getContractStatus())
        .etc(partners.getEtc())
        .user(userDto)
        .team(teamDTO)
        .build();
  }

  private TeamDTO convertToTeamDTO(Team team) {
    TeamDTO.TeamDTOBuilder teamBuilder = TeamDTO.builder()
        .pk(team.getPk())
        .nameKr(team.getNameKr())
        .nameEn(team.getNameEn())
        .logoUrl(team.getLogoUrl());

    // 리그 정보 포함
    ActualSeasonTeam actualSeasonTeam = actualSeasonTeamService.findLatestByTeam(team.getPk());
    if (actualSeasonTeam != null && actualSeasonTeam.getActualSeason() != null) {
      League league = actualSeasonTeam.getActualSeason().getLeague();
      if (league != null) {
        teamBuilder
            .leaguePk(league.getPk())
            .leagueNameKr(league.getNameKr())
            .leagueNameEn(league.getNameEn());
      }
    }

    return teamBuilder.build();
  }
  @Transactional
  public PartnersDetailDTO createPartners(CreatePartnersRequestDTO request) {

    User user = userService.findByPk(request.getUserPk());
    if (user == null) throw new NotFoundException(ResponseCode.NOT_FOUND_USER);
    Team team = teamService.findByPk(request.getTeamPk());
    if (team == null) throw new NotFoundException(ResponseCode.NOT_FOUND_TEAM);

    Partners partners = Partners.builder()
        .id(UUID.randomUUID().toString())
        .user(user)
        .name(request.getName())
        .partnersEmail(request.getPartnersEmail())
        .snsUrl(request.getSnsUrl())
        .team(team)
        .status(DataStatus.ACTIVATED)
        .build();

    partnersRepository.save(partners);

    UserListDTO userDto = UserListDTO.fromEntity(user);
    TeamDTO teamDTO = convertToTeamDTO(team);

    return PartnersDetailDTO.builder()
        .pk(partners.getPk())
        .user(userDto)
        .name(partners.getName())
        .partnersEmail(partners.getPartnersEmail())
        .snsUrl(partners.getSnsUrl())
        .contractStartDate(partners.getContractStartDate())
        .contractEndDate(partners.getContractEndDate())
        .contractStatus(partners.getContractStatus())
        .etc(partners.getEtc())
        .team(teamDTO)
        .build();
  }

  @Transactional
  public void deletePartners(Partners partners) {
    partners.setStatus(DataStatus.DEACTIVATED);
    partnersRepository.save(partners);
  }

  @Transactional
  public PartnersDetailDTO patchPartners(Partners partners, PatchPartnersRequestDTO request) {

    if (request.getName() != null) {
      partners.setName(request.getName());
    }
    if (request.getPartnersEmail() != null) {
      partners.setPartnersEmail(request.getPartnersEmail());
    }
    if (request.getSnsUrl() != null) {
      partners.setSnsUrl(request.getSnsUrl());
    }
    if (request.getEtc() != null) {
      partners.setEtc(request.getEtc());
    }
    if (request.getTeamPk() != null) {
      Team team = teamService.findByPk(request.getTeamPk());
      if (team == null) throw new NotFoundException(ResponseCode.NOT_FOUND_TEAM);
      partners.setTeam(team);
    }
    if (request.getUserPk() != null) {
      User user = userService.findByPk(request.getUserPk());
      if (user == null) throw new NotFoundException(ResponseCode.NOT_FOUND_USER);
      partners.setUser(user);
    }
    if (request.getContractStartDate() != null) {
      partners.setContractStartDate(LocalDateTime.parse(request.getContractStartDate()));
    }
    if (request.getContractEndDate() != null) {
      partners.setContractEndDate(LocalDateTime.parse(request.getContractEndDate()));
    }
    if (request.getContractStatus() != null) {
      try {
        partners.setContractStatus(ContractStatus.valueOf(request.getContractStatus()));
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("계약 상태 값이 유효하지 않습니다: " + request.getContractStatus());
      }
    }

    partnersRepository.save(partners);

    // DTO로 변환
    UserListDTO userDto = UserListDTO.fromEntity(partners.getUser());
    TeamDTO teamDTO = convertToTeamDTO(partners.getTeam());

    return PartnersDetailDTO.builder()
        .pk(partners.getPk())
        .user(userDto)
        .name(partners.getName())
        .partnersEmail(partners.getPartnersEmail())
        .snsUrl(partners.getSnsUrl())
        .contractStartDate(partners.getContractStartDate())
        .contractEndDate(partners.getContractEndDate())
        .contractStatus(partners.getContractStatus())
        .etc(partners.getEtc())
        .team(teamDTO)
        .build();
  }


}
