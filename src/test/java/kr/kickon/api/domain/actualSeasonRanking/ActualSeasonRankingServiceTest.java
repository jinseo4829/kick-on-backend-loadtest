package kr.kickon.api.domain.actualSeasonRanking;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.kickon.api.domain.actualSeasonRanking.dto.GetActualSeasonRankingDTO;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ActualSeasonRankingServiceTest {

    @InjectMocks
    private ActualSeasonRankingService service;

    @Mock
    private ActualSeasonRankingRepository repository;

    @Mock
    private JPAQueryFactory queryFactory;

    @Mock
    private JPAQuery<ActualSeasonRanking> jpaQuery;

    private ActualSeasonRanking ranking;
    private ActualSeason actualSeason;
    private Team team;

    @BeforeEach
    void setUp() {
        actualSeason = ActualSeason.builder()
                .pk(1L)
                .status(DataStatus.ACTIVATED)
                .build();

        team = Team.builder()
                .pk(1L)
                .status(DataStatus.ACTIVATED)
                .build();

        ranking = ActualSeasonRanking.builder()
                .pk(1L)
                .actualSeason(actualSeason)
                .team(team)
                .rankOrder(1)
                .points(10)
                .status(DataStatus.ACTIVATED)
                .build();
    }

    @Test
    @DisplayName("findByPk - 성공")
    void findByPk_success() {
        when(repository.findOne(any(BooleanExpression.class))).thenReturn(Optional.of(ranking));

        ActualSeasonRanking result = service.findByPk(1L);

        assertThat(result).isNotNull();
        assertThat(result.getPk()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findByActualSeasonAndTeam - 성공")
    void findByActualSeasonAndTeam_success() {
        when(repository.findOne(any(BooleanExpression.class))).thenReturn(Optional.of(ranking));

        ActualSeasonRanking result = service.findByActualSeasonAndTeam(actualSeason.getPk(), team.getPk());

        assertThat(result).isNotNull();
        assertThat(result.getTeam().getPk()).isEqualTo(team.getPk());
    }

    @Test
    @DisplayName("findByTeamPk - 성공")
    void findByTeamPk_success() {
        when(repository.findOne(any(BooleanExpression.class))).thenReturn(Optional.of(ranking));

        ActualSeasonRanking result = service.findByTeamPk(team.getPk());

        assertThat(result).isNotNull();
        assertThat(result.getTeam().getPk()).isEqualTo(team.getPk());
    }

    @Test
    @DisplayName("save - repository 호출")
    void save_success() {
        doReturn(ranking).when(repository).save(any(ActualSeasonRanking.class));

        service.save(ranking);

        verify(repository, times(1)).save(ranking);
    }

    @Test
    @DisplayName("findRecentSeasonRankingByLeague - QueryDSL fetch")
    void findRecentSeasonRankingByLeague_success() {
        QActualSeasonRanking qRanking = QActualSeasonRanking.actualSeasonRanking;
        QTeam qTeam = QTeam.team;

        // Mock QueryDSL chain
        JPAQuery queryDTO = mock(JPAQuery.class);
        when(queryFactory.select((Expression<Object>) any())).thenReturn(queryDTO);
        when(queryDTO.from(qRanking)).thenReturn(queryDTO);
        when(queryDTO.join(qRanking.team, qTeam)).thenReturn(queryDTO);
        when(queryDTO.where(any(BooleanExpression.class))).thenReturn(queryDTO);
        when(queryDTO.orderBy(any(OrderSpecifier.class))).thenReturn(queryDTO);
        when(queryDTO.fetch()).thenReturn(List.of(
                new GetActualSeasonRankingDTO(
                        ranking.getRankOrder(),
                        team.getLogoUrl(),
                        team.getNameKr(),
                        ranking.getGameNum(),
                        ranking.getPoints(),
                        ranking.getWonScores()
                )
        ));

        List<GetActualSeasonRankingDTO> result = service.findRecentSeasonRankingByLeague(actualSeason.getPk());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRankOrder()).isEqualTo(1);
    }

    @Test
    @DisplayName("updateActualSeasonRanking - 기존 랭킹 비활성화 후 새로 생성")
    void updateActualSeasonRanking_success() {
        ActualSeasonRanking oldRanking = ranking;
        when(repository.findOne(any(BooleanExpression.class))).thenReturn(Optional.of(oldRanking));
        doReturn(ranking).when(repository).save(any(ActualSeasonRanking.class));

        service.updateActualSeasonRanking(team, actualSeason);

        // 기존 랭킹 비활성화 확인
        assertThat(oldRanking.getStatus()).isEqualTo(DataStatus.DEACTIVATED);

        // save 호출 두 번 (기존 + 새로 생성)
        verify(repository, times(2)).save(any(ActualSeasonRanking.class));
    }

    @Test
    @DisplayName("updateActualSeasonRanking - 기존 랭킹 없을 때 새로 생성")
    void updateActualSeasonRanking_noOldRanking() {
        when(repository.findOne(any(BooleanExpression.class))).thenReturn(Optional.empty());
        doReturn(ranking).when(repository).save(any(ActualSeasonRanking.class));

        service.updateActualSeasonRanking(team, actualSeason);

        // save 호출 한 번 (새로 생성)
        verify(repository, times(1)).save(any(ActualSeasonRanking.class));
    }
}
