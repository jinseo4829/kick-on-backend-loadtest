package kr.kickon.api.domain.gambleSeasonPoint;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.kickon.api.global.common.entities.*;
import kr.kickon.api.global.common.enums.DataStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GambleSeasonPointServiceTest {

    @InjectMocks
    private GambleSeasonPointService service;

    @Mock
    private GambleSeasonPointRepository repository;

    @Mock
    private JPAQueryFactory queryFactory;

    @Mock
    private JPAQuery<Integer> jpaQuery;

    private GambleSeasonPoint gambleSeasonPoint;

    @BeforeEach
    void setUp() {
        gambleSeasonPoint = GambleSeasonPoint.builder()
                .pk(1L)
                .averagePoints(50)
                .status(DataStatus.ACTIVATED)
                .build();
    }

    // -----------------------------------------------------
    // findByPk
    // -----------------------------------------------------
    @Test
    @DisplayName("findByPk - 존재하면 반환")
    void findByPk_success() {
        when(repository.findOne(any(BooleanExpression.class)))
                .thenReturn(Optional.of(gambleSeasonPoint));

        GambleSeasonPoint result = service.findByPk(1L);

        assertThat(result).isNotNull();
        assertThat(result.getPk()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findByPk - 없으면 null")
    void findByPk_notFound() {
        when(repository.findOne(any(BooleanExpression.class)))
                .thenReturn(Optional.empty());

        GambleSeasonPoint result = service.findByPk(1L);
        assertThat(result).isNull();
    }

    // -----------------------------------------------------
    // findTotalPointByGambleSeasonAndTeam
    // -----------------------------------------------------
    @Test
    @DisplayName("findTotalPointByGambleSeasonAndTeam - 값 존재")
    void findTotalPoint_exist() {
        when(queryFactory.select(any(Expression.class))).thenReturn(jpaQuery);
        when(jpaQuery.from(any(EntityPath.class))).thenReturn(jpaQuery);
        when(jpaQuery.where(any(Predicate.class))).thenReturn(jpaQuery);
        when(jpaQuery.fetchOne()).thenReturn(100);

        Integer totalPoint = service.findTotalPointByGambleSeasonAndTeam(1L, 1L);

        assertThat(totalPoint).isEqualTo(100);
    }

    @Test
    @DisplayName("findTotalPointByGambleSeasonAndTeam - 값 없으면 0")
    void findTotalPoint_notExist() {
        when(queryFactory.select(any(Expression.class))).thenReturn(jpaQuery);
        when(jpaQuery.from(any(EntityPath.class))).thenReturn(jpaQuery);
        when(jpaQuery.where(any(Predicate.class))).thenReturn(jpaQuery);
        when(jpaQuery.fetchOne()).thenReturn(null);

        Integer totalPoint = service.findTotalPointByGambleSeasonAndTeam(1L, 1L);

        assertThat(totalPoint).isEqualTo(0);
    }

    // -----------------------------------------------------
    // findByTeamPkAndGamePk
    // -----------------------------------------------------
    @Test
    @DisplayName("findByTeamPkAndGamePk - 존재")
    void findByTeamPkAndGamePk_exist() {
        when(repository.findOne(any(BooleanExpression.class)))
                .thenReturn(Optional.of(gambleSeasonPoint));

        GambleSeasonPoint result = service.findByTeamPkAndGamePk(1L, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getPk()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findByTeamPkAndGamePk - 없음")
    void findByTeamPkAndGamePk_notExist() {
        when(repository.findOne(any(BooleanExpression.class)))
                .thenReturn(Optional.empty());

        GambleSeasonPoint result = service.findByTeamPkAndGamePk(1L, 1L);
        assertThat(result).isNull();
    }

    // -----------------------------------------------------
    // save
    // -----------------------------------------------------
    @Test
    @DisplayName("save - 호출 확인")
    void save_test() {
        service.save(gambleSeasonPoint);

        verify(repository, times(1)).save(gambleSeasonPoint);
    }
}
