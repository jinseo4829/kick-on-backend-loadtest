package kr.kickon.api.admin.gambleSeason;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.EntityPathBase;
import kr.kickon.api.admin.gambleSeason.dto.GambleSeasonDetailDTO;
import kr.kickon.api.admin.gambleSeason.dto.SeasonListDTO;
import kr.kickon.api.admin.gambleSeason.request.CreateGambleSeasonRequest;
import kr.kickon.api.admin.gambleSeason.request.GambleSeasonFilterRequest;
import kr.kickon.api.admin.gambleSeason.request.UpdateGambleSeasonRequest;
import kr.kickon.api.domain.actualSeason.ActualSeasonService;
import kr.kickon.api.domain.gambleSeason.GambleSeasonRepository;
import kr.kickon.api.domain.gambleSeasonRanking.GambleSeasonRankingService;
import kr.kickon.api.domain.gambleSeasonRanking.dto.GetGambleSeasonRankingDTO;
import kr.kickon.api.domain.gambleSeasonTeam.GambleSeasonTeamService;
import kr.kickon.api.domain.league.LeagueService;
import kr.kickon.api.domain.team.dto.SeasonTeamDTO;
import kr.kickon.api.global.common.entities.GambleSeason;
import kr.kickon.api.global.common.entities.League;
import kr.kickon.api.global.common.entities.QGambleSeason;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.kickon.api.global.common.entities.ActualSeason;
import kr.kickon.api.global.common.enums.DataStatus;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminGambleSeasonServiceTest {

    @InjectMocks
    private AdminGambleSeasonService adminGambleSeasonService;

    @Mock
    private JPAQueryFactory queryFactory;

    @Mock
    private GambleSeasonRepository gambleSeasonRepository;

    @Mock
    private GambleSeasonTeamService gambleSeasonTeamService;

    @Mock
    private GambleSeasonRankingService gambleSeasonRankingService;

    @Mock
    private LeagueService leagueService;

    @Mock
    private ActualSeasonService actualSeasonService;

    @Mock
    private JPAQuery<GambleSeason> jpaQuery;

    //region findByPk
    @Test
    @DisplayName("findByPk 성공 - GambleSeason 반환")
    void findByPk_success() {
        // given
        GambleSeason season = GambleSeason.builder()
                .pk(1L)
                .status(DataStatus.ACTIVATED)
                .build();

        given(gambleSeasonRepository.findByPkAndStatus(anyLong(), any()))
                .willReturn(Optional.of(season));

        // when
        GambleSeason result = adminGambleSeasonService.findByPk(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getPk()).isEqualTo(1L);
        verify(gambleSeasonRepository).findByPkAndStatus(1L, DataStatus.ACTIVATED);
    }

    @Test
    @DisplayName("findByPk 실패 - 존재하지 않음")
    void findByPk_notFound() {
        // given
        given(gambleSeasonRepository.findByPkAndStatus(anyLong(), any()))
                .willReturn(Optional.empty());

        // when
        GambleSeason result = adminGambleSeasonService.findByPk(99L);

        // then
        assertThat(result).isNull();
        verify(gambleSeasonRepository).findByPkAndStatus(99L, DataStatus.ACTIVATED);
    }
    //endregion

    //region getGambleSeasonListByFilter
    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("getGambleSeasonListByFilter 성공 - 필터 조회")
    void getGambleSeasonListByFilter_success() {
        // given
        GambleSeasonFilterRequest request = new GambleSeasonFilterRequest();
        Pageable pageable = PageRequest.of(0, 10);
        List<GambleSeason> content = List.of(GambleSeason.builder().title("테스트 시즌").build());

        // ✅ count 쿼리 mock
        JPAQuery<Long> countQuery = mock(JPAQuery.class);
        given(queryFactory.select((Expression<Object>) any())).willReturn((JPAQuery) countQuery);
        given(countQuery.from(any(EntityPathBase.class))).willReturn(countQuery);
        given(countQuery.leftJoin(any(EntityPathBase.class), any(EntityPathBase.class))).willReturn(countQuery);
        given(countQuery.where(any(BooleanBuilder.class))).willReturn(countQuery);
        given(countQuery.fetchOne()).willReturn(1L);

        // ✅ content 쿼리 mock
        given(queryFactory.selectFrom(any(QGambleSeason.class))).willReturn(jpaQuery);
        given(jpaQuery.leftJoin(any(EntityPathBase.class), any(EntityPathBase.class))).willReturn(jpaQuery);
        given(jpaQuery.fetchJoin()).willReturn(jpaQuery);
        given(jpaQuery.where(any(BooleanBuilder.class))).willReturn(jpaQuery);
        given(jpaQuery.orderBy(any(OrderSpecifier.class))).willReturn(jpaQuery);
        given(jpaQuery.offset(anyLong())).willReturn(jpaQuery);
        given(jpaQuery.limit(anyLong())).willReturn(jpaQuery);
        given(jpaQuery.fetch()).willReturn(content);

        // when
        Page<SeasonListDTO> result = adminGambleSeasonService.getGambleSeasonListByFilter(request, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("테스트 시즌");

        // verify
        verify(queryFactory, times(1)).selectFrom(any(QGambleSeason.class));
        verify(jpaQuery, times(1)).fetch();
    }
    //endregion

    //region getGambleSeasonDetail
    @Test
    @DisplayName("getGambleSeasonDetail 성공 - DTO 변환")
    void getGambleSeasonDetail_success() {
        // given
        GambleSeason season = GambleSeason.builder().pk(1L).build();
        List<SeasonTeamDTO> teams = List.of(mock(SeasonTeamDTO.class));
        List<GetGambleSeasonRankingDTO> rankings = List.of(mock(GetGambleSeasonRankingDTO.class));

        when(gambleSeasonTeamService.getgambleSeasonTeamListByGambleSeasonPk(1L))
                .thenReturn(teams);
        when(gambleSeasonRankingService.getRankingDtoBySeasonPk(1L))
                .thenReturn(rankings);

        // when
        GambleSeasonDetailDTO result = adminGambleSeasonService.getGambleSeasonDetail(season);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getGambleSeasonTeams()).isEqualTo(teams);
        assertThat(result.getGambleSeasonRanking()).isEqualTo(rankings);
    }
    //endregion

    @Test
    @DisplayName("createGambleSeason 성공 - 새 시즌 생성")
    void createGambleSeason_success() {
        // given
        CreateGambleSeasonRequest request = new CreateGambleSeasonRequest();
        request.setLeaguePk(10L);
        request.setTitle("테스트 시즌");
        request.setStartedAt("2025-01-01T00:00:00");
        request.setFinishedAt("2025-02-01T00:00:00");
        request.setDescription("설명");

        League league = League.builder().pk(10L).build();
        ActualSeason actualSeason = ActualSeason.builder().pk(20L).build();

        when(leagueService.findByPk(10L)).thenReturn(league);
        when(actualSeasonService.findRecentByLeaguePk(10L)).thenReturn(actualSeason);

        // save() 호출 시 pk를 강제로 설정
        when(gambleSeasonRepository.save(any(GambleSeason.class)))
                .thenAnswer(invocation -> {
                    GambleSeason season = invocation.getArgument(0);
                    ReflectionTestUtils.setField(season, "pk", 30L);
                    return season;
                });

        when(gambleSeasonTeamService.getgambleSeasonTeamListByGambleSeasonPk(30L))
                .thenReturn(List.of());
        when(gambleSeasonRankingService.getRankingDtoBySeasonPk(30L))
                .thenReturn(List.of());

        // when
        GambleSeasonDetailDTO result = adminGambleSeasonService.createGambleSeason(request);

        // then
        assertThat(result).isNotNull();
        verify(gambleSeasonRepository, times(1)).save(any(GambleSeason.class));
    }

    @Test
    @DisplayName("createGambleSeason 실패 - 리그 없음")
    void createGambleSeason_notFoundLeague() {
        // given
        CreateGambleSeasonRequest request = new CreateGambleSeasonRequest();
        request.setLeaguePk(10L);

        when(leagueService.findByPk(10L)).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> adminGambleSeasonService.createGambleSeason(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("해당 리그를 찾을 수 없습니다.");
    }
    //endregion

    //region updateGambleSeason
    @Test
    @DisplayName("updateGambleSeason 성공 - 필드 수정 및 저장")
    void updateGambleSeason_success() {
        // given
        GambleSeason season = GambleSeason.builder().pk(1L).build();
        UpdateGambleSeasonRequest request = new UpdateGambleSeasonRequest();
        request.setTitle("수정된 시즌");
        request.setStartedAt("2025-03-01T00:00:00");
        request.setFinishedAt("2025-03-10T00:00:00");
        request.setOperatingStatus("PENDING");

        doNothing().when(gambleSeasonTeamService)
                .updateSeasonTeams(any(GambleSeason.class), any());

        when(gambleSeasonRepository.save(any(GambleSeason.class))).thenReturn(season);
        when(gambleSeasonTeamService.getgambleSeasonTeamListByGambleSeasonPk(1L))
                .thenReturn(List.of());
        when(gambleSeasonRankingService.getRankingDtoBySeasonPk(1L))
                .thenReturn(List.of());

        // when
        GambleSeasonDetailDTO result = adminGambleSeasonService.updateGambleSeason(season, request);

        // then
        assertThat(result).isNotNull();
        verify(gambleSeasonRepository, times(1)).save(any(GambleSeason.class));
        verify(gambleSeasonTeamService, times(1))
                .updateSeasonTeams(any(GambleSeason.class), any());
    }
    //endregion
}
