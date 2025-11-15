package kr.kickon.api.domain.gambleSeasonRanking;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.kickon.api.domain.gambleSeasonPoint.GambleSeasonPointService;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GambleSeasonRankingServiceTest {

    @InjectMocks
    private GambleSeasonRankingService service;

    @Mock
    private GambleSeasonRankingRepository repository;

    @Mock
    private JPAQueryFactory queryFactory;

    @Mock
    private GambleSeasonPointService gambleSeasonPointService;

    @Mock
    private JPAQuery<GambleSeasonRanking> jpaQuery;

    private GambleSeasonRanking ranking;
    private GambleSeason gambleSeason;
    private Team team;

    @BeforeEach
    void setUp() {
        gambleSeason = GambleSeason.builder()
                .pk(1L)
                .status(DataStatus.ACTIVATED)
                .build();

        team = Team.builder()
                .pk(1L)
                .status(DataStatus.ACTIVATED)
                .build();

        ranking = GambleSeasonRanking.builder()
                .pk(1L)
                .gambleSeason(gambleSeason)
                .team(team)
                .rankOrder(1)
                .points(10)
                .gameNum(2)
                .status(DataStatus.ACTIVATED)
                .build();
    }

    @Test
    @DisplayName("findByPk - 성공")
    void findByPk_success() {
        when(repository.findOne(any(BooleanExpression.class))).thenReturn(Optional.of(ranking));

        GambleSeasonRanking result = service.findByPk(1L);

        assertThat(result).isNotNull();
        assertThat(result.getPk()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findByTeamPk - 성공")
    void findByTeamPk_success() {
        when(repository.findOne(any(BooleanExpression.class))).thenReturn(Optional.of(ranking));

        GambleSeasonRanking result = service.findByTeamPk(team.getPk());

        assertThat(result).isNotNull();
        assertThat(result.getTeam().getPk()).isEqualTo(team.getPk());
    }

    @Test
    @DisplayName("save - repository 호출")
    void save_success() {
        doReturn(ranking).when(repository).save(any(GambleSeasonRanking.class));

        service.save(ranking);

        verify(repository, times(1)).save(ranking);
    }

    @Test
    @DisplayName("recalculateRanking - 포인트 기준 재정렬 및 저장")
    void recalculateRanking_success() {
        List<GambleSeasonRanking> rankings = new ArrayList<>(List.of(ranking));

        when(gambleSeasonPointService.findTotalPointByGambleSeasonAndTeam(anyLong(), anyLong()))
                .thenReturn(15);

        when(repository.saveAll(anyList())).thenReturn(rankings);

        service.recalculateRanking(rankings);

        assertThat(rankings.get(0).getPoints()).isEqualTo(15);
        assertThat(rankings.get(0).getRankOrder()).isEqualTo(1);

        verify(repository, times(1)).saveAll(rankings);
    }


    @Test
    @DisplayName("updateGambleSeasonRanking - 기존 랭킹 비활성화 후 새로 생성")
    void updateGambleSeasonRanking_success() {
        when(repository.findOne(any(BooleanExpression.class))).thenReturn(Optional.of(ranking));
        doReturn(ranking).when(repository).save(any(GambleSeasonRanking.class));

        service.updateGambleSeasonRanking(team, gambleSeason);

        // 기존 랭킹 비활성화 확인
        assertThat(ranking.getStatus()).isEqualTo(DataStatus.DEACTIVATED);

        // save 호출 두 번 (기존 + 새로 생성)
        verify(repository, times(2)).save(any(GambleSeasonRanking.class));
    }

    @Test
    @DisplayName("updateGambleSeasonRanking - 기존 랭킹 없을 때 새로 생성")
    void updateGambleSeasonRanking_noOldRanking() {
        when(repository.findOne(any(BooleanExpression.class))).thenReturn(Optional.empty());
        doReturn(ranking).when(repository).save(any(GambleSeasonRanking.class));

        service.updateGambleSeasonRanking(team, gambleSeason);

        // save 호출 한 번 (새로 생성)
        verify(repository, times(1)).save(any(GambleSeasonRanking.class));
    }

    @Test
    @DisplayName("findRecentSeasonRankingByLeague - QueryDSL fetch")
    void findRecentSeasonRankingByLeague_success() {
        QGambleSeasonRanking qRanking = QGambleSeasonRanking.gambleSeasonRanking;
        QTeam qTeam = QTeam.team;

        when(queryFactory.selectFrom(qRanking)).thenReturn(jpaQuery);
        when(jpaQuery.join(qRanking.team, qTeam)).thenReturn(jpaQuery);
        when(jpaQuery.where(any(BooleanExpression.class))).thenReturn(jpaQuery);
        when(jpaQuery.orderBy(any(OrderSpecifier.class))).thenReturn(jpaQuery);
        when(jpaQuery.fetch()).thenReturn(List.of(ranking));

        List<GambleSeasonRanking> result = service.findRecentSeasonRankingByLeague(gambleSeason.getPk());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPk()).isEqualTo(1L);
    }
}
