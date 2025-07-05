package kr.kickon.api.domain.game;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
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
import kr.kickon.api.domain.game.response.MyPredictionStatsDTO;
import kr.kickon.api.domain.game.response.PredictOpenDTO;
import kr.kickon.api.domain.league.LeagueService;
import kr.kickon.api.domain.league.dto.LeagueDTO;
import kr.kickon.api.domain.team.dto.TeamDTO;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class GameService implements BaseService<Game> {
    private final GameRepository gameRepository;
    private final UserGameGambleService userGameGambleService;
    private final UserPointEventService userPointEventService;
    private final JPAQueryFactory queryFactory;
    private final UUIDGenerator uuidGenerator;
    public static String[] ScheduledStatus = {"TBD", "NS"};
    public static String[] FinishedStatus = {"FT", "AET", "PEN"};

    @Override
    public Game findById(String uuid) {
        BooleanExpression predicate = QGame.game.id.eq(uuid).and(QGame.game.status.eq(DataStatus.ACTIVATED));
        Optional<Game> game = gameRepository.findOne(predicate);
        return game.orElse(null);
    }

    @Override
    public Game findByPk(Long pk) {
        BooleanExpression predicate = QGame.game.pk.eq(pk).and(QGame.game.status.eq(DataStatus.ACTIVATED));
        Optional<Game> game = gameRepository.findOne(predicate);
        return game.orElse(null);
    }

    @Transactional
    public void updateGame(Long pk, GameUpdateRequest request) {
        Game game = findByPk(pk);
        if(game == null) throw new NotFoundException(ResponseCode.NOT_FOUND_GAME);

        if (request.getStartedAt() != null) {
            game.setStartedAt(request.getStartedAt());
        }
        if (request.getGameStatus() != null) {
            game.setGameStatus(request.getGameStatus());
        }
        if (request.getRound() != null) {
            game.setRound(request.getRound());
        }
        if (request.getApiId() != null) {
            game.setApiId(request.getApiId());
        }
        if (request.getHomeScore() != null) {
            game.setHomeScore(request.getHomeScore());
        }
        if (request.getAwayScore() != null) {
            game.setAwayScore(request.getAwayScore());
        }
        if (request.getHomePenaltyScore() != null) {
            game.setHomePenaltyScore(request.getHomePenaltyScore());
        }
        if (request.getAwayPenaltyScore() != null) {
            game.setAwayPenaltyScore(request.getAwayPenaltyScore());
        }

        gameRepository.save(game);
    }

    public GameDetailDTO getGameDetail(Long pk) {
        Game game = findByPk(pk);
        if(game == null) throw new NotFoundException(ResponseCode.NOT_FOUND_GAME);

        return toGameDetailDTO(game);
    }

    private GameDetailDTO toGameDetailDTO(Game game) {
        return GameDetailDTO.builder()
                .pk(game.getPk())
                .id(game.getId())
                .apiId(game.getApiId())
                .gameStatus(game.getGameStatus())
                .startedAt(game.getStartedAt())
                .homeScore(game.getHomeScore())
                .awayScore(game.getAwayScore())
                .homePenaltyScore(game.getHomePenaltyScore())
                .awayPenaltyScore(game.getAwayPenaltyScore())
                .round(game.getRound())
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
                        .operatingStatus(game.getActualSeason().getOperatingStatus()) // enum → string
                        .build())
                .build();
    }

    public List<GameListDTO> toGameListResponses(List<Game> games) {
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

    public Game findByApiId(Long apiId){
        BooleanExpression predicate = QGame.game.apiId.eq(apiId).and(QGame.game.status.eq(DataStatus.ACTIVATED));
        Optional<Game> game = gameRepository.findOne(predicate);
        if(game.isPresent()) return game.get();
        throw new NotFoundException(ResponseCode.NOT_FOUND_GAME);
    }

    public Game save(Game game) {
        return gameRepository.save(game);
    }

    public List<Game> findByActualSeason(Long actualSeasonPk, String gameStatus, LocalDateTime from, LocalDateTime to){

        JPAQuery<Game> query = queryFactory.selectFrom(QGame.game)
                .where(QGame.game.status.eq(DataStatus.ACTIVATED).and(QGame.game.actualSeason.pk.eq(actualSeasonPk)));

        LocalDateTime now = LocalDateTime.now();
        if (gameStatus.equals("finished")) {
            query.where(QGame.game.gameStatus.in(GameStatus.PROCEEDING, GameStatus.CANCELED, GameStatus.HOME,
                            GameStatus.AWAY, GameStatus.DRAW, GameStatus.PENDING).and(QGame.game.startedAt.lt(now.minusHours(2))))
                    .orderBy(QGame.game.startedAt.desc());
        } else {
            query.where(QGame.game.gameStatus.in(GameStatus.POSTPONED, GameStatus.PENDING).and(QGame.game.startedAt.goe(now.plusHours(2))))
                    .orderBy(QGame.game.startedAt.asc());
        }

        if (from != null && to != null) {
            query.where(QGame.game.startedAt.between(from, to));
        }

        return query.limit(6).fetch();
    }

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
            builder.and(QGame.game.startedAt.goe(now.plusHours(2)));
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


    public List<Game> findByToday() {
        // QGame 객체 생성
        QGame game = QGame.game;

        // 현재 시간과 4시간 전 시간 계산
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime past24Hours = now.minusHours(4);

        // QueryDSL을 사용하여 24시간 이내에 시작한 게임 중 종료된 게임을 조회
        return queryFactory
                .selectFrom(game)
                .where(
                        game.startedAt.between(past24Hours, now),  // 24시간 이내 시작한 게임
                        game.gameStatus.in(GameStatus.PENDING, GameStatus.POSTPONED,GameStatus.PROCEEDING),
                        game.status.eq(DataStatus.ACTIVATED)
                )
                .orderBy(game.startedAt.desc()) // 최신순 정렬
                .fetch();
    }

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

    public Page<Game> findGamesByFilter(GameFilterRequest filter, Pageable pageable)  {
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

    public List<LocalDate> getCalendarDates(Long leaguePk, LocalDate monthStart) {
        LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

        List<Game> games = queryFactory.selectFrom(QGame.game)
                .where(QGame.game.status.eq(DataStatus.ACTIVATED)
                        .and(QGame.game.actualSeason.league.pk.eq(leaguePk))
                        .and(QGame.game.startedAt.between(monthStart.atStartOfDay(), monthEnd.atTime(23,59,59))))
                .fetch();

        return games.stream()
                .map(game -> game.getStartedAt().toLocalDate())
                .distinct()
                .toList();
    }

    public LocalDate getNextAvailableGameDate(Long leaguePk, LocalDate today) {
        QGame game = QGame.game;
        Game nextGame = queryFactory.selectFrom(game)
                .where(
                        game.status.eq(DataStatus.ACTIVATED)
                                .and(game.actualSeason.league.pk.eq(leaguePk))
                                .and(game.gameStatus.in(GameStatus.PENDING, GameStatus.POSTPONED, GameStatus.PROCEEDING))
                                .and(game.startedAt.goe(today.atStartOfDay()))
                )
                .orderBy(game.startedAt.asc())
                .fetchFirst();

        if (nextGame == null) {
            return null;
        }
        return nextGame.getStartedAt().toLocalDate();
    }

    public PredictOpenDTO getPredictOpenPeriod(LocalDate today) {
        // 오늘 기준 가장 가까운 지난 일요일 찾기
        LocalDate startDate = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        // 그로부터 4주 후 토요일까지
        LocalDate endDate = startDate.plusWeeks(4).minusDays(1);

        return new PredictOpenDTO(startDate, endDate, 4);
    }

    public List<LocalDate> getMyPredictionDates(Long userPk) {
        return userGameGambleService.findByUserPk(userPk).stream()
                .map(gamble -> gamble.getGame().getStartedAt().toLocalDate())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public List<GameDTO> getMyPredictions(Long userPk, LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(23, 59, 59);

        return userGameGambleService.findByUserPk(userPk).stream()
                .filter(gamble -> !gamble.getGame().getStartedAt().isBefore(start)
                        && !gamble.getGame().getStartedAt().isAfter(end))
                .map(gamble -> {
                    Game game = gamble.getGame();

                    TeamDTO homeTeamDTO = new TeamDTO(game.getHomeTeam());
                    TeamDTO awayTeamDTO = new TeamDTO(game.getAwayTeam());

                    Map<PredictedResult, Long> userGamblePredictedResult = userGameGambleService.findGambleCountByGamePk(game.getPk());
                    long homeCount = userGamblePredictedResult.getOrDefault(PredictedResult.HOME, 0L);
                    long awayCount = userGamblePredictedResult.getOrDefault(PredictedResult.AWAY, 0L);
                    long drawCount = userGamblePredictedResult.getOrDefault(PredictedResult.DRAW, 0L);

                    long totalParticipation = homeCount + awayCount + drawCount;
                    int homeRatio = (totalParticipation > 0) ? (int) ((homeCount * 100) / totalParticipation) : 0;
                    int awayRatio = (totalParticipation > 0) ? (int) ((awayCount * 100) / totalParticipation) : 0;
                    int drawRatio = (totalParticipation > 0) ? (int) ((drawCount * 100) / totalParticipation) : 0;
                    GambleResultDTO gambleResultDTO = new GambleResultDTO(homeRatio, awayRatio, drawRatio, totalParticipation);

                    UserGameGambleDTO myGambleResultDTO = new kr.kickon.api.domain.userGameGamble.dto.UserGameGambleDTO(gamble);

                    GameDTO gameDTO = new GameDTO(homeTeamDTO, awayTeamDTO, game, gambleResultDTO);
                    gameDTO.setMyGambleResult(myGambleResultDTO);
                    gameDTO.setLeague(new LeagueDTO(game.getActualSeason().getLeague()));
                    return gameDTO;
                })
                .collect(Collectors.toList());
    }

    public MyPredictionStatsDTO getMyPredictionStats(Long userPk) {
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
        String mostHitTeamName = userGameGambleService.getMostHitTeamName(userPk);

        return MyPredictionStatsDTO.builder()
                .totalSuccessRate(totalSuccessRate)
                .totalParticipationCount(totalParticipation)
                .participationRate(participationRate)
                .thisMonthSuccessRate(thisMonthSuccessRate)
                .thisMonthHitSummary(thisMonthHit + "/" + thisMonthParticipation)
                .thisMonthPoints(thisMonthPoints)
                .totalPoints(totalPoints)
                .mostHitTeamName(mostHitTeamName)
                .build();
    }


}
