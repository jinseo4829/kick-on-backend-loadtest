package kr.kickon.api.admin.team;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import kr.kickon.api.admin.team.dto.TeamListDTO;
import kr.kickon.api.admin.team.request.TeamFilterRequest;
import kr.kickon.api.domain.actualSeasonTeam.ActualSeasonTeamService;
import kr.kickon.api.domain.league.dto.LeagueDTO;
import kr.kickon.api.global.common.entities.ActualSeasonTeam;
import kr.kickon.api.global.common.entities.League;
import kr.kickon.api.global.common.entities.QActualSeason;
import kr.kickon.api.global.common.entities.QActualSeasonTeam;
import kr.kickon.api.global.common.entities.QLeague;
import kr.kickon.api.global.common.entities.QTeam;
import kr.kickon.api.global.common.entities.Team;
import kr.kickon.api.global.common.enums.DataStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminTeamService {

  private final JPAQueryFactory queryFactory;
  private final ActualSeasonTeamService actualSeasonTeamService;

  @Transactional
  public Page<TeamListDTO> findTeamByFilter(TeamFilterRequest request,
      Pageable pageable) {
    QActualSeason actualSeason = QActualSeason.actualSeason;
    QLeague league = QLeague.league;
    QTeam team = QTeam.team;
    QActualSeasonTeam ast = QActualSeasonTeam.actualSeasonTeam;

    BooleanBuilder builder = new BooleanBuilder()
        .and(team.status.eq(DataStatus.ACTIVATED))
        .and(actualSeason.status.eq(DataStatus.ACTIVATED))
        .and(league.status.eq(DataStatus.ACTIVATED))
        .and(ast.status.eq(DataStatus.ACTIVATED));

    // 팀 이름(한글‧영어)
    if (request.getName() != null && !request.getName().isBlank()) {
      builder.and(
          team.nameKr.containsIgnoreCase(request.getName())
              .or(team.nameEn.containsIgnoreCase(request.getName()))
      );
    }

    // 실제 시즌 PK
    if (request.getActualSeasonPk() != null) {
      builder.and(actualSeason.pk.eq(request.getActualSeasonPk()));
    }

    // 연도
    if (request.getYear() != null) {
      builder.and(actualSeason.year.eq(request.getYear()));
    }

    // total count
    long total = Optional.ofNullable(
        queryFactory
            .select(team.countDistinct())
            .from(team)
            .join(ast).on(ast.team.eq(team))
            .join(ast.actualSeason, actualSeason)
            .join(actualSeason.league, league)
            .where(builder)
            .fetchOne()
    ).orElse(0L);

    // content
    List<Tuple> content = queryFactory
        .select(team, league)
        .from(team)
        .join(ast).on(ast.team.eq(team))
        .join(ast.actualSeason, actualSeason)
        .join(actualSeason.league, league)
        .where(builder)
        .orderBy(actualSeason.startedAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    List<TeamListDTO> dtos = content.stream()
        .map(t -> {
          Team tm = t.get(team);
          League lg = t.get(league);
          ActualSeasonTeam actualSeasonTeam = actualSeasonTeamService
              .findLatestByTeam(tm.getPk());

          Long seasonPk = null;
          String seasonTitle = null;
          if (ast != null && actualSeasonTeam.getActualSeason() != null) {
            seasonPk = actualSeasonTeam.getActualSeason().getPk();
            seasonTitle = actualSeasonTeam.getActualSeason().getTitle();
          }

          return TeamListDTO.fromEntity(tm)
              .toBuilder()
              .league(new LeagueDTO(lg))
              .seasonPk(seasonPk)
              .seasonTitle(seasonTitle)
              .build();
        })
        .toList();

    return new PageImpl<>(dtos, pageable, total);
  }
}
