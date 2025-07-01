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
import kr.kickon.api.admin.gambleSeason.request.CreateGambleSeasonRequestDTO;
import kr.kickon.api.admin.gambleSeason.request.GambleSeasonFilterRequest;
import kr.kickon.api.domain.actualSeason.ActualSeasonService;
import kr.kickon.api.domain.gambleSeason.GambleSeasonRepository;
import kr.kickon.api.domain.gambleSeasonRanking.GambleSeasonRankingService;
import kr.kickon.api.domain.gambleSeasonRanking.dto.GetGambleSeasonRankingDTO;
import kr.kickon.api.domain.gambleSeasonTeam.GambleSeasonTeamService;
import kr.kickon.api.domain.league.LeagueService;
import kr.kickon.api.domain.league.dto.LeagueDTO;
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

  public GambleSeason findByPk(Long pk){
    BooleanExpression predicate = QGambleSeason.gambleSeason.pk.eq(pk).and(QGambleSeason.gambleSeason.status.eq(DataStatus.ACTIVATED));
    Optional<GambleSeason> gambleSeason = gambleSeasonRepository.findOne(predicate);
    return gambleSeason.orElse(null);
  }

  @Transactional
  public Page<GambleSeasonListDTO> findGambleSeasonByFilter(GambleSeasonFilterRequest request,
      Pageable pageable) {
    QGambleSeason season = QGambleSeason.gambleSeason;
    QActualSeason   actualSeason   = QActualSeason.actualSeason;
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
      LocalDateTime start = LocalDateTime.parse(request.getStartedAt());
      builder.and(season.startedAt.goe(start));
    }

    // 마감일 ≤ requested.finishedAt
    if (request.getFinishedAt() != null && !request.getFinishedAt().isBlank()) {
      LocalDateTime end = LocalDateTime.parse(request.getFinishedAt());
      builder.and(season.finishedAt.loe(end));
    }

    // 운영 상태 ENUM
    if (request.getOperatingStatus() != null && !request.getOperatingStatus().isBlank()) {
      OperatingStatus os = OperatingStatus.valueOf(request.getOperatingStatus());
      builder.and(season.operatingStatus.eq(os));
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

  @Transactional
  public GambleSeasonDetailDTO getGambleSeasonDetail(GambleSeason season) {
    List<SeasonTeamDTO> teamList =
        gambleSeasonTeamService.findAllByGambleSeasonPk(season.getPk());

    List<GetGambleSeasonRankingDTO> rankingList =
        gambleSeasonRankingService.getRankingDtoBySeasonPk(season.getPk());

    return GambleSeasonDetailDTO.fromEntity(season, teamList, rankingList);
  }

  @Transactional
  public GambleSeasonDetailDTO createGambleSeason(CreateGambleSeasonRequestDTO request) {

    League league = leagueService.findByPk(request.getLeaguePk());
    if (league == null) throw new NotFoundException(ResponseCode.NOT_FOUND_LEAGUE);

    // 리그에 연결된 최신 ActualSeason 가져오기
    ActualSeason actualSeason =
        actualSeasonService.findRecentByLeaguePk(league.getPk());
    if (actualSeason == null) {
      throw new NotFoundException(ResponseCode.NOT_FOUND_ACTUAL_SEASON);
    }

    GambleSeason gambleSeason = GambleSeason.builder()
        .id(UUID.randomUUID().toString())
        .actualSeason(actualSeason)
        .title(request.getTitle())
        .startedAt(LocalDateTime.parse(request.getStartedAt()))
        .finishedAt(LocalDateTime.parse(request.getFinishedAt()))
        .description(request.getDescription())
        .operatingStatus(OperatingStatus.PENDING)
        .status(DataStatus.ACTIVATED)
        .build();

    gambleSeasonRepository.save(gambleSeason);

    LeagueDTO leagueDto = new LeagueDTO(league);

    return GambleSeasonDetailDTO.builder()
        .pk(gambleSeason.getPk())
        .league(leagueDto)
        .title(gambleSeason.getTitle())
        .startedAt(gambleSeason.getStartedAt())
        .finishedAt(gambleSeason.getFinishedAt())
        .operatingStatus(gambleSeason.getOperatingStatus())
        .description(gambleSeason.getDescription())
        .build();
  }
}
