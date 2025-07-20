package kr.kickon.api.admin.gambleSeason;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import kr.kickon.api.admin.gambleSeason.dto.GambleSeasonDetailDTO;
import kr.kickon.api.admin.gambleSeason.dto.GambleSeasonListDTO;
import kr.kickon.api.admin.gambleSeason.request.CreateGambleSeasonRequest;
import kr.kickon.api.admin.gambleSeason.request.GambleSeasonFilterRequest;
import kr.kickon.api.admin.gambleSeason.request.UpdateGambleSeasonRequest;
import kr.kickon.api.domain.actualSeason.ActualSeasonService;
import kr.kickon.api.domain.gambleSeason.GambleSeasonRepository;
import kr.kickon.api.domain.gambleSeasonRanking.GambleSeasonRankingService;
import kr.kickon.api.domain.gambleSeasonRanking.dto.GetGambleSeasonRankingDTO;
import kr.kickon.api.domain.gambleSeasonTeam.GambleSeasonTeamService;
import kr.kickon.api.domain.league.LeagueService;
import kr.kickon.api.domain.team.dto.SeasonTeamDTO;
import kr.kickon.api.global.common.entities.ActualSeason;
import kr.kickon.api.global.common.entities.GambleSeason;
import kr.kickon.api.global.common.entities.League;
import kr.kickon.api.global.common.entities.QActualSeason;
import kr.kickon.api.global.common.entities.QGambleSeason;
import kr.kickon.api.global.common.entities.QLeague;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.common.enums.OperatingStatus;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.error.exceptions.BadRequestException;
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
public class AdminGambleSeasonService {

  private final JPAQueryFactory queryFactory;
  private final GambleSeasonRepository gambleSeasonRepository;
  private final GambleSeasonTeamService gambleSeasonTeamService;
  private final GambleSeasonRankingService gambleSeasonRankingService;
  private final LeagueService leagueService;
  private final ActualSeasonService actualSeasonService;

  //region 승부 예측 시즌 단건 조회
  /**
   * pk로 GambleSeason 단건 조회
   */
  public GambleSeason findByPk(Long pk){
    BooleanExpression predicate = QGambleSeason.gambleSeason.pk.eq(pk).and(QGambleSeason.gambleSeason.status.eq(DataStatus.ACTIVATED));
    Optional<GambleSeason> gambleSeason = gambleSeasonRepository.findOne(predicate);
    return gambleSeason.orElse(null);
  }
  //endregion

