package kr.kickon.api.domain.gambleSeasonTeam;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import kr.kickon.api.domain.gambleSeasonTeam.GambleSeasonTeamRepository;
import kr.kickon.api.domain.team.TeamService;
import kr.kickon.api.domain.team.dto.SeasonTeamDTO;
import kr.kickon.api.domain.team.dto.TeamDTO;
import kr.kickon.api.global.common.BaseService;
import kr.kickon.api.global.common.entities.GambleSeason;
import kr.kickon.api.global.common.entities.GambleSeasonTeam;
import kr.kickon.api.global.common.entities.QGambleSeasonTeam;
import kr.kickon.api.global.common.entities.Team;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.common.enums.OperatingStatus;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import kr.kickon.api.global.util.UUIDGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class GambleSeasonTeamService implements BaseService<GambleSeasonTeam> {
    private final GambleSeasonTeamRepository gambleSeasonTeamRepository;
    private final JPAQueryFactory queryFactory;
    private final UUIDGenerator uuidGenerator;
    private final TeamService teamService;

    @Override
    public GambleSeasonTeam findById(String uuid) {
        BooleanExpression predicate = QGambleSeasonTeam.gambleSeasonTeam.id.eq(uuid).and(QGambleSeasonTeam.gambleSeasonTeam.status.eq(DataStatus.ACTIVATED));
        Optional<GambleSeasonTeam> gambleSeasonTeam = gambleSeasonTeamRepository.findOne(predicate);
        return gambleSeasonTeam.orElse(null);
    }

    @Override
    public GambleSeasonTeam findByPk(Long pk) {
        BooleanExpression predicate = QGambleSeasonTeam.gambleSeasonTeam.pk.eq(pk).and(QGambleSeasonTeam.gambleSeasonTeam.status.eq(DataStatus.ACTIVATED));
        Optional<GambleSeasonTeam> gambleSeasonTeam = gambleSeasonTeamRepository.findOne(predicate);
        if(gambleSeasonTeam.isEmpty()) throw new NotFoundException(ResponseCode.NOT_FOUND_GAMBLE_SEASON);
        return gambleSeasonTeam.get();
    }

    public GambleSeasonTeam findRecentOperatingByTeamPk(Long pk) {
        QGambleSeasonTeam gambleSeasonTeam = QGambleSeasonTeam.gambleSeasonTeam;
        BooleanExpression predicate = gambleSeasonTeam.team.pk.eq(pk).and(gambleSeasonTeam.status.eq(DataStatus.ACTIVATED).and(gambleSeasonTeam.gambleSeason.operatingStatus.eq(OperatingStatus.PROCEEDING)).and(gambleSeasonTeam.gambleSeason.status.eq(DataStatus.ACTIVATED)));
        Optional<GambleSeasonTeam> gambleSeasonTeamData = gambleSeasonTeamRepository.findOne(predicate);
        return gambleSeasonTeamData.orElse(null);
    }

    public List<SeasonTeamDTO> findAllByGambleSeasonPk(Long seasonPk) {
        return gambleSeasonTeamRepository
            .findAllByGambleSeason_PkAndStatus(seasonPk, DataStatus.ACTIVATED)
            .stream()
            .map(gst -> new SeasonTeamDTO(gst.getTeam()))
            .toList();
    }

    @Transactional
    public void patchSeasonTeams(GambleSeason season, List<Long> teamPkList) {

        if (teamPkList == null) return;

        // 현재 저장돼 있는 팀 PK 들
        List<GambleSeasonTeam> currentEntities =
            gambleSeasonTeamRepository.findAllByGambleSeason_PkAndStatus(
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
            GambleSeasonTeam gambleSeasonTeam = GambleSeasonTeam.builder()
                .id(UUID.randomUUID().toString())
                .gambleSeason(season)
                .team(team)
                .status(DataStatus.ACTIVATED)
                .build();
            gambleSeasonTeamRepository.save(gambleSeasonTeam);
        }

        // 삭제해야 할 팀 = 현재목록 − 요청목록
        Set<Long> removeSet = new HashSet<>(currentPkSet);
        removeSet.removeAll(requestedPkSet);

        currentEntities.stream()
            .filter(gst -> removeSet.contains(gst.getTeam().getPk()))
            .forEach(gst -> {
                gst.setStatus(DataStatus.DEACTIVATED);
            });
    }
}

