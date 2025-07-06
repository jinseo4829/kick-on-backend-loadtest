package kr.kickon.api.domain.actualSeasonTeam;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import kr.kickon.api.domain.team.TeamService;
import kr.kickon.api.domain.team.dto.SeasonTeamDTO;
import kr.kickon.api.global.common.BaseService;
import kr.kickon.api.global.common.entities.ActualSeason;
import kr.kickon.api.global.common.entities.ActualSeasonTeam;
import kr.kickon.api.global.common.entities.QActualSeasonTeam;
import kr.kickon.api.global.common.entities.Team;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.common.enums.OperatingStatus;
import kr.kickon.api.global.util.UUIDGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ActualSeasonTeamService implements BaseService<ActualSeasonTeam> {
    private final ActualSeasonTeamRepository actualSeasonTeamRepository;
    private final JPAQueryFactory queryFactory;
    private final UUIDGenerator uuidGenerator;
    private final TeamService teamService;
    @Override
    public ActualSeasonTeam findById(String uuid) {
        BooleanExpression predicate = QActualSeasonTeam.actualSeasonTeam.id.eq(uuid).and(QActualSeasonTeam.actualSeasonTeam.status.eq(DataStatus.ACTIVATED));
        Optional<ActualSeasonTeam> actualSeasonTeam = actualSeasonTeamRepository.findOne(predicate);
        return actualSeasonTeam.orElse(null);
    }

    @Override
    public ActualSeasonTeam findByPk(Long pk) {
        BooleanExpression predicate = QActualSeasonTeam.actualSeasonTeam.pk.eq(pk).and(QActualSeasonTeam.actualSeasonTeam.status.eq(DataStatus.ACTIVATED));
        Optional<ActualSeasonTeam> actualSeasonTeam = actualSeasonTeamRepository.findOne(predicate);
        return actualSeasonTeam.orElse(null);
    }

    public ActualSeasonTeam findByActualSeasonTeam(ActualSeason actualSeason,Long teamPk) {
        BooleanExpression predicate = QActualSeasonTeam.actualSeasonTeam.actualSeason.pk.eq(actualSeason.getPk()).and(QActualSeasonTeam.actualSeasonTeam.status.eq(DataStatus.ACTIVATED).and(QActualSeasonTeam.actualSeasonTeam.team.pk.eq(teamPk)));
        Optional<ActualSeasonTeam> actualSeasonTeam = actualSeasonTeamRepository.findOne(predicate);
        return actualSeasonTeam.orElse(null);
    }

    public List<ActualSeasonTeam> findByActualSeason(Long actualSeasonPk, String keyword){
        QActualSeasonTeam actualSeasonTeam = QActualSeasonTeam.actualSeasonTeam;

        JPAQuery<ActualSeasonTeam> query = queryFactory
                .selectFrom(actualSeasonTeam)
                .where(
                        actualSeasonTeam.actualSeason.pk.eq(actualSeasonPk),
                        actualSeasonTeam.status.eq(DataStatus.ACTIVATED),
                        actualSeasonTeam.team.status.eq(DataStatus.ACTIVATED),
                        actualSeasonTeam.actualSeason.operatingStatus.eq(OperatingStatus.PROCEEDING)
                )
                .orderBy(actualSeasonTeam.createdAt.desc());
        if(keyword!=null) query.where(actualSeasonTeam.team.nameKr.containsIgnoreCase(keyword)
                .or(actualSeasonTeam.team.nameEn.containsIgnoreCase(keyword)));
//                .fetch();
        return query.fetch();
    }

    public ActualSeasonTeam findLatestByTeam(Long teamPk) {
        QActualSeasonTeam actualSeasonTeam = QActualSeasonTeam.actualSeasonTeam;

        return queryFactory
                .selectFrom(actualSeasonTeam)
                .where(
                        actualSeasonTeam.team.pk.eq(teamPk),
                        actualSeasonTeam.status.eq(DataStatus.ACTIVATED),
                        actualSeasonTeam.team.status.eq(DataStatus.ACTIVATED),
                        actualSeasonTeam.actualSeason.operatingStatus.eq(OperatingStatus.PROCEEDING),
                        actualSeasonTeam.actualSeason.status.eq(DataStatus.ACTIVATED),
                        actualSeasonTeam.actualSeason.league.status.eq(DataStatus.ACTIVATED)

                )
                .orderBy(actualSeasonTeam.createdAt.desc()) // 최신 순 정렬
                .fetchFirst(); // 가장 최신 1개만 조회
    }

    public ActualSeasonTeam save(ActualSeasonTeam actualSeasonTeam) {
        return actualSeasonTeamRepository.save(actualSeasonTeam);
    }

    // ActualSeasonTeamService
    public boolean existsByActualSeasonAndTeamPk(ActualSeason actualSeason, Long teamPk) {
        return actualSeasonTeamRepository.existsByActualSeasonAndTeam_Pk(actualSeason, teamPk);
    }

    public List<SeasonTeamDTO> findAllByActualSeasonPk(Long seasonPk) {
        return actualSeasonTeamRepository
            .findAllByActualSeason_PkAndStatus(seasonPk, DataStatus.ACTIVATED)
            .stream()
            .map(gst -> new SeasonTeamDTO(gst.getTeam()))
            .toList();
    }

    @Transactional
    public void patchSeasonTeams(ActualSeason season, List<Long> teamPkList) {

        if (teamPkList == null) return;

        // 현재 저장돼 있는 팀 PK 들
        List<ActualSeasonTeam> currentEntities =
            actualSeasonTeamRepository.findAllByActualSeason_PkAndStatus(
                season.getPk(), DataStatus.ACTIVATED);

        Set<Long> currentPkSet = currentEntities.stream()
            .map(gst -> gst.getTeam().getPk())
            .collect(Collectors.toSet());

        // 요청으로 들어온 팀 PK 들
        Set<Long> requestedPkSet = new HashSet<>(teamPkList);

        // 추가해야 할 팀 = 요청목록 − 현재목록
        Set<Long> addSet = new HashSet<>(requestedPkSet);
        addSet.removeAll(currentPkSet);

        for (Long teamPk : addSet) {
            Team team = teamService.findByPk(teamPk);
            ActualSeasonTeam actualSeasonTeam = ActualSeasonTeam.builder()
                .id(UUID.randomUUID().toString())
                .actualSeason(season)
                .team(team)
                .status(DataStatus.ACTIVATED)
                .build();
            actualSeasonTeamRepository.save(actualSeasonTeam);
        }

        // 삭제해야 할 팀 = 현재목록 − 요청목록
        Set<Long> removeSet = new HashSet<>(currentPkSet);
        removeSet.removeAll(requestedPkSet);

        currentEntities.stream()
            .filter(ast -> removeSet.contains(ast.getTeam().getPk()))
            .forEach(ast -> {
                ast.setStatus(DataStatus.DEACTIVATED);
            });
    }

    public void patchActualSeasonTeam(ActualSeason actualSeason,Long teamPk) {
        ActualSeasonTeam actualSeasonTeam = findLatestByTeam(teamPk);
        actualSeasonTeam.setActualSeason(actualSeason);
    }
}
