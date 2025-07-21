package kr.kickon.api.admin.team;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import kr.kickon.api.admin.team.dto.TeamDetailDTO;
import kr.kickon.api.admin.team.dto.TeamListDTO;
import kr.kickon.api.admin.team.request.UpdateTeamRequest;
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
import kr.kickon.api.global.common.entities.QActualSeasonRanking;
import kr.kickon.api.global.common.entities.QActualSeasonTeam;
import kr.kickon.api.global.common.entities.QLeague;
import kr.kickon.api.global.common.entities.QTeam;
import kr.kickon.api.global.common.entities.Team;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.common.enums.OperatingStatus;
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

  //region 팀 단건 조회
  /**
   * pk로 team 단건 조회
   */
  public Team findByPk(Long pk) {
    return teamRepository.findByPkAndStatus(pk, DataStatus.ACTIVATED).orElse(null);
  }
  //endregion

//region 팀 리스트 조회 (필터 optional)
  /**
   * 필터 조건(팀 이름, 시즌 PK)에 따른 팀 리스트 조회 (페이징 포함)
   */
  @Transactional
  public Page<TeamListDTO> getTeamListByFilter(TeamFilterRequest request,
      Pageable pageable) {
    QActualSeason actualSeason = QActualSeason.actualSeason;
    QLeague league = QLeague.league;
    QTeam team = QTeam.team;
    QActualSeasonTeam actualSeasonTeam = QActualSeasonTeam.actualSeasonTeam;

    BooleanBuilder builder = new BooleanBuilder()
        .and(team.status.eq(DataStatus.ACTIVATED));

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

    // total count
    long total = Optional.ofNullable(
        queryFactory
            .select(team.countDistinct())
            .from(team)
            .leftJoin(actualSeasonTeam).on(actualSeasonTeam.team.eq(team),
                actualSeasonTeam.status.eq(DataStatus.ACTIVATED)
            )
            .leftJoin(actualSeasonTeam.actualSeason, actualSeason)
            .where(builder)
            .fetchOne()
    ).orElse(0L);

    // content
    List<Tuple> content = queryFactory
        .selectDistinct(team, league)
        .from(team)
        .leftJoin(actualSeasonTeam).on(actualSeasonTeam.team.eq(team),
            actualSeasonTeam.status.eq(DataStatus.ACTIVATED)
        )
        .leftJoin(actualSeasonTeam.actualSeason, actualSeason)
        .leftJoin(actualSeason.league, league).on(league.status.eq(DataStatus.ACTIVATED))
        .where(builder)
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    List<TeamListDTO> dtos = (List<TeamListDTO>) content.stream()
        .map(t -> {
          Team teamEntity = t.get(team);
          League leagueEntity = t.get(league);
          ActualSeasonTeam actualSeasonTeamEntity = actualSeasonTeamService
              .findLatestByTeam(teamEntity.getPk());
          ActualSeason actualSeasonEntity = null;
          if (actualSeasonTeamEntity != null) {
            actualSeasonEntity = actualSeasonTeamEntity.getActualSeason();
          }
          Integer rankOrder = null;

              if (actualSeasonEntity != null) {
                ActualSeasonRanking seasonRanking =
                    actualSeasonRankingService.findByActualSeasonAndTeam(
                        actualSeasonEntity.getPk(),
                        teamEntity.getPk()
                    );
                if (seasonRanking != null)
                  rankOrder = seasonRanking.getRankOrder();
              }
          TeamListDTO.SeasonInfo seasonInfo = (actualSeasonEntity != null)
              ? TeamListDTO.SeasonInfo.builder()
                .pk(actualSeasonEntity.getPk())
                .title(actualSeasonEntity.getTitle())
                .rankOrder(rankOrder)
                .build()
              : null;

          return TeamListDTO.fromEntity(teamEntity)
              .toBuilder()
              .league(leagueEntity != null ? new LeagueDTO(leagueEntity) : null)
              .actualSeason(seasonInfo)
              .build();
        })
        .toList();

    return new PageImpl<>(dtos, pageable, total);
  }
//endregion

