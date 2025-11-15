package kr.kickon.api.admin.actualSeason;

import static org.junit.jupiter.api.Assertions.*;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.EntityPathBase;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.kickon.api.admin.actualSeason.dto.ActualSeasonDetailDTO;
import kr.kickon.api.admin.actualSeason.request.ActualSeasonFilterRequest;
import kr.kickon.api.admin.actualSeason.request.UpdateActualSeasonRequest;
import kr.kickon.api.admin.gambleSeason.dto.SeasonListDTO;
import kr.kickon.api.domain.actualSeason.ActualSeasonRepository;
import kr.kickon.api.domain.actualSeasonTeam.ActualSeasonTeamService;
import kr.kickon.api.domain.league.LeagueService;
import kr.kickon.api.domain.team.dto.SeasonTeamDTO;
import kr.kickon.api.global.common.entities.*;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.common.enums.OperatingStatus;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.error.exceptions.BadRequestException;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class AdminActualSeasonServiceTest {

    @InjectMocks
    private AdminActualSeasonService adminActualSeasonService;

    @Mock
    private JPAQueryFactory queryFactory;

    @Mock
    private ActualSeasonRepository actualSeasonRepository;

    @Mock
    private ActualSeasonTeamService actualSeasonTeamService;

    @Mock
    private LeagueService leagueService;

    @Mock
    private JPAQuery<ActualSeason> jpaQuery;

    //region findByPk
    @Test
    @DisplayName("findByPk 성공 - ActualSeason 반환")
    void findByPk_success() {
        // given
        ActualSeason season = ActualSeason.builder()
                .pk(1L)
                .status(DataStatus.ACTIVATED)
                .build();

        given(actualSeasonRepository.findByPkAndStatus(anyLong(), any()))
                .willReturn(Optional.of(season));

        // when
        ActualSeason result = adminActualSeasonService.findByPk(1L);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getPk()).isEqualTo(1L);
        verify(actualSeasonRepository).findByPkAndStatus(1L, DataStatus.ACTIVATED);
    }

    @Test
    @DisplayName("findByPk 실패 - 존재하지 않음")
    void findByPk_notFound() {
        // given
        given(actualSeasonRepository.findByPkAndStatus(anyLong(), any()))
                .willReturn(Optional.empty());

        // when
        ActualSeason result = adminActualSeasonService.findByPk(99L);

        // then
        assertThat(result).isNull();
        verify(actualSeasonRepository).findByPkAndStatus(99L, DataStatus.ACTIVATED);
    }
    //endregion

    //region getActualSeasonListByFilter
    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("getActualSeasonListByFilter 성공 - 필터 조회")
    void getActualSeasonListByFilter_success() {
        // given
        ActualSeasonFilterRequest request = new ActualSeasonFilterRequest();
        Pageable pageable = PageRequest.of(0, 10);
        List<ActualSeason> content = List.of(ActualSeason.builder()
                .pk(1L)
                .title("테스트 시즌")
                .startedAt(LocalDate.of(2025, 1, 1))
                .finishedAt(LocalDate.of(2025, 2, 1))
                .build());

        // ✅ count 쿼리 mock
        JPAQuery<Long> countQuery = mock(JPAQuery.class);
        given(queryFactory.select((Expression<Object>) any())).willReturn((JPAQuery) countQuery);
        given(countQuery.from(any(EntityPathBase.class))).willReturn(countQuery);
        given(countQuery.leftJoin(any(EntityPathBase.class), any(EntityPathBase.class))).willReturn(countQuery);
        given(countQuery.where(any(BooleanBuilder.class))).willReturn(countQuery);
        given(countQuery.fetchOne()).willReturn(1L);

        // ✅ content 쿼리 mock
        given(queryFactory.selectFrom(any(QActualSeason.class))).willReturn(jpaQuery);
        given(jpaQuery.leftJoin(any(EntityPathBase.class), any(EntityPathBase.class))).willReturn(jpaQuery);
        given(jpaQuery.fetchJoin()).willReturn(jpaQuery);
        given(jpaQuery.where(any(BooleanBuilder.class))).willReturn(jpaQuery);
        given(jpaQuery.orderBy(any(OrderSpecifier.class))).willReturn(jpaQuery);
        given(jpaQuery.offset(anyLong())).willReturn(jpaQuery);
        given(jpaQuery.limit(anyLong())).willReturn(jpaQuery);
        given(jpaQuery.fetch()).willReturn(content);

        // when
        Page<SeasonListDTO> result = adminActualSeasonService.getActualSeasonListByFilter(request, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("테스트 시즌");
    }
    //endregion

    //region getActualSeasonDetail
    @Test
    @DisplayName("getActualSeasonDetail 성공 - DTO 변환")
    void getActualSeasonDetail_success() {
        // given
        ActualSeason season = ActualSeason.builder()
                .pk(1L)
                .title("테스트 시즌")
                .startedAt(LocalDate.of(2025, 1, 1))
                .finishedAt(LocalDate.of(2025, 2, 1))
                .build();
        List<SeasonTeamDTO> teams = List.of(mock(SeasonTeamDTO.class));

        when(actualSeasonTeamService.findAllByActualSeason(season)).thenReturn(teams);

        // when
        ActualSeasonDetailDTO result = adminActualSeasonService.getActualSeasonDetail(season);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getActualSeasonTeams()).isEqualTo(teams);
    }
    //endregion

    //region updateActualSeason
    @Test
    @DisplayName("updateActualSeason 성공 - 필드 수정 및 저장")
    void updateActualSeason_success() {
        // given
        ActualSeason season = ActualSeason.builder().pk(1L).build();
        UpdateActualSeasonRequest request = new UpdateActualSeasonRequest();
        request.setTitle("수정된 시즌");
        request.setStartedAt("2025-03-01T00:00:00");
        request.setFinishedAt("2025-03-10T00:00:00");
        request.setOperatingStatus("PENDING");

        doNothing().when(actualSeasonTeamService).updateSeasonTeams(any(), any());
        when(actualSeasonRepository.save(any(ActualSeason.class))).thenReturn(season);
        when(actualSeasonTeamService.findAllByActualSeason(any(ActualSeason.class)))
                .thenReturn(List.of());

        // when
        ActualSeasonDetailDTO result = adminActualSeasonService.updateActualSeason(season, request);

        // then
        assertThat(result).isNotNull();
        verify(actualSeasonRepository, times(1)).save(any(ActualSeason.class));
        verify(actualSeasonTeamService, times(1))
                .updateSeasonTeams(any(), any());
    }

    @Test
    @DisplayName("updateActualSeason 실패 - 잘못된 운영 상태 ENUM")
    void updateActualSeason_invalidEnum() {
        // given
        ActualSeason season = ActualSeason.builder().pk(1L).build();
        UpdateActualSeasonRequest request = new UpdateActualSeasonRequest();
        request.setOperatingStatus("INVALID_STATUS");

        // when & then
        assertThatThrownBy(() -> adminActualSeasonService.updateActualSeason(season, request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Validation failed");
    }

    @Test
    @DisplayName("updateActualSeason 실패 - 리그 없음")
    void updateActualSeason_notFoundLeague() {
        // given
        ActualSeason season = ActualSeason.builder().pk(1L).build();
        UpdateActualSeasonRequest request = new UpdateActualSeasonRequest();
        request.setLeaguePk(100L);

        when(leagueService.findByPk(100L)).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> adminActualSeasonService.updateActualSeason(season, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("해당 리그를 찾을 수 없습니다.");
    }
    //endregion

    //region deleteActualSeason
    @Test
    @DisplayName("deleteActualSeason 성공 - soft delete")
    void deleteActualSeason_success() {
        // given
        ActualSeason season = ActualSeason.builder().pk(1L)
                .status(DataStatus.ACTIVATED)
                .build();

        when(actualSeasonRepository.save(any(ActualSeason.class))).thenReturn(season);
        doNothing().when(actualSeasonTeamService).deleteActualSeasonTeams(any(ActualSeason.class));

        // when
        adminActualSeasonService.deleteActualSeason(season);

        // then
        assertThat(season.getStatus()).isEqualTo(DataStatus.DEACTIVATED);
        verify(actualSeasonRepository).save(any(ActualSeason.class));
        verify(actualSeasonTeamService).deleteActualSeasonTeams(any(ActualSeason.class));
    }
    //endregion
}