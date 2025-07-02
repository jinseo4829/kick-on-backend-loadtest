package kr.kickon.api.admin.actualSeason;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import kr.kickon.api.admin.actualSeason.dto.ActualSeasonDetailDTO;
import kr.kickon.api.admin.actualSeason.request.ActualSeasonFilterRequest;
import kr.kickon.api.admin.actualSeason.request.PatchActualSeasonRequestDTO;
import kr.kickon.api.admin.gambleSeason.dto.GambleSeasonListDTO;
import kr.kickon.api.domain.actualSeason.ActualSeasonRepository;
import kr.kickon.api.domain.actualSeasonTeam.ActualSeasonTeamService;
import kr.kickon.api.domain.league.LeagueService;
import kr.kickon.api.domain.team.dto.SeasonTeamDTO;
import kr.kickon.api.global.common.entities.ActualSeason;
import kr.kickon.api.global.common.entities.League;
import kr.kickon.api.global.common.entities.QActualSeason;
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
public class AdminActualSeasonService {

  private final JPAQueryFactory queryFactory;
  private final ActualSeasonRepository actualSeasonRepository;
  private final ActualSeasonTeamService actualSeasonTeamService;
  private final LeagueService leagueService;

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

  @Transactional
  public ActualSeasonDetailDTO patchActualSeason(ActualSeason season, PatchActualSeasonRequestDTO request) {

    if (request.getLeaguePk() != null) {
      League league = leagueService.findByPk(request.getLeaguePk());
      if (league == null)
        throw new NotFoundException(ResponseCode.NOT_FOUND_LEAGUE);
      season.setLeague(league);
    }
    if (request.getTitle() != null) {
      season.setTitle(request.getTitle());
    }
    if (request.getStartedAt() != null) {
      season.setStartedAt(LocalDate.from(LocalDateTime.parse(request.getStartedAt())));
    }
    if (request.getFinishedAt() != null) {
      season.setFinishedAt(LocalDate.from(LocalDateTime.parse(request.getFinishedAt())));
    }
    if (request.getOperatingStatus() != null) {
      try {
        season.setOperatingStatus(OperatingStatus.valueOf(request.getOperatingStatus()));
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("상태 값이 유효하지 않습니다: " + request.getOperatingStatus());
      }
    }
    if (request.getDescription() != null) {
      season.setDescription(request.getDescription());
    }
    if (request.getYear() != null) {
      season.setYear(request.getYear());
    }
    // 참여 팀 목록 수정
    actualSeasonTeamService.patchSeasonTeams(season, request.getActualSeasonTeams());
    actualSeasonRepository.save(season);

    List<SeasonTeamDTO> teamList =
        actualSeasonTeamService.findAllByActualSeasonPk(season.getPk());

    return ActualSeasonDetailDTO.fromEntity(season, teamList);
  }
}
