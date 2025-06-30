package kr.kickon.api.admin.gambleSeason;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import kr.kickon.api.admin.gambleSeason.dto.GambleSeasonListDTO;
import kr.kickon.api.admin.gambleSeason.request.GambleSeasonFilterRequest;
import kr.kickon.api.global.common.entities.GambleSeason;
import kr.kickon.api.global.common.entities.QActualSeason;
import kr.kickon.api.global.common.entities.QGambleSeason;
import kr.kickon.api.global.common.entities.QLeague;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.common.enums.OperatingStatus;
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
}
