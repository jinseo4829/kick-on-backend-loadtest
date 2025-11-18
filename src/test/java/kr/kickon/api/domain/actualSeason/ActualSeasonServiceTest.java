package kr.kickon.api.domain.actualSeason;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.kickon.api.global.common.entities.ActualSeason;
import kr.kickon.api.global.common.entities.QActualSeason;
import kr.kickon.api.global.common.enums.DataStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActualSeasonServiceTest {

    @InjectMocks
    private ActualSeasonService actualSeasonService;

    @Mock
    private ActualSeasonRepository actualSeasonRepository;

    @Mock
    private JPAQueryFactory queryFactory;

    @Mock
    private JPAQuery<ActualSeason> jpaQuery;

    @Test
    @DisplayName("findByPk 성공 - ActualSeason 반환")
    void findByPk_success() {
        // given
        ActualSeason season = ActualSeason.builder()
                .pk(1L)
                .status(DataStatus.ACTIVATED)
                .build();

        when(actualSeasonRepository.findOne(any(BooleanExpression.class)))
                .thenReturn(Optional.of(season));

        // when
        ActualSeason result = actualSeasonService.findByPk(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getPk()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findByPk 실패 - 존재하지 않음")
    void findByPk_notFound() {
        when(actualSeasonRepository.findOne(any(BooleanExpression.class)))
                .thenReturn(Optional.empty());

        ActualSeason result = actualSeasonService.findByPk(999L);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("findByYearAndLeague 성공 - ActualSeason 반환")
    void findByYearAndLeague_success() {
        // given
        ActualSeason season = ActualSeason.builder()
                .year(2024)
                .status(DataStatus.ACTIVATED)
                .build();

        when(actualSeasonRepository.findOne(any(BooleanExpression.class)))
                .thenReturn(Optional.of(season));

        // when
        ActualSeason result = actualSeasonService.findByYearAndLeague(2024, 10L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getYear()).isEqualTo(2024);
    }

    @Test
    @DisplayName("findRecentByLeaguePk 성공 - 최신 시즌 반환")
    void findRecentByLeaguePk_success() {
        // given
        ActualSeason recent = ActualSeason.builder().year(2025).build();
        List<ActualSeason> mockList = List.of(recent);

        when(queryFactory.selectFrom(any(QActualSeason.class))).thenReturn(jpaQuery);
        when(jpaQuery.where(any(BooleanExpression.class))).thenReturn(jpaQuery);
        when(jpaQuery.orderBy(any(OrderSpecifier.class))).thenReturn(jpaQuery);
        when(jpaQuery.fetch()).thenReturn(mockList);

        // when
        ActualSeason result = actualSeasonService.findRecentByLeaguePk(10L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getYear()).isEqualTo(2025);
    }

    @Test
    @DisplayName("findRecentByLeaguePk 실패 - 결과 없음")
    void findRecentByLeaguePk_notFound() {
        when(queryFactory.selectFrom(any(QActualSeason.class))).thenReturn(jpaQuery);
        when(jpaQuery.where(any(BooleanExpression.class))).thenReturn(jpaQuery);
        when(jpaQuery.orderBy(any(OrderSpecifier.class))).thenReturn(jpaQuery);
        when(jpaQuery.fetch()).thenReturn(Collections.emptyList());

        ActualSeason result = actualSeasonService.findRecentByLeaguePk(10L);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("save 성공 - Repository save 호출 확인")
    void save_success() {
        ActualSeason season = ActualSeason.builder().pk(1L).build();
        when(actualSeasonRepository.save(any())).thenReturn(season);

        actualSeasonService.save(season);

        verify(actualSeasonRepository, times(1)).save(season);
    }
}