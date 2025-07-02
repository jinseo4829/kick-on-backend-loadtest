package kr.kickon.api.admin.actualSeason;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import kr.kickon.api.admin.actualSeason.dto.ActualSeasonDetailDTO;
import kr.kickon.api.admin.actualSeason.request.ActualSeasonFilterRequest;
import kr.kickon.api.admin.gambleSeason.dto.GambleSeasonListDTO;
import kr.kickon.api.domain.actualSeason.ActualSeasonRepository;
import kr.kickon.api.domain.actualSeasonTeam.ActualSeasonTeamService;
import kr.kickon.api.domain.team.dto.SeasonTeamDTO;
import kr.kickon.api.global.common.entities.ActualSeason;
import kr.kickon.api.global.common.entities.QActualSeason;
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
public class AdminActualSeasonService {

  private final JPAQueryFactory queryFactory;
  private final ActualSeasonRepository actualSeasonRepository;
  private final ActualSeasonTeamService actualSeasonTeamService;
  public ActualSeason findByPk(Long pk){
    BooleanExpression predicate = QActualSeason.actualSeason.pk.eq(pk).and(QActualSeason.actualSeason.status.eq(DataStatus.ACTIVATED));
    Optional<ActualSeason> actualSeason = actualSeasonRepository.findOne(predicate);
    return actualSeason.orElse(null);
  }

  @Transactional
  public Page<GambleSeasonListDTO> findActualSeasonByFilter(ActualSeasonFilterRequest request,
      Pageable pageable) {
    QActualSeason actualSeason   = QActualSeason.actualSeason;
    QLeague league = QLeague.league;

    BooleanBuilder builder = new BooleanBuilder()
        .and(actualSeason.status.eq(DataStatus.ACTIVATED));

    // 리그 PK
    if (request.getLeaguePk() != null) {
      builder.and(actualSeason.league.eq(league));
      builder.and(league.pk.eq(request.getLeaguePk()));    }

    // 운영 상태 ENUM
    if (request.getOperatingStatus() != null && !request.getOperatingStatus().isBlank()) {
      OperatingStatus os = OperatingStatus.valueOf(request.getOperatingStatus());
      builder.and(actualSeason.operatingStatus.eq(os));
    }

    // total count
    long total = Optional.ofNullable(
        queryFactory.select(actualSeason.count())
            .from(actualSeason)
            .leftJoin(actualSeason.league, league)
            .where(builder)
            .fetchOne()
    ).orElse(0L);

    // content
    List<ActualSeason> content = queryFactory
        .selectFrom(actualSeason)
        .leftJoin(actualSeason.league, league).fetchJoin()
        .where(builder)
        .orderBy(actualSeason.startedAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    List<GambleSeasonListDTO> dtos = content.stream()
        .map(GambleSeasonListDTO::fromActualSeason)
        .toList();

    return new PageImpl<>(dtos, pageable, total);
  }

  @Transactional
  public ActualSeasonDetailDTO getActualSeasonDetail(ActualSeason season) {
    List<SeasonTeamDTO> teamList =
        actualSeasonTeamService.findAllByActualSeasonPk(season.getPk());

    return ActualSeasonDetailDTO.fromEntity(season, teamList);
  }
}
