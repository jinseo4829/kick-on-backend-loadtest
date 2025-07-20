package kr.kickon.api.admin.actualSeason;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import kr.kickon.api.admin.actualSeason.dto.ActualSeasonDetailDTO;
import kr.kickon.api.admin.actualSeason.request.ActualSeasonFilterRequest;
import kr.kickon.api.admin.actualSeason.request.UpdateActualSeasonRequest;
import kr.kickon.api.admin.gambleSeason.dto.SeasonListDTO;
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
public class AdminActualSeasonService {

  private final JPAQueryFactory queryFactory;
  private final ActualSeasonRepository actualSeasonRepository;
  private final ActualSeasonTeamService actualSeasonTeamService;
  private final LeagueService leagueService;

  //region 실제 시즌 단건 조회
  /**
   * pk로 ActualSeason 단건 조회
   */
  public ActualSeason findByPk(Long pk){
    return actualSeasonRepository.findByPkAndStatus(pk, DataStatus.ACTIVATED)
        .orElse(null);
  }
  //endregion

  //region 실제 시즌 리스트 조회 (필터 optional)
  /**
   * 필터 조건에 따라 실제 시즌 목록 조회
   */
  @Transactional
  public Page<SeasonListDTO> getActualSeasonListByFilter(ActualSeasonFilterRequest request,
      Pageable pageable) {
    QActualSeason actualSeason = QActualSeason.actualSeason;
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

    List<SeasonListDTO> dtos = content.stream()
        .map(SeasonListDTO::fromActualSeason)
        .toList();

    return new PageImpl<>(dtos, pageable, total);
  }
  //endregion

  //region 실제 시즌 상세 조회
  /**
   * 시즌 단건 조회 후 상세 DTO 변환
   */
  @Transactional
  public ActualSeasonDetailDTO getActualSeasonDetail(ActualSeason actualSeason) {
    List<SeasonTeamDTO> teamList =
        actualSeasonTeamService.findAllByActualSeasonPk(actualSeason.getPk());

    return ActualSeasonDetailDTO.fromEntity(actualSeason, teamList);
  }
  //endregion

  //region 실제 시즌 수정
  /**
   * 기존 시즌 수정 (리그, 타이틀, 기간 등)
   */
  @Transactional
  public ActualSeasonDetailDTO updateActualSeason(ActualSeason actualSeason, UpdateActualSeasonRequest request) {

    if (request.getLeaguePk() != null) {
      League league = leagueService.findByPk(request.getLeaguePk());
      if (league == null)
        throw new NotFoundException(ResponseCode.NOT_FOUND_LEAGUE);
      actualSeason.setLeague(league);
    }
    if (request.getTitle() != null) {
      actualSeason.setTitle(request.getTitle());
    }
    if (request.getStartedAt() != null) {
      actualSeason.setStartedAt(LocalDate.from(LocalDateTime.parse(request.getStartedAt())));
    }
    if (request.getFinishedAt() != null) {
      actualSeason.setFinishedAt(LocalDate.from(LocalDateTime.parse(request.getFinishedAt())));
    }
    if (request.getOperatingStatus() != null) {
      try {
        actualSeason.setOperatingStatus(OperatingStatus.valueOf(request.getOperatingStatus()));
      } catch (IllegalArgumentException e) {
        throw new BadRequestException(ResponseCode.INVALID_REQUEST);
      }
    }
    if (request.getDescription() != null) {
      actualSeason.setDescription(request.getDescription());
    }
    if (request.getYear() != null) {
      actualSeason.setYear(request.getYear());
    }
    // 참여 팀 목록 수정
    actualSeasonTeamService.patchSeasonTeams(actualSeason, request.getActualSeasonTeams());
    actualSeasonRepository.save(actualSeason);

    return getActualSeasonDetail(actualSeason);
  }
  //endregion

  //region 실제 시즌 삭제
  /**
   * 주어진 실제 시즌을 비활성화(soft delete) 처리
   */
  @Transactional
  public void deleteActualSeason(ActualSeason actualSeason) {
    actualSeason.setStatus(DataStatus.DEACTIVATED);
    actualSeasonRepository.save(actualSeason);
  }
  //endregion
}
