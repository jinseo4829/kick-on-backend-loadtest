package kr.kickon.api.admin.team;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
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
import kr.kickon.api.domain.userFavoriteTeam.UserFavoriteTeamService;
import kr.kickon.api.global.common.entities.ActualSeason;
import kr.kickon.api.global.common.entities.ActualSeasonRanking;
import kr.kickon.api.global.common.entities.ActualSeasonTeam;
import kr.kickon.api.global.common.entities.GambleSeason;
import kr.kickon.api.global.common.entities.GambleSeasonRanking;
import kr.kickon.api.global.common.entities.GambleSeasonTeam;
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
  private final UserFavoriteTeamService userFavoriteTeamService;

  public Team findByPk(Long pk) {
    return teamRepository.findById(pk)
        .filter(t -> t.getStatus() == DataStatus.ACTIVATED)
        .orElse(null);
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

          Integer actualRankOrder = null;
          ActualSeasonRanking ar =
              actualSeasonRankingService.findByActualSeasonAndTeam(actualSeasonTeam.getActualSeason()
                  .getPk(), actualSeasonTeam.getTeam().getPk());
          if (ar != null) actualRankOrder = ar.getRankOrder();

          TeamListDTO.SeasonInfo actualSeasonInfo = null;
          if (actualSeasonTeam != null && actualSeasonTeam.getActualSeason() != null) {
            actualSeasonInfo = TeamListDTO.SeasonInfo.builder()
                .pk(actualSeasonTeam.getActualSeason().getPk())
                .title(actualSeasonTeam.getActualSeason().getTitle())
                .rankOrder(actualRankOrder)
                .build();
          }

          return TeamListDTO.fromEntity(tm)
              .toBuilder()
              .league(new LeagueDTO(lg))
              .actualSeason(actualSeasonInfo)
              .build();
        })
        .toList();

    return new PageImpl<>(dtos, pageable, total);
  }

  @Transactional
  public TeamDetailDTO getTeamDetail(Team team) {

    TeamDetailDTO.SeasonInfo actualSeasonInfo = null;
    TeamDetailDTO.SeasonInfo gambleSeasonInfo = null;
    LeagueDTO leagueDto = null;

    ActualSeasonTeam ast = actualSeasonTeamService.findLatestByTeam(team.getPk());
    if (ast != null && ast.getActualSeason() != null) {
      ActualSeason actualSeason = ast.getActualSeason();

      League league = actualSeason.getLeague();
      if (league != null) {
        leagueDto = new LeagueDTO(league);
      }

      Integer actualRankOrder = null;
      ActualSeasonRanking ar =
          actualSeasonRankingService.findByActualSeasonAndTeam(actualSeason.getPk(), team.getPk());
      if (ar != null) actualRankOrder = ar.getRankOrder();

    actualSeasonInfo = TeamDetailDTO.SeasonInfo.builder()
        .pk(actualSeason.getPk())
        .title(actualSeason.getTitle())
        .rankOrder(actualRankOrder)
        .build();
  }

    // GambleSeasonTeam 기반 정보

    if (leagueDto != null) {
      GambleSeasonTeam gst =
          gambleSeasonTeamService.findRecentOperatingByTeamPk(team.getPk());
      if (gst != null && gst.getGambleSeason() != null) {
        GambleSeason gs = gst.getGambleSeason();
        Integer gambleRankOrder = null;
        GambleSeasonRanking gr = gambleSeasonRankingService.findByGambleSeasonAndTeam(gs.getPk(), team.getPk());
        if (gr != null)
          gambleRankOrder = gr.getRankOrder();

        gambleSeasonInfo = TeamDetailDTO.SeasonInfo.builder()
            .pk(gs.getPk())
            .title(gs.getTitle())
            .rankOrder(gambleRankOrder)
            .build();
      }
    }
    Integer fanCnt = userFavoriteTeamService.countFansByTeamPk(team.getPk());

    return TeamDetailDTO.fromEntity(team)
        .toBuilder()
        .league(leagueDto)
        .actualSeason(actualSeasonInfo)
        .gambleSeason(gambleSeasonInfo)
        .fanCount(fanCnt != null ? fanCnt : 0)
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

      return getTeamDetail(team);
  }
}
