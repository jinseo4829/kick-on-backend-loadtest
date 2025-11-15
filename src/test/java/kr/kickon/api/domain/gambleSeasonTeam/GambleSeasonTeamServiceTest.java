package kr.kickon.api.domain.gambleSeasonTeam;

import com.querydsl.core.types.dsl.BooleanExpression;
import kr.kickon.api.domain.team.TeamService;
import kr.kickon.api.domain.team.dto.SeasonTeamDTO;
import kr.kickon.api.global.common.entities.*;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.common.enums.OperatingStatus;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GambleSeasonTeamServiceTest {

    @InjectMocks
    private GambleSeasonTeamService service;

    @Mock
    private GambleSeasonTeamRepository repository;

    @Mock
    private TeamService teamService;

    private GambleSeason gambleSeason;
    private Team team;
    private GambleSeasonTeam gambleSeasonTeam;

    @BeforeEach
    void setUp() {
        gambleSeason = GambleSeason.builder()
                .pk(1L)
                .operatingStatus(OperatingStatus.PROCEEDING)
                .status(DataStatus.ACTIVATED)
                .build();

        team = Team.builder()
                .pk(1L)
                .status(DataStatus.ACTIVATED)
                .build();

        gambleSeasonTeam = GambleSeasonTeam.builder()
                .pk(1L)
                .gambleSeason(gambleSeason)
                .team(team)
                .status(DataStatus.ACTIVATED)
                .build();
    }

    // -----------------------------------------------------
    // findByPk
    // -----------------------------------------------------
    @Test
    @DisplayName("findByPk - 성공")
    void findByPk_success() {
        when(repository.findOne(any(BooleanExpression.class)))
                .thenReturn(Optional.of(gambleSeasonTeam));

        GambleSeasonTeam result = service.findByPk(1L);

        assertThat(result).isNotNull();
        assertThat(result.getPk()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findByPk - 실패(NotFoundException)")
    void findByPk_notFound() {
        when(repository.findOne(any(BooleanExpression.class)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByPk(1L))
                .isInstanceOf(NotFoundException.class);
    }

    // -----------------------------------------------------
    // getRecentOperatingByTeamPk
    // -----------------------------------------------------
    @Test
    @DisplayName("getRecentOperatingByTeamPk - 존재하는 경우 반환")
    void getRecentOperatingByTeamPk_exist() {
        when(repository.findOne(any(BooleanExpression.class)))
                .thenReturn(Optional.of(gambleSeasonTeam));

        GambleSeasonTeam result = service.getRecentOperatingByTeamPk(1L);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("getRecentOperatingByTeamPk - 없으면 null 반환")
    void getRecentOperatingByTeamPk_notExist() {
        when(repository.findOne(any(BooleanExpression.class)))
                .thenReturn(Optional.empty());

        GambleSeasonTeam result = service.getRecentOperatingByTeamPk(1L);

        assertThat(result).isNull();
    }

    // -----------------------------------------------------
    // getgambleSeasonTeamListByGambleSeasonPk
    // -----------------------------------------------------
    @Test
    @DisplayName("getgambleSeasonTeamListByGambleSeasonPk - 성공")
    void getgambleSeasonTeamListByGambleSeasonPk_success() {
        when(repository.findAllByGambleSeason_PkAndStatus(1L, DataStatus.ACTIVATED))
                .thenReturn(List.of(gambleSeasonTeam));

        List<SeasonTeamDTO> result = service.getgambleSeasonTeamListByGambleSeasonPk(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPk()).isEqualTo(team.getPk());
    }

    // -----------------------------------------------------
    // updateSeasonTeams
    // -----------------------------------------------------
    @Test
    @DisplayName("updateSeasonTeams - 새 팀 추가")
    void updateSeasonTeams_addNewTeams() {
        // 기존 등록팀: 없음
        when(repository.findAllByGambleSeason_PkAndStatus(1L, DataStatus.ACTIVATED))
                .thenReturn(List.of());

        // 새로 들어온 팀
        when(teamService.findByPk(1L)).thenReturn(team);
        when(repository.save(any(GambleSeasonTeam.class))).thenReturn(gambleSeasonTeam);

        service.updateSeasonTeams(gambleSeason, List.of(1L));

        verify(repository, times(1)).save(any(GambleSeasonTeam.class));
    }

    @Test
    @DisplayName("updateSeasonTeams - 기존 팀 삭제(비활성화)")
    void updateSeasonTeams_disableOldTeams() {
        // 기존에 팀 1번이 등록되어 있음
        when(repository.findAllByGambleSeason_PkAndStatus(1L, DataStatus.ACTIVATED))
                .thenReturn(List.of(gambleSeasonTeam));

        // 요청에는 팀이 없음 → 기존 팀 DEACTIVATED 처리
        service.updateSeasonTeams(gambleSeason, List.of());

        assertThat(gambleSeasonTeam.getStatus()).isEqualTo(DataStatus.DEACTIVATED);
    }

    // -----------------------------------------------------
    // updateGambleSeasonTeam
    // -----------------------------------------------------
    @Test
    @DisplayName("updateGambleSeasonTeam - 기존 없음 → 새로 생성")
    void updateGambleSeasonTeam_newCreate() {
        when(repository.findOne(any(BooleanExpression.class)))
                .thenReturn(Optional.empty());

        when(repository.save(any(GambleSeasonTeam.class)))
                .thenReturn(gambleSeasonTeam);

        service.updateGambleSeasonTeam(gambleSeason, team);

        verify(repository, times(1)).save(any(GambleSeasonTeam.class));
    }

    @Test
    @DisplayName("updateGambleSeasonTeam - 기존 존재 → Season만 교체")
    void updateGambleSeasonTeam_updateExisting() {
        when(repository.findOne(any(BooleanExpression.class)))
                .thenReturn(Optional.of(gambleSeasonTeam));

        service.updateGambleSeasonTeam(gambleSeason, team);

        assertThat(gambleSeasonTeam.getGambleSeason()).isEqualTo(gambleSeason);
        verify(repository, times(1)).save(gambleSeasonTeam);
    }
}
