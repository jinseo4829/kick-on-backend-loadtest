package kr.kickon.api.domain.team;

import static org.junit.jupiter.api.Assertions.*;

import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.kickon.api.global.common.entities.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private JPAQueryFactory queryFactory;

    @InjectMocks
    private TeamService teamService;

    @Mock
    private QTeam qTeam;

    @Mock
    private QActualSeasonTeam qActualSeasonTeam;

    @Mock
    private QActualSeason qActualSeason;

    @Mock
    private QLeague qLeague;

    @Test
    @DisplayName("findByPk - 존재하는 팀 조회")
    void findByPk_success() {
        Team team = new Team();
        team.setPk(1L);
        team.setNameKr("Team A");

        when(teamRepository.findOne(any(Predicate.class))).thenReturn(Optional.of(team));

        Team result = teamService.findByPk(1L);

        assertNotNull(result);
        assertEquals("Team A", result.getNameKr());
    }

    @Test
    @DisplayName("findByPk - 존재하지 않는 팀 조회")
    void findByPk_notFound() {
        when(teamRepository.findOne(any(Predicate.class))).thenReturn(Optional.empty());

        Team result = teamService.findByPk(999L);

        assertNull(result);
    }

    @Test
    @DisplayName("findAll - 활성화된 팀 목록 조회")
    void findAll_success() {
        Team team1 = new Team();
        team1.setPk(1L);
        team1.setNameKr("Team A");

        Team team2 = new Team();
        team2.setPk(2L);
        team2.setNameKr("Team B");

        when(queryFactory.selectFrom(QTeam.team)).thenReturn(mock(JPAQuery.class, invocation -> {
            if ("where".equals(invocation.getMethod().getName())) {
                return mock(JPAQuery.class, invocation2 -> {
                    if ("fetch".equals(invocation2.getMethod().getName())) {
                        return List.of(team1, team2);
                    }
                    return null;
                });
            }
            return null;
        }));

        List<Team> results = teamService.findAll();
        assertEquals(2, results.size());
    }

    @Test
    @DisplayName("findByApiId - 활성화된 팀 조회")
    void findByApiId_success() {
        Team team = new Team();
        team.setPk(1L);
        team.setApiId(100L);
        team.setNameKr("Team A");

        when(teamRepository.findOne(any(Predicate.class))).thenReturn(Optional.of(team));

        Team result = teamService.findByApiId(100L);

        assertNotNull(result);
        assertEquals(100L, result.getApiId());
    }

    @Test
    @DisplayName("findByKeyword - 키워드로 팀 검색")
    void findByKeyword_success() {
        Team team = new Team();
        team.setPk(1L);
        team.setNameKr("Team A");
        team.setNameEn("TA");

        // JPAQuery 모킹
        JPAQuery<Team> jpaQueryMock = mock(JPAQuery.class);
        when(queryFactory.selectFrom(QTeam.team)).thenReturn(jpaQueryMock);
        when(jpaQueryMock.where(any(Predicate.class))).thenReturn(jpaQueryMock);
        when(jpaQueryMock.fetch()).thenReturn(List.of(team));

        List<Team> results = teamService.findByKeyword("Team");

        assertEquals(1, results.size());
        assertEquals("Team A", results.get(0).getNameKr());
    }

    @Test
    @DisplayName("save - 팀 저장")
    void save_success() {
        Team team = new Team();
        team.setNameKr("Team A");

        when(teamRepository.save(any(Team.class))).thenReturn(team);

        Team result = teamService.save(team);

        assertNotNull(result);
        assertEquals("Team A", result.getNameKr());
    }
}
