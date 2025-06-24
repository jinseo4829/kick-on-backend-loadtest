package kr.kickon.api.admin.partners;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import kr.kickon.api.admin.partners.dto.PartnersListDTO;
import kr.kickon.api.admin.partners.request.PartnersFilterRequest;
import kr.kickon.api.domain.actualSeasonTeam.ActualSeasonTeamService;
import kr.kickon.api.domain.team.dto.TeamDTO;
import kr.kickon.api.domain.userFavoriteTeam.UserFavoriteTeamService;
import kr.kickon.api.global.common.entities.ActualSeasonTeam;
import kr.kickon.api.global.common.entities.League;
import kr.kickon.api.global.common.entities.Partners;
import kr.kickon.api.global.common.entities.QPartners;
import kr.kickon.api.global.common.entities.QTeam;
import kr.kickon.api.global.common.entities.QUser;
import kr.kickon.api.global.common.entities.QUserFavoriteTeam;
import kr.kickon.api.global.common.entities.Team;
import kr.kickon.api.global.common.entities.UserFavoriteTeam;
import kr.kickon.api.global.common.enums.DataStatus;
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
  private final UserFavoriteTeamService userFavoriteTeamService;
  private final ActualSeasonTeamService actualSeasonTeamService;

  public Page<PartnersListDTO> findPartnersByFilter(PartnersFilterRequest request,
      Pageable pageable) {
    QPartners partners = QPartners.partners;
    QUser user = QUser.user;
    QUserFavoriteTeam userFavoriteTeam = QUserFavoriteTeam.userFavoriteTeam;
    QTeam qteam = QTeam.team;

    BooleanBuilder builder = new BooleanBuilder();

    if (request.getName() != null && !request.getName().isBlank()) {
      builder.and(partners.name.containsIgnoreCase(request.getName()));
    }

    if (request.getNickname() != null && !request.getNickname().isBlank()) {
      builder.and(user.nickname.containsIgnoreCase(request.getNickname()));
    }

    // 팀명으로 필터링
    if (request.getFavoriteTeam() != null && !request.getFavoriteTeam().isBlank()) {
      builder.and(qteam.nameKr.containsIgnoreCase(request.getFavoriteTeam()));
      builder.and(userFavoriteTeam.user.eq(user));
    }
    builder.and(user.status.eq(DataStatus.ACTIVATED));
    builder.and(partners.status.eq(DataStatus.ACTIVATED));

    // total count
    long total = queryFactory
        .select(partners.count())
        .from(partners)
        .join(partners.user, user)
        .leftJoin(userFavoriteTeam).on(userFavoriteTeam.user.eq(user))
        .leftJoin(userFavoriteTeam.team, qteam)
        .where(builder)
        .fetchOne();

    // content
    List<Partners> content = queryFactory
        .selectFrom(partners)
        .join(partners.user, user).fetchJoin()
        .leftJoin(userFavoriteTeam).on(userFavoriteTeam.user.eq(user))
        .leftJoin(userFavoriteTeam.team, qteam)
        .where(builder)
        .orderBy(partners.createdAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    List<PartnersListDTO> dtos = content.stream().map(partner -> {
      PartnersListDTO dto = PartnersListDTO.fromEntity(partner);

      List<UserFavoriteTeam> favoriteTeams = userFavoriteTeamService.findAllByUserPk(
          partner.getUser().getPk());

      List<TeamDTO> teamDTOs = favoriteTeams.stream().map(fav -> {
        Team team = fav.getTeam();

        // ✅ 변수명 충돌 방지: teamBuilder 로 이름 변경
        TeamDTO.TeamDTOBuilder teamBuilder = TeamDTO.builder()
            .pk(team.getPk())
            .nameKr(team.getNameKr())
            .nameEn(team.getNameEn())
            .logoUrl(team.getLogoUrl());

        // ✅ 리그 정보 설정
        ActualSeasonTeam actualSeasonTeam = actualSeasonTeamService.findLatestByTeam(team.getPk());
        if (actualSeasonTeam != null && actualSeasonTeam.getActualSeason() != null) {
          League league = actualSeasonTeam.getActualSeason().getLeague();
          if (league != null) {
            teamBuilder.leaguePk(league.getPk())
                .leagueNameKr(league.getNameKr())
                .leagueNameEn(league.getNameEn());
          }
        }

        return teamBuilder.build();
      }).toList();


      dto.setFavoriteTeams(teamDTOs);
      return dto;
    }).toList();

    return new PageImpl<>(dtos, pageable, total);
  }
}