//region 팀 상세 조회
  /**
   * 팀 상세 정보 조회
   */
  @Transactional
  public TeamDetailDTO getTeamDetail(Team team) {

    TeamListDTO.SeasonInfo actualSeasonInfo = null;
    TeamListDTO.SeasonInfo gambleSeasonInfo = null;

    ActualSeasonTeam actualSeasonTeamEntity = actualSeasonTeamService.findLatestByTeam(team.getPk());
    if (actualSeasonTeamEntity != null && actualSeasonTeamEntity.getActualSeason() != null) {
      ActualSeason actualSeasonEntity = actualSeasonTeamEntity.getActualSeason();

      Integer actualRankOrder = null;
      ActualSeasonRanking actualSeasonRankingEntity =
          actualSeasonRankingService.findByActualSeasonAndTeam(actualSeasonEntity.getPk(), team.getPk());
      if (actualSeasonRankingEntity != null) actualRankOrder = actualSeasonRankingEntity.getRankOrder();

    actualSeasonInfo = TeamListDTO.SeasonInfo.builder()
        .pk(actualSeasonEntity.getPk())
        .title(actualSeasonEntity.getTitle())
        .rankOrder(actualRankOrder)
        .build();
  }

    // GambleSeasonTeam 기반 정보

      GambleSeasonTeam gambleSeasonTeamEntity =
          gambleSeasonTeamService.getRecentOperatingByTeamPk(team.getPk());
      if (gambleSeasonTeamEntity != null && gambleSeasonTeamEntity.getGambleSeason() != null) {
        GambleSeason gambleSeasonEntity = gambleSeasonTeamEntity.getGambleSeason();
        Integer gambleRankOrderEntity = null;
        GambleSeasonRanking gambleSeasonRankingEntity = gambleSeasonRankingService.findByGambleSeasonAndTeam(gambleSeasonEntity.getPk(), team.getPk());
        if (gambleSeasonRankingEntity != null)
          gambleRankOrderEntity = gambleSeasonRankingEntity.getRankOrder();

        gambleSeasonInfo = TeamListDTO.SeasonInfo.builder()
            .pk(gambleSeasonEntity.getPk())
            .title(gambleSeasonEntity.getTitle())
            .rankOrder(gambleRankOrderEntity)
            .build();
      }

    Integer fanCount = userFavoriteTeamService.countFansByTeamPk(team.getPk());

    return TeamDetailDTO.fromEntity(team)
        .toBuilder()
        .actualSeason(actualSeasonInfo)
        .gambleSeason(gambleSeasonInfo)
        .fanCount(fanCount != null ? fanCount : 0)
        .build();
  }
//endregion

//region 팀 정보 수정
  /**
   * 팀 정보 수정
   * - 이름(한글, 영어), 시즌 연결, 로고 URL 등 수정
   * - 연관된 실제 시즌 또는 승부 예측 시즌도 함께 재할당
   */
  @Transactional
  public TeamDetailDTO updateTeam(
      Team team, UpdateTeamRequest request) {
      if (request.getNameKr() != null) {
        team.setNameKr(request.getNameKr());
      }
      if (request.getNameEn() != null) {
        team.setNameEn(request.getNameEn());
      }
      if (request.getActualSeasonPk() != null) {
        ActualSeason actualSeasonEntity = actualSeasonService.findByPk(request.getActualSeasonPk());
        if (actualSeasonEntity == null)
          throw new NotFoundException(ResponseCode.NOT_FOUND_ACTUAL_SEASON);
        actualSeasonTeamService.updateActualSeasonTeam(actualSeasonEntity, team);
        actualSeasonRankingService.updateActualSeasonRanking(team, actualSeasonEntity);
      }
      if (request.getGambleSeasonPk() != null) {
        GambleSeason gambleSeasonEntity = gambleSeasonService.findByPk(request.getGambleSeasonPk());
        if (gambleSeasonEntity == null)
          throw new NotFoundException(ResponseCode.NOT_FOUND_GAMBLE_SEASON);
        gambleSeasonTeamService.updateGambleSeasonTeam(gambleSeasonEntity, team);
        gambleSeasonRankingService.updateGambleSeasonRanking(team, gambleSeasonEntity);
      }
      if (request.getLogoUrl() != null) {
        team.setLogoUrl(request.getLogoUrl());
      }
      teamRepository.save(team);
      Team updatedTeam = teamRepository.findById(team.getPk())
          .orElseThrow(() -> new NotFoundException(ResponseCode.NOT_FOUND_TEAM));

      return getTeamDetail(updatedTeam);
  }
//endregion
}
