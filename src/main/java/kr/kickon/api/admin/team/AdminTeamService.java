package kr.kickon.api.admin.team;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import kr.kickon.api.admin.team.dto.TeamDetailDTO;
import kr.kickon.api.admin.team.dto.TeamListDTO;
import kr.kickon.api.admin.team.request.PatchTeamRequestDTO;
import kr.kickon.api.admin.team.request.TeamFilterRequest;
import kr.kickon.api.domain.actualSeason.ActualSeasonService;
import kr.kickon.api.domain.actualSeasonRanking.ActualSeasonRankingService;
import kr.kickon.api.domain.actualSeasonTeam.ActualSeasonTeamService;
import kr.kickon.api.domain.gambleSeason.GambleSeasonService;
import kr.kickon.api.domain.gambleSeasonRanking.GambleSeasonRankingService;
import kr.kickon.api.domain.gambleSeasonTeam.GambleSeasonTeamService;
import kr.kickon.api.domain.league.dto.LeagueDTO;
import kr.kickon.api.domain.team.TeamRepository;
import kr.kickon.api.global.common.entities.ActualSeason;
import kr.kickon.api.global.common.entities.ActualSeasonRanking;
import kr.kickon.api.global.common.entities.ActualSeasonTeam;
import kr.kickon.api.global.common.entities.GambleSeason;
import kr.kickon.api.global.common.entities.GambleSeasonRanking;
import kr.kickon.api.global.common.entities.League;
import kr.kickon.api.global.common.entities.QActualSeason;
import kr.kickon.api.global.common.entities.QActualSeasonTeam;
import kr.kickon.api.global.common.entities.QLeague;
import kr.kickon.api.global.common.entities.QTeam;
import kr.kickon.api.global.common.entities.Team;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.error.exceptions.NotFoundException;
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
  private final TeamRepository teamRepository;
  private final GambleSeasonService gambleSeasonService;
  private final ActualSeasonRankingService actualSeasonRankingService;
  private final GambleSeasonRankingService gambleSeasonRankingService;
  private final ActualSeasonService actualSeasonService;
  private final GambleSeasonTeamService gambleSeasonTeamService;

  public Team findByPk(Long pk) {
    BooleanExpression predicate = QTeam.team.pk.eq(pk).and(QTeam.team.status.eq(DataStatus.ACTIVATED));
    Optional<Team> team = teamRepository.findOne(predicate);
    return team.orElse(null);
  }

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

    List<TeamListDTO> dtos = (List<TeamListDTO>) content.stream()
        .map(t -> {
          Team tm = t.get(team);
          League lg = t.get(league);
          ActualSeasonTeam actualSeasonTeam = actualSeasonTeamService
              .findLatestByTeam(tm.getPk());

          Long seasonPk = null;
          String seasonTitle = null;
          if (actualSeasonTeam.getActualSeason() != null) {
            seasonPk = actualSeasonTeam.getActualSeason().getPk();
            seasonTitle = actualSeasonTeam.getActualSeason().getTitle();
          }

          return TeamListDTO.fromEntity(tm)
              .toBuilder()
              .league(new LeagueDTO(lg))
              .actualSeasonPk(seasonPk)
              .actualSeasonTitle(seasonTitle)
              .build();
        })
        .toList();

    return new PageImpl<>(dtos, pageable, total);
  }

  @Transactional
  public TeamDetailDTO getTeamDetail(Team team) {

    ActualSeasonTeam ast = actualSeasonTeamService.findLatestByTeam(team.getPk());
    LeagueDTO leagueDto = null;
    League league = ast.getActualSeason().getLeague();

    Long actualSeasonPk = ast.getActualSeason().getPk();
    String actualSeasonTitle = ast.getActualSeason().getTitle();
    if (league != null) {
      leagueDto = new LeagueDTO(ast.getActualSeason().getLeague());
    }
    ActualSeasonRanking actualSeasonRanking = actualSeasonRankingService.findByActualSeasonAndTeam(
        actualSeasonPk, team.getPk());

    // GambleSeasonTeam 기반 정보
    GambleSeason gs = gambleSeasonService.findRecentOperatingSeasonByLeaguePk(league.getPk());
    Long gambleSeasonPk = null;
    String gambleSeasonTitle = null;
    if (gs != null) {
      gambleSeasonPk = gs.getPk();
      gambleSeasonTitle = gs.getTitle();
    }

    GambleSeasonRanking gambleSeasonRanking = gambleSeasonRankingService.findByTeamPk(team.getPk());

    return TeamDetailDTO.fromEntity(team)
        .toBuilder()
        .league(leagueDto)
        .actualSeasonPk(actualSeasonPk)
        .actualSeasonTitle(actualSeasonTitle)
        .actualSeasonRankOrder(actualSeasonRanking.getRankOrder())
        .gambleSeasonPk(gambleSeasonPk)
        .gambleSeasonTitle(gambleSeasonTitle)
        .gambleSeasonRankOrder(gambleSeasonRanking.getRankOrder())
        .build();
  }

  @Transactional
  public TeamDetailDTO patchTeam(
      Team team, PatchTeamRequestDTO request) {

    if (request.getNameKr() != null) {
      team.setNameKr(request.getNameKr());
    }
    if (request.getNameEn() != null) {
      team.setNameEn(request.getNameEn());
    }
    if (request.getActualSeasonPk() != null) {
      ActualSeason actualSeason = actualSeasonService.findByPk(request.getActualSeasonPk());
      if (actualSeason == null)
        throw new NotFoundException(ResponseCode.NOT_FOUND_ACTUAL_SEASON);
      actualSeasonTeamService.patchActualSeasonTeam(actualSeason, team.getPk());
    }
    if (request.getGambleSeasonPk() != null) {
      GambleSeason gambleSeason = gambleSeasonService.findByPk(request.getGambleSeasonPk());
      if (gambleSeason == null)
        throw new NotFoundException(ResponseCode.NOT_FOUND_GAMBLE_SEASON);
      gambleSeasonTeamService.patchGambleSeasonTeam(gambleSeason, team.getPk());
    }
    if (request.getLogoUrl() != null) {
      team.setLogoUrl(request.getLogoUrl());
    }
    return TeamDetailDTO.fromEntity(team);
  }
}
