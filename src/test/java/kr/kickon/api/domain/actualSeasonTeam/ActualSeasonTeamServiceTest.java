package kr.kickon.api.domain.actualSeasonTeam;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.kickon.api.domain.team.TeamService;
import kr.kickon.api.global.common.entities.ActualSeason;
import kr.kickon.api.global.common.entities.ActualSeasonTeam;
import kr.kickon.api.global.common.entities.QActualSeasonTeam;
import kr.kickon.api.global.common.entities.Team;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.common.enums.OperatingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ActualSeasonTeamServiceTest {

    @InjectMocks
    private ActualSeasonTeamService service;

    @Mock
    private ActualSeasonTeamRepository repository;

    @Mock
    private TeamService teamService;

    @Mock
    private JPAQueryFactory queryFactory;

    @Mock
    private JPAQuery<ActualSeasonTeam> jpaQuery;

    private ActualSeason actualSeason;
    private Team team;
    private ActualSeasonTeam actualSeasonTeam;

    @BeforeEach
    void setUp() {
        actualSeason = ActualSeason.builder()
                .pk(1L)
                .operatingStatus(OperatingStatus.PROCEEDING)
                .status(DataStatus.ACTIVATED)
                .build();

        team = Team.builder()
                .pk(1L)
                .status(DataStatus.ACTIVATED)
                .build();

        actualSeasonTeam = ActualSeasonTeam.builder()
                .pk(1L)
                .actualSeason(actualSeason)
                .team(team)
                .status(DataStatus.ACTIVATED)
                .build();
    }

    @Test
    @DisplayName("findLatestByTeam - 성공")
    void findLatestByTeam_success() {
        when(queryFactory.selectFrom(any(QActualSeasonTeam.class))).thenReturn(jpaQuery);
        when(jpaQuery.where(any(Predicate[].class))).thenReturn(jpaQuery);
        when(jpaQuery.orderBy(any(OrderSpecifier.class))).thenReturn(jpaQuery);
        when(jpaQuery.fetchFirst()).thenReturn(actualSeasonTeam);

        ActualSeasonTeam result = service.findLatestByTeam(1L);

        assertThat(result).isNotNull();
        assertThat(result.getPk()).isEqualTo(1L);
    }


    @Test
    @DisplayName("updateActualSeasonTeam - 새로 생성")
    void updateActualSeasonTeam_createNew() {
        // findLatestByTeam null 반환
        ActualSeasonTeamService spyService = Mockito.spy(service);
        doReturn(null).when(spyService).findLatestByTeam(team.getPk());

        when(repository.save(any())).thenReturn(actualSeasonTeam);

        spyService.updateActualSeasonTeam(actualSeason, team);

        verify(repository, times(1)).save(any(ActualSeasonTeam.class));
    }

    @Test
    @DisplayName("updateActualSeasonTeam - 기존 존재")
    void updateActualSeasonTeam_updateExisting() {
        ActualSeasonTeamService spyService = Mockito.spy(service);
        doReturn(actualSeasonTeam).when(spyService).findLatestByTeam(team.getPk());

        spyService.updateActualSeasonTeam(actualSeason, team);

        assertThat(actualSeasonTeam.getActualSeason()).isEqualTo(actualSeason);
        verify(repository, times(1)).save(actualSeasonTeam);
    }

    @Test
    @DisplayName("findByActualSeason - 성공")
    void findByActualSeason_success() {
        when(queryFactory.selectFrom(any(QActualSeasonTeam.class))).thenReturn(jpaQuery);
        when(jpaQuery.where(any(Predicate[].class))).thenReturn(jpaQuery);
        when(jpaQuery.orderBy(any(OrderSpecifier.class))).thenReturn(jpaQuery);
        when(jpaQuery.fetch()).thenReturn(List.of(actualSeasonTeam));

        List<ActualSeasonTeam> result = service.findByActualSeason(actualSeason.getPk(), null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPk()).isEqualTo(1L);
    }

    @Test
    @DisplayName("deleteActualSeasonTeams - 성공")
    void deleteActualSeasonTeams_success() {
        when(queryFactory.selectFrom(any(QActualSeasonTeam.class))).thenReturn(jpaQuery);
        when(jpaQuery.where(any(Predicate[].class))).thenReturn(jpaQuery);
        when(jpaQuery.orderBy(any(OrderSpecifier.class))).thenReturn(jpaQuery);
        when(jpaQuery.fetch()).thenReturn(List.of(actualSeasonTeam));

        service.deleteActualSeasonTeams(actualSeason);

        assertThat(actualSeasonTeam.getStatus()).isEqualTo(DataStatus.DEACTIVATED);
    }
}
