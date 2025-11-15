package kr.kickon.api.domain.gambleSeason;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.kickon.api.global.common.entities.GambleSeason;
import kr.kickon.api.global.common.entities.QGambleSeason;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.error.exceptions.NotFoundException;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GambleSeasonServiceTest {

    @InjectMocks
    private GambleSeasonService gambleSeasonService;

    @Mock
    private GambleSeasonRepository gambleSeasonRepository;

    @Mock
    private JPAQueryFactory queryFactory;

    @Mock
    private JPAQuery<GambleSeason> jpaQuery;

    @Test
    @DisplayName("findByPk 성공 - GambleSeason 반환")
    void findByPk_success() {
        GambleSeason season = new GambleSeason();
        season.setPk(1L);
        season.setStatus(DataStatus.ACTIVATED);

        when(gambleSeasonRepository.findOne(any(BooleanExpression.class)))
                .thenReturn(Optional.of(season));

        GambleSeason result = gambleSeasonService.findByPk(1L);

        assertThat(result).isNotNull();
        assertThat(result.getPk()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findByPk 실패 - NotFoundException")
    void findByPk_notFound() {
        when(gambleSeasonRepository.findOne(any(BooleanExpression.class)))
                .thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> gambleSeasonService.findByPk(999L));

        assertThat(ex.getResponseCode()).isEqualTo(ResponseCode.NOT_FOUND_GAMBLE_SEASON);
    }

    @Test
    @DisplayName("findRecentOperatingSeasonByLeaguePk 성공 - 최신 운영 시즌 반환")
    void findRecentOperatingSeasonByLeaguePk_success() {
        GambleSeason season = new GambleSeason();
        season.setPk(10L);
        season.setStatus(DataStatus.ACTIVATED);

        // QueryDSL 체인 모킹
        when(queryFactory.selectFrom(any(QGambleSeason.class))).thenReturn(jpaQuery);
        when(jpaQuery.where(any(BooleanExpression.class))).thenReturn(jpaQuery);
        when(jpaQuery.orderBy(any(OrderSpecifier.class))).thenReturn(jpaQuery);
        when(jpaQuery.limit(any(Long.class))).thenReturn(jpaQuery);
        when(jpaQuery.fetch()).thenReturn(List.of(season));

        GambleSeason result = gambleSeasonService.findRecentOperatingSeasonByLeaguePk(1L);

        assertThat(result).isNotNull();
        assertThat(result.getPk()).isEqualTo(10L);
    }

    @Test
    @DisplayName("findRecentOperatingSeasonByLeaguePk 실패 - NotFoundException")
    void findRecentOperatingSeasonByLeaguePk_notFound() {
        when(queryFactory.selectFrom(any(QGambleSeason.class))).thenReturn(jpaQuery);
        when(jpaQuery.where(any(BooleanExpression.class))).thenReturn(jpaQuery);
        when(jpaQuery.orderBy(any(OrderSpecifier.class))).thenReturn(jpaQuery);
        when(jpaQuery.limit(any(Long.class))).thenReturn(jpaQuery);
        when(jpaQuery.fetch()).thenReturn(Collections.emptyList());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> gambleSeasonService.findRecentOperatingSeasonByLeaguePk(1L));

        assertThat(ex.getResponseCode()).isEqualTo(ResponseCode.NOT_FOUND_GAMBLE_SEASON);
    }
}
