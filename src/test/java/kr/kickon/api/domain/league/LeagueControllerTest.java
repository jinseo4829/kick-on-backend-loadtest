package kr.kickon.api.domain.league;

import kr.kickon.api.domain.league.dto.LeagueDTO;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.entities.*;
import kr.kickon.api.global.common.enums.LeagueType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeagueControllerTest {

    @InjectMocks
    private LeagueController leagueController;

    @Mock
    private LeagueService leagueService;

    private League league1;
    private League league2;

    @BeforeEach
    void setUp() {
        league1 = League.builder()
                .pk(1L)
                .type(LeagueType.League)
                .nameEn("Premier League")
                .nameKr("프리미어 리그")
                .logoUrl("logo1.png")
                .build();

        league2 = League.builder()
                .pk(2L)
                .type(LeagueType.League)
                .nameEn("La Liga")
                .nameKr("라리가")
                .logoUrl("logo2.png")
                .build();
    }

    @Test
    @DisplayName("리그 리스트 조회 - 성공")
    void getHomeNews_success() {
        // given
        when(leagueService.findAll()).thenReturn(List.of(league1, league2));

        // when
        ResponseEntity<ResponseDTO<List<LeagueDTO>>> response = leagueController.getHomeNews();

        // then
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).hasSize(2);

        LeagueDTO dto1 = response.getBody().getData().get(0);
        assertThat(dto1.getPk()).isEqualTo(1L);
        assertThat(dto1.getNameEn()).isEqualTo("Premier League");

        LeagueDTO dto2 = response.getBody().getData().get(1);
        assertThat(dto2.getPk()).isEqualTo(2L);
        assertThat(dto2.getNameKr()).isEqualTo("라리가");
    }
}
