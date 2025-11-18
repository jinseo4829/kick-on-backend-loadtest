package kr.kickon.api.domain.league;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.kickon.api.global.common.entities.*;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.common.enums.LeagueType;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeagueServiceTest {

    @InjectMocks
    private LeagueService leagueService;

    @Mock
    private LeagueRepository leagueRepository;

    @Mock
    private JPAQueryFactory queryFactory;

    @Mock
    private JPAQuery<League> jpaQuery;

    private League league;

    @BeforeEach
    void setUp() {
        league = League.builder()
                .pk(1L)
                .apiId(100L)
                .type(LeagueType.League)
                .nameEn("Premier League")
                .nameKr("프리미어 리그")
                .status(DataStatus.ACTIVATED)
                .build();
    }

    @Test
    @DisplayName("findByPk - 성공")
    void findByPk_success() {
        when(leagueRepository.findOne(any(BooleanExpression.class)))
                .thenReturn(Optional.of(league));

        League result = leagueService.findByPk(1L);

        assertThat(result).isNotNull();
        assertThat(result.getPk()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findByApiId - 성공")
    void findByApiId_success() {
        when(leagueRepository.findOne(any(BooleanExpression.class)))
                .thenReturn(Optional.of(league));

        League result = leagueService.findByApiId(100L);

        assertThat(result).isNotNull();
        assertThat(result.getApiId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("findByApiId - 실패, NotFoundException 발생")
    void findByApiId_notFound() {
        when(leagueRepository.findOne(any(BooleanExpression.class)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> leagueService.findByApiId(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("해당 리그를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("save - Repository 호출")
    void save_success() {
        leagueService.save(league);
        verify(leagueRepository, times(1)).save(league);
    }

    @Test
    @DisplayName("findAllLeagues - 성공")
    void findAllLeagues_success() {
        when(queryFactory.selectFrom(any(QLeague.class))).thenReturn(jpaQuery);
        when(jpaQuery.where(any(Predicate[].class))).thenReturn(jpaQuery);
        when(jpaQuery.fetch()).thenReturn(List.of(league));

        List<League> result = leagueService.findAllLeagues();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPk()).isEqualTo(1L);
    }


    @Test
    @DisplayName("findAll - 성공")
    void findAll_success() {
        when(queryFactory.selectFrom(any(QLeague.class))).thenReturn(jpaQuery);
        doReturn(jpaQuery).when(jpaQuery).where(any(BooleanExpression.class));
        when(jpaQuery.fetch()).thenReturn(List.of(league));

        List<League> result = leagueService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPk()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findAllBySeason - 성공")
    void findAllBySeason_success() {
        when(queryFactory.selectFrom(any(QLeague.class))).thenReturn(jpaQuery);

        when(jpaQuery.join(any(QActualSeason.class))).thenReturn(jpaQuery);
        when(jpaQuery.on(any(BooleanExpression.class))).thenReturn(jpaQuery);
        doReturn(jpaQuery).when(jpaQuery).where(any(BooleanExpression.class));

        when(jpaQuery.fetch()).thenReturn(List.of(league));

        List<League> result = leagueService.findAllBySeason(2025);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPk()).isEqualTo(1L);
    }
}