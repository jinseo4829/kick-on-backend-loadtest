package kr.kickon.api.admin.partners;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import kr.kickon.api.admin.partners.dto.PartnersDetailDTO;
import kr.kickon.api.admin.partners.dto.PartnersListDTO;
import kr.kickon.api.admin.partners.request.CreatePartnersRequest;
import kr.kickon.api.admin.partners.request.PartnersFilterRequest;
import kr.kickon.api.admin.partners.request.UpdatePartnersRequest;
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
import kr.kickon.api.global.common.entities.QUser;
import kr.kickon.api.global.common.entities.Team;
import kr.kickon.api.global.common.entities.User;
import kr.kickon.api.global.common.enums.ContractStatus;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import kr.kickon.api.global.error.exceptions.BadRequestException;
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

  //region 파트너스 엔티티 조회
  public Partners findByPk(Long pk){
    return partnersRepository.findByPkAndStatus(pk, DataStatus.ACTIVATED)
        .orElse(null);
  }
  //endregion

//region 파트너스 리스트 조회 (필터 이용)
  public Page<PartnersListDTO> getPartnersListByFilter(PartnersFilterRequest request,
      Pageable pageable) {
    QPartners partners = QPartners.partners;
    QUser user = QUser.user;
    QActualSeasonTeam actualSeasonTeam = QActualSeasonTeam.actualSeasonTeam;
    QActualSeason actualSeason = QActualSeason.actualSeason;
    QLeague league = QLeague.league;

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
      builder.and(league.pk.eq(request.getLeaguePk()));
    }

    // total count
    long total = Optional.ofNullable(queryFactory
        .select(partners.count())
        .from(partners)
        .join(partners.user, user)
        .leftJoin(actualSeasonTeam).on(actualSeasonTeam.team.eq(partners.team))
        .leftJoin(actualSeason).on(actualSeasonTeam.actualSeason.eq(actualSeason))
        .leftJoin(league).on(actualSeason.league.eq(league))
        .where(builder)
        .fetchOne()).orElse(0L);

    // content
    List<Tuple> tuples = queryFactory
        .select(partners, league)
        .distinct()
        .from(partners)
        .join(partners.user, user)
        .leftJoin(actualSeasonTeam).on(actualSeasonTeam.team.eq(partners.team))
        .leftJoin(actualSeason).on(actualSeasonTeam.actualSeason.eq(actualSeason))
        .leftJoin(league).on(actualSeason.league.eq(league))
        .where(builder)
        .orderBy(partners.createdAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    List<PartnersListDTO> dtos = tuples.stream().map(tuple -> {
      Partners partnersEntity = tuple.get(partners);
      League leagueEntity = tuple.get(league);

      if (partnersEntity == null) {
        return null;
      }

      Team teamEntity = partnersEntity.getTeam();

      // 팀 DTO 생성
      TeamDTO teamDTO = null;
      if (teamEntity != null) {
        teamDTO = TeamDTO.fromTeamAndLeague(teamEntity, leagueEntity);
      }
      return PartnersListDTO.fromEntity(partnersEntity, teamDTO);
    })
    .filter(Objects::nonNull)
    .toList();

    return new PageImpl<>(dtos, pageable, total);
  }//endregion

//region 파트너스 상세 정보 조회
  public PartnersDetailDTO getPartnersDetail(Partners partners) {

    User userEntity = partners.getUser();
    if (userEntity == null) throw new NotFoundException(ResponseCode.NOT_FOUND_USER);
    UserListDTO userDto = UserListDTO.fromEntity(userEntity);

    Team team = partners.getTeam();

    // 실제 시즌 팀 정보 조회
    ActualSeasonTeam actualSeasonTeamEntity = actualSeasonTeamService.findLatestByTeam(team.getPk());
    League league = (actualSeasonTeamEntity != null && actualSeasonTeamEntity.getActualSeason() != null)
        ? actualSeasonTeamEntity.getActualSeason().getLeague()
        : null;

    // TeamDTO 생성
    TeamDTO teamDTO = TeamDTO.fromTeamAndLeague(team, league);

    return PartnersDetailDTO.fromEntity(partners, userDto, teamDTO);
  }
//endregion

//region 파트너스 생성
  @Transactional
  public PartnersDetailDTO createPartners(CreatePartnersRequest request) {

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

    // 실제 시즌 팀 정보 조회
    ActualSeasonTeam actualSeasonTeamEntity = actualSeasonTeamService.findLatestByTeam(team.getPk());
    League league = (actualSeasonTeamEntity != null && actualSeasonTeamEntity.getActualSeason() != null)
        ? actualSeasonTeamEntity.getActualSeason().getLeague()
        : null;

    // TeamDTO 생성
    TeamDTO teamDTO = TeamDTO.fromTeamAndLeague(team, league);

    return PartnersDetailDTO.fromEntity(partners, userDto, teamDTO);
  }
//endregion

//region 파트너스 삭제
  @Transactional
  public void deletePartners(Partners partners) {
    partners.setStatus(DataStatus.DEACTIVATED);
    partnersRepository.save(partners);
  }
//endregion

//region 파트너스 정보 수정
  @Transactional
  public PartnersDetailDTO updatePartners(Partners partners, UpdatePartnersRequest request) {

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
      if (team == null)
        throw new NotFoundException(ResponseCode.NOT_FOUND_TEAM);
      partners.setTeam(team);
    }
    if (request.getUserPk() != null) {
      User user = userService.findByPk(request.getUserPk());
      if (user == null)
        throw new NotFoundException(ResponseCode.NOT_FOUND_USER);
      partners.setUser(user);
    }
    if (request.getContractStartDate() != null) {
      try {
        partners.setContractStartDate(LocalDateTime.parse(request.getContractStartDate()));
      } catch (DateTimeParseException e) {
        throw new BadRequestException(ResponseCode.INVALID_PARSING_INPUT);
      }
    }
    if (request.getContractEndDate() != null) {
      try {
        partners.setContractEndDate(LocalDateTime.parse(request.getContractEndDate()));
      } catch (DateTimeParseException e) {
        throw new BadRequestException(ResponseCode.INVALID_PARSING_INPUT);
      }
    }
    if (request.getContractStatus() != null) {
      try {
        partners.setContractStatus(ContractStatus.valueOf(request.getContractStatus()));
      } catch (IllegalArgumentException e) {
        throw new BadRequestException(ResponseCode.INVALID_REQUEST);
      }
    }

    partnersRepository.save(partners);

    return getPartnersDetail(partners);
  }
  //endregion
}