  //region 승부 예측 시즌 리스트 조회 (필터 optional)
  /**
   * 필터 조건에 따라 승부 예측 시즌 목록 조회
   */
  @Transactional
  public Page<GambleSeasonListDTO> getGambleSeasonListByFilter(GambleSeasonFilterRequest request,
      Pageable pageable) {
    QGambleSeason season = QGambleSeason.gambleSeason;
    QActualSeason actualSeason = QActualSeason.actualSeason;
    QLeague league = QLeague.league;

    BooleanBuilder builder = new BooleanBuilder()
        .and(season.status.eq(DataStatus.ACTIVATED));

    // 리그 PK
    if (request.getLeaguePk() != null) {
      builder.and(season.actualSeason.eq(actualSeason));
      builder.and(actualSeason.league.eq(league));
      builder.and(league.pk.eq(request.getLeaguePk()));    }

    // 시작일 ≥ requested.startedAt
    if (request.getStartedAt() != null && !request.getStartedAt().isBlank()) {
      LocalDateTime startedAtEntity = LocalDateTime.parse(request.getStartedAt());
      builder.and(season.startedAt.goe(startedAtEntity));
    }

    // 마감일 ≤ requested.finishedAt
    if (request.getFinishedAt() != null && !request.getFinishedAt().isBlank()) {
      LocalDateTime finishedAtEntity = LocalDateTime.parse(request.getFinishedAt());
      builder.and(season.finishedAt.loe(finishedAtEntity));
    }

    // 운영 상태 ENUM
    if (request.getOperatingStatus() != null && !request.getOperatingStatus().isBlank()) {
      OperatingStatus operatingStatusEntity = OperatingStatus.valueOf(request.getOperatingStatus());
      builder.and(season.operatingStatus.eq(operatingStatusEntity));
    }

    // total count
    long total = Optional.ofNullable(
        queryFactory.select(season.count())
            .from(season)
            .leftJoin(season.actualSeason, actualSeason)
            .leftJoin(actualSeason.league, league)
            .where(builder)
            .fetchOne()
    ).orElse(0L);

    // content
    List<GambleSeason> content = queryFactory
        .selectFrom(season)
        .leftJoin(season.actualSeason, actualSeason).fetchJoin()
        .leftJoin(actualSeason.league, league).fetchJoin()
        .where(builder)
        .orderBy(season.startedAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    List<GambleSeasonListDTO> dtos = content.stream()
        .map(GambleSeasonListDTO::fromEntity)
        .toList();

    return new PageImpl<>(dtos, pageable, total);
  }
  //endregion

  //region 승부 예측 시즌 상세 조회
  /**
   * 시즌 단건 조회 후 상세 DTO 변환
   */
  @Transactional
  public GambleSeasonDetailDTO getGambleSeasonDetail(GambleSeason gambleSeason) {
    List<SeasonTeamDTO> teamList =
        gambleSeasonTeamService.getgambleSeasonTeamListByGambleSeasonPk(gambleSeason.getPk());

    List<GetGambleSeasonRankingDTO> rankingList =
        gambleSeasonRankingService.getRankingDtoBySeasonPk(gambleSeason.getPk());

    return GambleSeasonDetailDTO.fromEntity(gambleSeason, teamList, rankingList);
  }
  //endregion

  //region 승부 예측 시즌 생성
  /**
   * 새로운 승부 예측 시즌 생성
   */
  @Transactional
  public GambleSeasonDetailDTO createGambleSeason(CreateGambleSeasonRequest request) {

    League leagueEntity = leagueService.findByPk(request.getLeaguePk());
    if (leagueEntity == null) throw new NotFoundException(ResponseCode.NOT_FOUND_LEAGUE);

    // 리그에 연결된 최신 ActualSeason 가져오기
    ActualSeason actualSeasonEntity =
        actualSeasonService.findRecentByLeaguePk(leagueEntity.getPk());
    if (actualSeasonEntity == null) {
      throw new NotFoundException(ResponseCode.NOT_FOUND_ACTUAL_SEASON);
    }

    GambleSeason gambleSeason = GambleSeason.builder()
        .id(UUID.randomUUID().toString())
        .actualSeason(actualSeasonEntity)
        .title(request.getTitle())
        .startedAt(LocalDateTime.parse(request.getStartedAt()))
        .finishedAt(LocalDateTime.parse(request.getFinishedAt()))
        .description(request.getDescription())
        .operatingStatus(OperatingStatus.PENDING)
        .status(DataStatus.ACTIVATED)
        .build();

    gambleSeasonRepository.save(gambleSeason);

    return getGambleSeasonDetail(gambleSeason);

  }
  //endregion

  //region 승부 예측 시즌 수정
  /**
   * 기존 시즌 수정 (리그, 타이틀, 기간 등)
   */
  @Transactional
  public GambleSeasonDetailDTO updateGambleSeason(GambleSeason gambleSeason, UpdateGambleSeasonRequest request) {

    if (request.getLeaguePk() != null) {
      League leagueEntity = leagueService.findByPk(request.getLeaguePk());
      if (leagueEntity == null)
        throw new NotFoundException(ResponseCode.NOT_FOUND_LEAGUE);
      ActualSeason actualSeasonEntity =
          actualSeasonService.findRecentByLeaguePk(leagueEntity.getPk());
      if (actualSeasonEntity == null) {
        throw new NotFoundException(ResponseCode.NOT_FOUND_ACTUAL_SEASON);
      }
      gambleSeason.setActualSeason(actualSeasonEntity);
    }
    if (request.getTitle() != null) {
      gambleSeason.setTitle(request.getTitle());
    }
    if (request.getStartedAt() != null) {
      gambleSeason.setStartedAt(LocalDateTime.parse(request.getStartedAt()));
    }
    if (request.getFinishedAt() != null) {
      gambleSeason.setFinishedAt(LocalDateTime.parse(request.getFinishedAt()));
    }
    if (request.getOperatingStatus() != null) {
      try {
        gambleSeason.setOperatingStatus(OperatingStatus.valueOf(request.getOperatingStatus()));
      } catch (IllegalArgumentException e) {
        throw new BadRequestException(ResponseCode.INVALID_REQUEST);
      }
    }
    if (request.getDescription() != null) {
      gambleSeason.setDescription(request.getDescription());
    }

    // 참여 팀 목록 수정
    gambleSeasonTeamService.updateSeasonTeams(gambleSeason, request.getGambleSeasonTeams());
    gambleSeasonRepository.save(gambleSeason);

    return getGambleSeasonDetail(gambleSeason);
  }
  //endregion
}
