package kr.kickon.api.domain.game;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.transaction.Transactional;
import kr.kickon.api.admin.game.dto.GameDetailDTO;
import kr.kickon.api.admin.game.dto.GameListDTO;
import kr.kickon.api.admin.game.request.GameFilterRequest;
import kr.kickon.api.admin.game.request.GameUpdateRequest;
import kr.kickon.api.admin.migration.dto.ApiGamesDTO;
import kr.kickon.api.domain.game.dto.GambleResultDTO;
import kr.kickon.api.domain.game.dto.GameDTO;
import kr.kickon.api.domain.game.response.CalendarDateCountDTO;
import kr.kickon.api.domain.game.response.MyPredictionStatsResponse;
import kr.kickon.api.domain.game.response.PredictOpenResponse;
import kr.kickon.api.domain.league.LeagueService;
import kr.kickon.api.domain.league.dto.LeagueDTO;
import kr.kickon.api.domain.notification.NotificationService;
import kr.kickon.api.domain.team.TeamService;
import kr.kickon.api.domain.team.dto.TeamDTO;
import kr.kickon.api.domain.userFavoriteTeam.UserFavoriteTeamService;
import kr.kickon.api.domain.userGameGamble.UserGameGambleService;
import kr.kickon.api.domain.userGameGamble.dto.UserGameGambleDTO;
import kr.kickon.api.domain.userPointEvent.UserPointEventService;
import kr.kickon.api.global.common.BaseService;
import kr.kickon.api.global.common.entities.*;
import kr.kickon.api.global.common.enums.*;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import kr.kickon.api.global.util.UUIDGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class GameService implements BaseService<Game> {
    private final GameRepository gameRepository;
    private final UserGameGambleService userGameGambleService;
    private final UserPointEventService userPointEventService;
    private final TeamService teamService;
    private final JPAQueryFactory queryFactory;
    public static String[] ScheduledStatus = {"TBD", "NS"};
    public static String[] FinishedStatus = {"FT", "AET", "PEN"};
    private final UserFavoriteTeamService userFavoriteTeamService;
    private final NotificationService notificationService;

    // region {findById} Game UUID 기반 조회
    @Override
    public Game findById(String uuid) {
        BooleanExpression predicate = QGame.game.id.eq(uuid).and(QGame.game.status.eq(DataStatus.ACTIVATED));
        Optional<Game> gameEntity = gameRepository.findOne(predicate);
        return gameEntity.orElse(null);
    }
    // endregion

    // region {findByPk} Game PK 기반 조회
    @Override
    public Game findByPk(Long pk) {
        BooleanExpression predicate = QGame.game.pk.eq(pk).and(QGame.game.status.eq(DataStatus.ACTIVATED));
        Optional<Game> gameEntity = gameRepository.findOne(predicate);
        return gameEntity.orElse(null);
    }
    // endregion

    // region {findByApiId} 외부 API ID 기반 조회
    public Game findByApiId(Long apiId){
        log.info("api id : {}", apiId.toString());
        BooleanExpression predicate = QGame.game.apiId.eq(apiId).and(QGame.game.status.eq(DataStatus.ACTIVATED));
        Optional<Game> gameEntity = gameRepository.findOne(predicate);
        if(gameEntity.isPresent()) return gameEntity.get();
        throw new NotFoundException(ResponseCode.NOT_FOUND_GAME);
    }
    // endregion

    // region {findByActualSeason} 실제 시즌 기반 경기 목록 조회
    /**
     * 실제 시즌 PK 및 상태에 따른 게임 리스트 조회
     * - 진행 중이거나 완료된 경기, 예정된 경기 구분
     * - 시간 범위(from~to)가 있으면 해당 기간 필터링
     * - proceeding 상태일 경우 날짜 기준으로 조회
     * - 최대 6개까지 조회
     */
    public List<Game> findByActualSeason(Long actualSeasonPk, String gameStatus, LocalDateTime from, LocalDateTime to){

        JPAQuery<Game> query = queryFactory.selectFrom(QGame.game)
                .where(QGame.game.status.eq(DataStatus.ACTIVATED).and(QGame.game.actualSeason.pk.eq(actualSeasonPk)));

        LocalDateTime now = LocalDateTime.now();
        if (gameStatus.equals("finished")) {
            query.where(QGame.game.gameStatus.in(GameStatus.PROCEEDING, GameStatus.CANCELED, GameStatus.HOME,
                            GameStatus.AWAY, GameStatus.DRAW, GameStatus.PENDING).and(QGame.game.startedAt.lt(now.minusHours(2))))
                    .orderBy(QGame.game.startedAt.desc());
        } else {
            query.where(QGame.game.gameStatus.in(GameStatus.POSTPONED, GameStatus.PENDING))
                    .orderBy(QGame.game.startedAt.asc());
        }

        if (from != null && to != null) {
            query.where(QGame.game.startedAt.between(from, to));
        }

        return query.limit(6).fetch();
    }
    // endregion

    // region {findByActualSeasonByFavoriteTeam} 사용자의 응원 팀 기준 경기 조회
    /**
     * 실제 시즌 PK, 경기 상태, 선호 팀 PK 등을 기반으로 경기를 조회한다.
     * - homeTeam 또는 awayTeam이 사용자의 응원 팀인 경기만 필터링
     * - gameStatus 기준으로 'finished' or 'proceeding' 분기
     * - 시간 필터(from ~ to) 존재 시 해당 범위 내 경기만 필터링
     * - 팀당 최대 limitPerTeam 개수만큼 조회
     */
    public List<Game> findByActualSeasonByFavoriteTeam(
            Long actualSeasonPk,
            String gameStatus,
            Long favoriteTeamPk,
            int limitPerTeam,
            LocalDateTime from,
            LocalDateTime to
    ) {
        LocalDateTime now = LocalDateTime.now();
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QGame.game.status.eq(DataStatus.ACTIVATED));
        builder.and(QGame.game.actualSeason.pk.eq(actualSeasonPk));
        builder.and(QGame.game.homeTeam.pk.eq(favoriteTeamPk)
                .or(QGame.game.awayTeam.pk.eq(favoriteTeamPk)));

        if ("finished".equalsIgnoreCase(gameStatus)) {
            builder.and(QGame.game.gameStatus.in(
                    GameStatus.PROCEEDING,
                    GameStatus.CANCELED,
                    GameStatus.HOME,
                    GameStatus.AWAY,
                    GameStatus.DRAW,
                    GameStatus.PENDING
            ));
            builder.and(QGame.game.startedAt.lt(now.minusHours(2)));
        } else {
            builder.and(QGame.game.gameStatus.in(
                    GameStatus.POSTPONED,
                    GameStatus.PENDING
            ));
        }

        if (from != null && to != null) {
            builder.and(QGame.game.startedAt.between(from, to));
        }

        return queryFactory.selectFrom(QGame.game)
                .where(builder)
                .orderBy("finished".equalsIgnoreCase(gameStatus) ?
                        QGame.game.startedAt.desc() :
                        QGame.game.startedAt.asc())
                .limit(limitPerTeam)
                .fetch();
    }
    // endregion

    // region {save} 게임 저장
    public Game save(Game game) {
        return gameRepository.save(game);
    }
    // endregion

    // region {updateGame} 게임 정보 수정
    @Transactional
    public void updateGame(Long pk, GameUpdateRequest request) {
        Game gameEntity = findByPk(pk);
        if(gameEntity == null) throw new NotFoundException(ResponseCode.NOT_FOUND_GAME);

        if (request.getStartedAt() != null) {
            gameEntity.setStartedAt(request.getStartedAt());
        }
        if (request.getGameStatus() != null) {
            gameEntity.setGameStatus(request.getGameStatus());
        }
        if (request.getRound() != null) {
            gameEntity.setRound(request.getRound());
        }
        if (request.getApiId() != null) {
            gameEntity.setApiId(request.getApiId());
        }
        if (request.getHomeScore() != null) {
            gameEntity.setHomeScore(request.getHomeScore());
        }
        if (request.getAwayScore() != null) {
            gameEntity.setAwayScore(request.getAwayScore());
        }
        if (request.getHomePenaltyScore() != null) {
            gameEntity.setHomePenaltyScore(request.getHomePenaltyScore());
        }
        if (request.getAwayPenaltyScore() != null) {
            gameEntity.setAwayPenaltyScore(request.getAwayPenaltyScore());
        }

        gameRepository.save(gameEntity);
    }
    // endregion

    // region {getGameListByToday} 오늘 진행 예정인 경기 조회
    public List<Game> getPendingGames() {
        // QGame 객체 생성
        QGame game = QGame.game;

        // 현재 시간과 4시간 전 시간 계산
        LocalDateTime now = LocalDateTime.now().minusHours(3);
        LocalDateTime past24Hours = now.minusHours(4320);

        // QueryDSL을 사용하여 24시간 이내에 시작한 게임 중 종료된 게임을 조회
        return queryFactory
                .selectFrom(game)
                .where(
                        game.startedAt.between(past24Hours, now),  // 24시간 이내 시작한 게임
                        game.gameStatus.in(GameStatus.PENDING, GameStatus.POSTPONED, GameStatus.PROCEEDING),
                        game.status.eq(DataStatus.ACTIVATED)
                )
                .orderBy(game.startedAt.desc()) // 최신순 정렬
                .fetch();
    }
    // endregion

    // region {getGameDetail} 게임 상세 정보 조회
    public GameDetailDTO getGameDetail(Long pk) {
        Game gameEntity = findByPk(pk);
        if(gameEntity == null) throw new NotFoundException(ResponseCode.NOT_FOUND_GAME);

        return GameDetailDTO.builder()
                .pk(gameEntity.getPk())
                .id(gameEntity.getId())
                .apiId(gameEntity.getApiId())
                .gameStatus(gameEntity.getGameStatus())
                .startedAt(gameEntity.getStartedAt())
                .homeScore(gameEntity.getHomeScore())
                .awayScore(gameEntity.getAwayScore())
                .homePenaltyScore(gameEntity.getHomePenaltyScore())
                .awayPenaltyScore(gameEntity.getAwayPenaltyScore())
                .round(gameEntity.getRound())
                .etc(gameEntity.getEtc())
                .homeTeam(GameListDTO.TeamSummary.builder()
                        .pk(gameEntity.getHomeTeam().getPk())
                        .nameKr(gameEntity.getHomeTeam().getNameKr())
                        .nameEn(gameEntity.getHomeTeam().getNameEn())
                        .logoUrl(gameEntity.getHomeTeam().getLogoUrl())
                        .build())
                .awayTeam(GameListDTO.TeamSummary.builder()
                        .pk(gameEntity.getAwayTeam().getPk())
                        .nameKr(gameEntity.getAwayTeam().getNameKr())
                        .nameEn(gameEntity.getAwayTeam().getNameEn())
                        .logoUrl(gameEntity.getAwayTeam().getLogoUrl())
                        .build())
                .actualSeason(GameListDTO.ActualSeasonSummary.builder()
                        .pk(gameEntity.getActualSeason().getPk())
                        .title(gameEntity.getActualSeason().getTitle())
                        .year(gameEntity.getActualSeason().getYear())
                        .operatingStatus(gameEntity.getActualSeason().getOperatingStatus()) // enum → string
                        .build())
                .build();
    }
    // endregion

    // region {getGameListByFilter} 조건 필터 기반 경기 목록 페이징 조회
    /**
     * 필터 조건(GameFilterRequest)에 따라 경기를 조회하고 페이징 처리하여 반환합니다.
     * - 팀 PK, 게임 상태, 시작일 범위, 시즌 연도 및 리그 기준 필터링
     * - 실제 시즌, 홈팀, 어웨이팀까지 fetchJoin 포함
     */
    public Page<Game> getGameListByFilter(GameFilterRequest filter, Pageable pageable)  {
        QGame game = QGame.game;
        QActualSeason actualSeason = QActualSeason.actualSeason;

        BooleanBuilder builder = new BooleanBuilder();

        // 팀 PK (home 또는 away)
        if (filter.getTeamPk() != null) {
            builder.and(game.homeTeam.pk.eq(filter.getTeamPk())
                    .or(game.awayTeam.pk.eq(filter.getTeamPk())));
        }

        // GameStatus 목록
        if (filter.getGameStatuses() != null && !filter.getGameStatuses().isEmpty()) {
            builder.and(game.gameStatus.in(filter.getGameStatuses()));
        }

        // 경기 시작/종료 시점
        if (filter.getStartedFrom() != null) {
            builder.and(game.startedAt.goe(filter.getStartedFrom()));
        }
        if (filter.getStartedTo() != null) {
            builder.and(game.startedAt.loe(filter.getStartedTo()));
        }

        // ActualSeason 연동 조건
        if (filter.getYear() != null || filter.getLeaguePk() != null) {
            BooleanBuilder seasonCondition = new BooleanBuilder();

            if (filter.getYear() != null) {
                seasonCondition.and(game.actualSeason.year.eq(filter.getYear()));
            }

            if (filter.getLeaguePk() != null) {
                seasonCondition.and(game.actualSeason.league.pk.eq(filter.getLeaguePk()));
            }

            builder.and(seasonCondition);
        }

        builder.and(game.status.eq(DataStatus.ACTIVATED));

        List<Game> content = queryFactory
                .selectFrom(game)
                .leftJoin(game.homeTeam).fetchJoin()
                .leftJoin(game.awayTeam).fetchJoin()
                .leftJoin(game.actualSeason).fetchJoin()
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(game.startedAt.asc())
                .fetch();

        long total = queryFactory
                .select(game.countDistinct())
                .from(game)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }
    // endregion

    // region {getGameStatus} 외부 API 경기 데이터 기반 게임 상태 조회
    /**
     * 외부 API 경기 데이터를 바탕으로 우리 시스템의 GameStatus 값을 계산합니다.
     * - 예정(PENDING), 완료(Finished-> HOME / AWAY / DRAW), 연기(POSTPONED), 취소(CANCELED), 진행 중(PROCEEDING) 여부 판단
     * - 승부차기 여부에 따라 HOME / AWAY 구분
     */
    public static @NotNull GameStatus getGameStatus(ApiGamesDTO apiData, List<String> scheduledStatus, List<String> finishedStatus) {
        GameStatus gameStatus;
        if(scheduledStatus.contains(apiData.getStatus())){
            // 시작 안한 경기
            gameStatus = GameStatus.PENDING;
        }

        else if(finishedStatus.contains(apiData.getStatus())){
            // 끝난 경기
            if(apiData.getStatus().equals("PEN")){
                // 승부차기인지 체크
                gameStatus = apiData.getHomePenaltyScore() > apiData.getAwayPenaltyScore() ? GameStatus.HOME : GameStatus.AWAY;
            } else {
                // 일반적으로 경기 마무리 된 경우
                gameStatus = apiData.getHomeScore().equals(apiData.getAwayScore()) ? GameStatus.DRAW : apiData.getHomeScore() > apiData.getAwayScore() ? GameStatus.HOME : GameStatus.AWAY;
            }
        } else if(apiData.getStatus().equals("PST")){
            // 연기된 경기
            gameStatus = GameStatus.POSTPONED;
        } else if(apiData.getStatus().equals("CANC") || apiData.getStatus().equals("ABD")){
            // 취소된 경기
            gameStatus = GameStatus.CANCELED;
        } else {
            // 진행중인 경기
            gameStatus = GameStatus.PROCEEDING;
        }
        return gameStatus;
    }
    // endregion

    // region {getCalendarDateListByMyTeams} 사용자의 응원 팀 기준으로 해당 월 경기 날짜별 개수 조회
    /**
     * 사용자의 즐겨찾기 팀 기준으로, 특정 월(monthStart)에 진행 예정 또는 진행된 경기를 조회합니다.
     * - 게임 시작일을 기준으로 날짜별 개수를 카운트하여 반환
     * - today 이후 날짜만 포함되도록 조정
     */
    public List<CalendarDateCountDTO> getCalendarDateListByMyTeams(Long userPk, LocalDate monthStart) {
        LocalDate today = LocalDate.now();
        LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());
        LocalDate fromDate = monthStart.isBefore(today) && monthStart.getMonth() == today.getMonth() ? today : monthStart;

        List<UserFavoriteTeam> favoriteTeams = userFavoriteTeamService.findAllByUserPk(userPk);

        if (favoriteTeams == null || favoriteTeams.isEmpty()) {
            return List.of();
        }

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QGame.game.status.eq(DataStatus.ACTIVATED));
        builder.and(QGame.game.startedAt.between(fromDate.atStartOfDay(), monthEnd.atTime(23,59,59)));

        BooleanBuilder teamCondition = new BooleanBuilder();
        for (UserFavoriteTeam fav : favoriteTeams) {
            teamCondition.or(QGame.game.homeTeam.pk.eq(fav.getTeam().getPk()));
            teamCondition.or(QGame.game.awayTeam.pk.eq(fav.getTeam().getPk()));
        }
        builder.and(teamCondition);

        List<Game> games = queryFactory.selectFrom(QGame.game)
                .where(builder)
                .fetch();

        // 날짜별 count 집계
        Map<LocalDate, Long> dateCountMap = games.stream()
                .collect(Collectors.groupingBy(
                        g -> g.getStartedAt().toLocalDate(),
                        Collectors.counting()
                ));

        return dateCountMap.entrySet().stream()
                .map(e -> new CalendarDateCountDTO(e.getKey(), e.getValue().intValue()))
                .sorted(Comparator.comparing(CalendarDateCountDTO::getDate))
                .toList();
    }
    // endregion

    // region {getNextAvailableGameDateDetail} 사용자 응원 팀의 다음 경기 날짜 조회
    /**
     * 사용자의 응원 팀들이 포함된 경기 중 오늘 이후로 가장 가까운 경기의 날짜를 반환합니다.
     * - 대상 게임은 PENDING, POSTPONED, PROCEEDING 상태여야 함
     * - 홈팀 또는 어웨이팀으로 포함된 경기만 대상
     */
    public LocalDate getNextAvailableGameDateDetail(Long userPk, LocalDate today) {

        QGame game = QGame.game;

        // 사용자의 응원팀 PK 리스트 조회
        List<Long> favoriteTeamPks = userFavoriteTeamService.findAllByUserPk(userPk).stream()
                .map(userFavoriteTeam -> userFavoriteTeam.getTeam().getPk())
                .toList();

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(game.status.eq(DataStatus.ACTIVATED));
        builder.and(game.gameStatus.in(GameStatus.PENDING, GameStatus.POSTPONED, GameStatus.PROCEEDING));
        builder.and(game.startedAt.goe(today.atStartOfDay()));

        if (!favoriteTeamPks.isEmpty()) {
            // 응원팀이 있는 경우: 홈 또는 어웨이 팀으로 조건 추가
            builder.and(
                    game.homeTeam.pk.in(favoriteTeamPks)
                            .or(game.awayTeam.pk.in(favoriteTeamPks))
            );
        } else {
            // 응원팀이 없는 경우: 프리미어리그 기준 가장 가까운 예정 경기 날짜 반환
            Long premierLeaguePk = 1L;
            builder.and(game.actualSeason.league.pk.eq(premierLeaguePk));
        }

        Game nextGameEntity = queryFactory.selectFrom(game)
                .where(builder)
                .orderBy(game.startedAt.asc())
                .fetchFirst();

        return (nextGameEntity != null) ? nextGameEntity.getStartedAt().toLocalDate() : null;
    }
    // endregion

    // region {getPredictOpenPeriodDetail} 승부 예측 기능 오픈 기간 계산
    /**
     * 사용자의 응원팀 중, 가장 가까운 경기 날짜를 기준으로 승부 예측 오픈 기간(start~end)을 계산합니다.
     * - 대상: 오늘 이후, 응원팀이 홈/원정팀으로 포함된 경기
     * - 예측 기간: 경기 날짜 ~ 28일 후까지
     */
    public PredictOpenResponse getPredictOpenPeriodDetail(Long userPk) {
        // 오늘 이후 내 응원팀의 가장 가까운 경기 날짜 찾기
        LocalDateTime nearestGameDateTime = queryFactory
                .select(QGame.game.startedAt.min())
                .from(QGame.game)
                .where(
                        QGame.game.status.eq(DataStatus.ACTIVATED),
                        QGame.game.gameStatus.in(GameStatus.PENDING, GameStatus.POSTPONED, GameStatus.PROCEEDING),
                        QGame.game.startedAt.goe(LocalDate.now().atStartOfDay()),
                        QGame.game.homeTeam.pk.in(
                                JPAExpressions.select(QUserFavoriteTeam.userFavoriteTeam.team.pk)
                                        .from(QUserFavoriteTeam.userFavoriteTeam)
                                        .where(QUserFavoriteTeam.userFavoriteTeam.user.pk.eq(userPk))
                        ).or(QGame.game.awayTeam.pk.in(
                                JPAExpressions.select(QUserFavoriteTeam.userFavoriteTeam.team.pk)
                                        .from(QUserFavoriteTeam.userFavoriteTeam)
                                        .where(QUserFavoriteTeam.userFavoriteTeam.user.pk.eq(userPk))
                        ))
                )
                .fetchFirst();

        if (nearestGameDateTime == null) {
            throw new NotFoundException(ResponseCode.NOT_FOUND_GAME);
        }

        LocalDate startDate = nearestGameDateTime.toLocalDate();
        LocalDate endDate = startDate.plusDays(28);

        return new PredictOpenResponse(startDate, endDate, 4);

    }
    // endregion

    // region {getMyPredictionDateList} 유저의 승부예측 날짜 목록 조회
    /**
     * 사용자가 참여한 모든 경기 예측(UserGameGamble) 내역 중,
     * 예측한 게임의 시작일(LocalDate)을 기준으로 날짜별 개수를 집계하여 정렬된 리스트로 반환합니다.
     */
    public List<CalendarDateCountDTO> getMyPredictionDateList(Long userPk) {
        return userGameGambleService.findByUserPk(userPk).stream()
                .collect(Collectors.groupingBy(
                        g -> g.getGame().getStartedAt().toLocalDate(),
                        Collectors.counting()
                ))
                .entrySet()
                .stream()
                .map(entry -> new CalendarDateCountDTO(entry.getKey(), entry.getValue().intValue()))
                .sorted(Comparator.comparing(CalendarDateCountDTO::getDate))
                .toList();
    }
    // endregion

    // region {getMyPredictionList} 유저의 예측한 경기 리스트 조회
    /**
     * 사용자가 특정 기간(from ~ to) 동안 예측한 경기(Game)을 조회하여
     * GameDTO 목록으로 반환합니다.
     * 각 경기에는 다음 정보가 포함됩니다:
     * - 홈팀 / 어웨이팀
     * - 승부 예측 분포(GambleResultDTO)
     * - 내가 예측한 결과(UserGameGambleDTO)
     * - 소속 리그 정보(LeagueDTO)
     */
    public List<GameDTO> getMyPredictionList(Long userPk, LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(23, 59, 59);

        return userGameGambleService.findByUserPk(userPk).stream()
                .filter(gamble -> !gamble.getGame().getStartedAt().isBefore(start)
                        && !gamble.getGame().getStartedAt().isAfter(end))
                .map(gamble -> {
                    Game gameEntity = gamble.getGame();

                    TeamDTO homeTeamDTO = new TeamDTO(gameEntity.getHomeTeam());
                    TeamDTO awayTeamDTO = new TeamDTO(gameEntity.getAwayTeam());

                    Map<PredictedResult, Long> userGamblePredictedResult = userGameGambleService.findGambleCountByGamePk(gameEntity.getPk());
                    long homeCount = userGamblePredictedResult.getOrDefault(PredictedResult.HOME, 0L);
                    long awayCount = userGamblePredictedResult.getOrDefault(PredictedResult.AWAY, 0L);
                    long drawCount = userGamblePredictedResult.getOrDefault(PredictedResult.DRAW, 0L);

                    long totalParticipation = homeCount + awayCount + drawCount;
                    int homeRatio = (totalParticipation > 0) ? (int) ((homeCount * 100) / totalParticipation) : 0;
                    int awayRatio = (totalParticipation > 0) ? (int) ((awayCount * 100) / totalParticipation) : 0;
                    int drawRatio = (totalParticipation > 0) ? (int) ((drawCount * 100) / totalParticipation) : 0;
                    GambleResultDTO gambleResultDTO = new GambleResultDTO(homeRatio, awayRatio, drawRatio, totalParticipation);

                    UserGameGambleDTO myGambleResultDTO = new kr.kickon.api.domain.userGameGamble.dto.UserGameGambleDTO(gamble);

                    GameDTO gameDTO = new GameDTO(homeTeamDTO, awayTeamDTO, gameEntity, gambleResultDTO);
                    gameDTO.setMyGambleResult(myGambleResultDTO);
                    gameDTO.setLeague(new LeagueDTO(gameEntity.getActualSeason().getLeague()));
                    return gameDTO;
                })
                .collect(Collectors.toList());
    }
    // endregion

    // region {getMyPredictionStats} 나의 예측 통계 정보 조회
    /**
     * 사용자 PK 기준으로 예측 통계 정보를 집계하여 반환합니다.
     * 포함 내용:
     * - 전체 예측 횟수, 적중률
     * - 응원팀 경기 대비 참여율
     * - 이번 달 예측 참여 및 적중률
     * - 이번 달 및 전체 획득 포인트
     * - 가장 많이 적중한 팀 정보
     */
    public MyPredictionStatsResponse getMyPredictionStats(Long userPk) {
        // 1. 전체 예측 참여 내역 조회
        List<UserGameGamble> gambles = userGameGambleService.findByUserPk(userPk);

        int totalParticipation = gambles.size();
        int totalHit = (int) gambles.stream()
                .filter(g -> g.getGambleStatus() == GambleStatus.SUCCEED || g.getGambleStatus() == GambleStatus.PERFECT)
                .count();
        double totalSuccessRate = (totalParticipation > 0) ? (double) totalHit / totalParticipation : 0.0;

        // 2. 내 응원팀 경기 대비 참여율
        int totalFavoriteGames = userGameGambleService.countFavoriteTeamGames(userPk);
        double participationRate = (totalFavoriteGames > 0) ? (double) totalParticipation / totalFavoriteGames : 0.0;

        // 3. 이번 달 통계
        LocalDate now = LocalDate.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = now.withDayOfMonth(now.lengthOfMonth()).atTime(23,59,59);

        List<UserGameGamble> thisMonthGambles = gambles.stream()
                .filter(g -> !g.getCreatedAt().isBefore(startOfMonth) && !g.getCreatedAt().isAfter(endOfMonth))
                .toList();

        int thisMonthParticipation = thisMonthGambles.size();
        int thisMonthHit = (int) thisMonthGambles.stream()
                .filter(g -> g.getGambleStatus() == GambleStatus.SUCCEED || g.getGambleStatus() == GambleStatus.PERFECT)
                .count();
        double thisMonthSuccessRate = (thisMonthParticipation > 0) ? (double) thisMonthHit / thisMonthParticipation : 0.0;

        // 4. 포인트
        int thisMonthPoints = userPointEventService.getPointSumByUser(userPk, startOfMonth, endOfMonth);
        int totalPoints = userPointEventService.getPointSumByUser(userPk, null, null);

        // 5. 가장 많이 적중한 응원팀
        String mostHitTeamName = null;
        String mostHitTeamLogoUrl = null;
        String mostHitTeamColor = null;

        QUserGameGamble gamble = QUserGameGamble.userGameGamble;

        List<Tuple> supportCounts = queryFactory
                .select(gamble.supportingTeam.pk, gamble.count())
                .from(gamble)
                .where(
                        gamble.user.pk.eq(userPk),
                        gamble.gambleStatus.in(GambleStatus.SUCCEED, GambleStatus.PERFECT)
                )
                .groupBy(gamble.supportingTeam.pk)
                .fetch();

        Map<Long, Long> teamCountMap = new HashMap<>();
        for (Tuple t : supportCounts) {
            Long teamPk = t.get(0, Long.class);
            Long count = t.get(1, Long.class);
            teamCountMap.put(teamPk, count);
        }

        Long mostHitTeamPk = teamCountMap.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        if (mostHitTeamPk != null) {
            Team team = teamService.findByPk(mostHitTeamPk);
            if (team != null) {
                mostHitTeamName = team.getNameKr();
                mostHitTeamLogoUrl = team.getSmallLogoUrl();
                mostHitTeamColor = team.getTeamColor();
            }
        }

        return MyPredictionStatsResponse.builder()
                .totalSuccessRate(totalSuccessRate)
                .totalParticipationCount(totalParticipation)
                .totalParticipationRate(participationRate)
                .thisMonthSuccessRate(thisMonthSuccessRate)
                .thisMonthHitSummary(thisMonthHit + "/" + thisMonthParticipation)
                .thisMonthPoints(thisMonthPoints)
                .totalPoints(totalPoints)
                .mostHitTeamName(mostHitTeamName)
                .mostHitTeamLogoUrl(mostHitTeamLogoUrl)
                .mostHitTeamColor(mostHitTeamColor)
                .build();
    }
    // endregion

    // region {fromGameList} Game 엔티티 리스트를 GameListDTO 리스트로 변환합니다.
    /**
     * Game 엔티티 리스트를 GameListDTO 리스트로 변환합니다.
     */
    public List<GameListDTO> fromGameList(List<Game> games) {
        return games.stream().map(game -> GameListDTO.builder()
                .pk(game.getPk())
                .gameStatus(game.getGameStatus())
                .etc(game.getEtc())
                .homeTeam(GameListDTO.TeamSummary.builder()
                        .pk(game.getHomeTeam().getPk())
                        .nameKr(game.getHomeTeam().getNameKr())
                        .nameEn(game.getHomeTeam().getNameEn())
                        .logoUrl(game.getHomeTeam().getLogoUrl())
                        .build())
                .awayTeam(GameListDTO.TeamSummary.builder()
                        .pk(game.getAwayTeam().getPk())
                        .nameKr(game.getAwayTeam().getNameKr())
                        .nameEn(game.getAwayTeam().getNameEn())
                        .logoUrl(game.getAwayTeam().getLogoUrl())
                        .build())
                .actualSeason(GameListDTO.ActualSeasonSummary.builder()
                        .pk(game.getActualSeason().getPk())
                        .title(game.getActualSeason().getTitle())
                        .year(game.getActualSeason().getYear())
                        .operatingStatus(game.getActualSeason().getOperatingStatus())
                        .build())
                .build()
        ).toList();
    }
    // endregion

    public void notifyGameFinished(Game game) {
        Set<User> usersToNotify = new HashSet<>();

        usersToNotify.addAll(userFavoriteTeamService.findUsersByTeamPk(game.getHomeTeam().getPk()));
        usersToNotify.addAll(userFavoriteTeamService.findUsersByTeamPk(game.getAwayTeam().getPk()));

        String redirectUrl = "/gamble/" + game.getPk();

        for (User user : usersToNotify) {
            notificationService.sendNotification(
                    user,
                    "GAME_RESULT",
                    game.getHomeTeam().getNameKr() + " vs " + game.getAwayTeam().getNameKr() + " 경기가 종료됐어요. 승부예측결과를 확인해 보세요.",
                    redirectUrl
            );
        }
    }

    public void notifyGamesBeforeHours(int hoursBefore) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime targetStart = now.plusHours(hoursBefore).minusMinutes(2);
        LocalDateTime targetEnd = now.plusHours(hoursBefore).plusMinutes(2);

        List<Game> games = gameRepository.findByStartedAtBetween(targetStart, targetEnd);

        for (Game game : games) {
            Set<User> usersToNotify = new HashSet<>();
            usersToNotify.addAll(userFavoriteTeamService.findUsersByTeamPk(game.getHomeTeam().getPk()));
            usersToNotify.addAll(userFavoriteTeamService.findUsersByTeamPk(game.getAwayTeam().getPk()));

            String redirectUrl = "/gamble/" + game.getPk();
            String message;
            String type;

            if (hoursBefore == 72) {
                message = String.format("%s vs %s 경기가 D-3 남았어요.", game.getHomeTeam().getNameKr(), game.getAwayTeam().getNameKr());
                type = "GAME_REMINDER_D3";
            } else if (hoursBefore == 24) {
                message = String.format("%s vs %s 경기가 D-1 남았어요.", game.getHomeTeam().getNameKr(), game.getAwayTeam().getNameKr());
                type = "GAME_REMINDER_D1";
            } else {
                continue;
            }

            for (User user : usersToNotify) {
                notificationService.sendNotification(user, type, message, redirectUrl);
            }
        }
    }


}
