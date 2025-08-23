package kr.kickon.api.domain.gambleSeasonTeam;

import com.querydsl.core.types.dsl.BooleanExpression;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import kr.kickon.api.domain.team.TeamService;
import kr.kickon.api.domain.team.dto.SeasonTeamDTO;
import kr.kickon.api.global.common.entities.GambleSeason;
import kr.kickon.api.global.common.entities.GambleSeasonTeam;
import kr.kickon.api.global.common.entities.QGambleSeasonTeam;
import kr.kickon.api.global.common.entities.Team;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.common.enums.OperatingStatus;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class GambleSeasonTeamService{

    private final GambleSeasonTeamRepository gambleSeasonTeamRepository;
    private final TeamService teamService;

    public GambleSeasonTeam findByPk(Long pk) {
        BooleanExpression predicate = QGambleSeasonTeam.gambleSeasonTeam.pk.eq(pk)
            .and(QGambleSeasonTeam.gambleSeasonTeam.status.eq(DataStatus.ACTIVATED));
        Optional<GambleSeasonTeam> gambleSeasonTeam = gambleSeasonTeamRepository.findOne(predicate);
        if (gambleSeasonTeam.isEmpty())
            throw new NotFoundException(ResponseCode.NOT_FOUND_GAMBLE_SEASON);
        return gambleSeasonTeam.get();
    }

    public GambleSeasonTeam getRecentOperatingByTeamPk(Long pk) {
        QGambleSeasonTeam gambleSeasonTeam = QGambleSeasonTeam.gambleSeasonTeam;
        BooleanExpression predicate = gambleSeasonTeam.team.pk.eq(pk).and(
            gambleSeasonTeam.status.eq(DataStatus.ACTIVATED)
                .and(gambleSeasonTeam.gambleSeason.operatingStatus.eq(OperatingStatus.PROCEEDING))
                .and(gambleSeasonTeam.gambleSeason.status.eq(DataStatus.ACTIVATED)));
        Optional<GambleSeasonTeam> gambleSeasonTeamData = gambleSeasonTeamRepository.findOne(
            predicate);
        return gambleSeasonTeamData.orElse(null);
    }

//region GambleSeasonTeam 리스트 조회

    /**
     * 주어진 GambleSeason의 PK로 GambleSeasonTeam 리스트를 조회하고, 각 팀 정보를 SeasonTeamDTO로 매핑하여 반환한다.
     */
    public List<SeasonTeamDTO> getgambleSeasonTeamListByGambleSeasonPk(Long seasonPk) {
        return gambleSeasonTeamRepository
            .findAllByGambleSeason_PkAndStatus(seasonPk, DataStatus.ACTIVATED)
            .stream()
            .map(gst -> new SeasonTeamDTO(gst.getTeam()))
            .toList();
    }
//endregion

//region GambleSeasonTeam 수정

    /**
     * 주어진 GambleSeason과 팀 PK 리스트를 기반으로 GambleSeasonTeam을 갱신한다. - 요청에 포함된 팀 중 기존에 없던 팀은 추가 - 기존에 있었지만
     * 요청에 포함되지 않은 팀은 비활성화
     */
    @Transactional
    public void updateSeasonTeams(GambleSeason gambleseason, List<Long> teamPkList) {

        if (teamPkList == null)
            return;

        // 현재 저장돼 있는 팀 PK 들
        List<GambleSeasonTeam> currentEntities =
            gambleSeasonTeamRepository.findAllByGambleSeason_PkAndStatus(
                gambleseason.getPk(), DataStatus.ACTIVATED);

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
            if (team == null) {
                throw new NotFoundException(ResponseCode.NOT_FOUND_TEAM);
            }
            GambleSeasonTeam gambleSeasonTeam = GambleSeasonTeam.builder()
                .gambleSeason(gambleseason)
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
//endregion

    //region GambleSeasonTeam 팀 시즌 재할당

    /**
     * 특정 팀의 최근 GambleSeasonTeam을 찾아 새로운 GambleSeason으로 재할당한다.
     */
    public void updateGambleSeasonTeam(GambleSeason gambleSeason, Team team) {
        GambleSeasonTeam gambleSeasonTeam = getRecentOperatingByTeamPk(team.getPk());
        if (gambleSeasonTeam == null) {
            gambleSeasonTeam = GambleSeasonTeam.builder()
                .gambleSeason(gambleSeason)
                .team(team)
                .build();
        } else {
            gambleSeasonTeam.setGambleSeason(gambleSeason);
        }
        gambleSeasonTeamRepository.save(gambleSeasonTeam);
    }
}

