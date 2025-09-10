package kr.kickon.api.admin.partners;

import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.kickon.api.admin.partners.dto.PartnersDetailDTO;
import kr.kickon.api.admin.partners.request.CreatePartnersRequest;
import kr.kickon.api.admin.partners.request.UpdatePartnersRequest;
import kr.kickon.api.domain.actualSeasonTeam.ActualSeasonTeamService;
import kr.kickon.api.domain.partners.PartnersRepository;
import kr.kickon.api.domain.team.TeamService;
import kr.kickon.api.domain.user.UserService;
import kr.kickon.api.global.common.entities.*;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminPartnersServiceTest {

    @InjectMocks
    private AdminPartnersService adminPartnersService;

    @Mock
    private JPAQueryFactory queryFactory;
    @Mock
    private ActualSeasonTeamService actualSeasonTeamService;
    @Mock
    private PartnersRepository partnersRepository;
    @Mock
    private UserService userService;
    @Mock
    private TeamService teamService;

    //region findByPk
    @Test
    @DisplayName("파트너스 조회 성공")
    void findByPk_success() {
        Partners partners = Partners.builder().pk(1L).status(DataStatus.ACTIVATED).build();
        given(partnersRepository.findByPkAndStatus(1L, DataStatus.ACTIVATED))
                .willReturn(Optional.of(partners));

        Partners result = adminPartnersService.findByPk(1L);

        assertThat(result).isNotNull();
        assertThat(result.getPk()).isEqualTo(1L);
    }

    @Test
    @DisplayName("파트너스 조회 실패 - 존재하지 않음")
    void findByPk_notFound() {
        given(partnersRepository.findByPkAndStatus(1L, DataStatus.ACTIVATED))
                .willReturn(Optional.empty());

        Partners result = adminPartnersService.findByPk(1L);

        assertThat(result).isNull();
    }
    //endregion

    //region createPartners
    @Test
    @DisplayName("파트너스 생성 성공")
    void createPartners_success() {
        // given
        User user = User.builder().pk(1L).nickname("user").status(DataStatus.ACTIVATED).build();
        Team team = Team.builder().pk(10L).nameKr("팀").nameEn("team").logoUrl("logo.png").build();
        League league = League.builder().pk(100L).nameKr("리그").nameEn("league").logoUrl("league.png").build();
        ActualSeason actualSeason = ActualSeason.builder().pk(200L).league(league).build();
        ActualSeasonTeam actualSeasonTeam = ActualSeasonTeam.builder().actualSeason(actualSeason).team(team).build();

        CreatePartnersRequest request = new CreatePartnersRequest();
        request.setUserPk(1L);
        request.setTeamPk(10L);
        request.setName("파트너");
        request.setPartnersEmail("test@test.com");
        request.setSnsUrl("snsUrl");

        when(userService.findByPk(1L)).thenReturn(user);
        when(teamService.findByPk(10L)).thenReturn(team);
        when(actualSeasonTeamService.findLatestByTeam(10L)).thenReturn(actualSeasonTeam);

        // when
        PartnersDetailDTO result = adminPartnersService.createPartners(request);

        // then
        assertThat(result.getUser().getPk()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("파트너");
        assertThat(result.getPartnersEmail()).isEqualTo("test@test.com");
        assertThat(result.getSnsUrl()).isEqualTo("snsUrl");

        // teamDTO 검증
        assertThat(result.getTeam().getPk()).isEqualTo(10L);
        assertThat(result.getTeam().getNameKr()).isEqualTo("팀");
        assertThat(result.getTeam().getNameEn()).isEqualTo("team");
        assertThat(result.getTeam().getLogoUrl()).isEqualTo("logo.png");
        assertThat(result.getTeam().getLeaguePk()).isEqualTo(100L);
        assertThat(result.getTeam().getLeagueNameKr()).isEqualTo("리그");
        assertThat(result.getTeam().getLeagueNameEn()).isEqualTo("league");
        assertThat(result.getTeam().getLeagueLogoUrl()).isEqualTo("league.png");

        verify(partnersRepository).save(any(Partners.class));
    }


    @Test
    @DisplayName("파트너스 생성 실패 - 유저 없음")
    void createPartners_fail_userNotFound() {
        CreatePartnersRequest request = new CreatePartnersRequest();
        request.setUserPk(99L);
        request.setTeamPk(10L);

        given(userService.findByPk(99L)).willReturn(null);

        assertThatThrownBy(() -> adminPartnersService.createPartners(request))
                .isInstanceOf(NotFoundException.class)
                .extracting("responseCode")
                .isEqualTo(ResponseCode.NOT_FOUND_USER);
    }

    @Test
    @DisplayName("파트너스 생성 실패 - 팀 없음")
    void createPartners_fail_teamNotFound() {
        CreatePartnersRequest request = new CreatePartnersRequest();
        request.setUserPk(1L);
        request.setTeamPk(99L);

        given(userService.findByPk(1L)).willReturn(User.builder().pk(1L).build());
        given(teamService.findByPk(99L)).willReturn(null);

        assertThatThrownBy(() -> adminPartnersService.createPartners(request))
                .isInstanceOf(NotFoundException.class)
                .extracting("responseCode")
                .isEqualTo(ResponseCode.NOT_FOUND_TEAM);
    }
    //endregion

    //region deletePartners
    @Test
    @DisplayName("파트너스 삭제 - 상태 DEACTIVATED 변경")
    void deletePartners_success() {
        Partners partners = Partners.builder()
                .pk(1L)
                .status(DataStatus.ACTIVATED)
                .build();

        adminPartnersService.deletePartners(partners);

        assertThat(partners.getStatus()).isEqualTo(DataStatus.DEACTIVATED);
        verify(partnersRepository).save(partners);
    }
    //endregion

    //region updatePartners
    @Test
    @DisplayName("파트너스 수정 성공 - 이름 변경")
    void updatePartners_success() {
        Partners partners = Partners.builder()
                .pk(1L)
                .name("oldName")
                .user(User.builder().pk(1L).build())
                .team(Team.builder().pk(1L).build())
                .status(DataStatus.ACTIVATED)
                .build();

        UpdatePartnersRequest request = new UpdatePartnersRequest();
        request.setName("newName");

        given(partnersRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        PartnersDetailDTO result = adminPartnersService.updatePartners(partners, request);

        assertThat(result.getName()).isEqualTo("newName");
        verify(partnersRepository).save(partners);
    }
    //endregion
}