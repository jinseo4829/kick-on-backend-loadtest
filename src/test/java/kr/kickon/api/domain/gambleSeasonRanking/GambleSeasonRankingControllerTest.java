package kr.kickon.api.domain.gambleSeasonRanking;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.kickon.api.domain.gambleSeason.GambleSeasonService;
import kr.kickon.api.domain.gambleSeasonRanking.dto.GetGambleSeasonRankingDTO;
import kr.kickon.api.domain.gambleSeasonRanking.request.GetGambleSeasonRankingRequestDTO;
import kr.kickon.api.global.auth.jwt.user.JwtTokenProvider;
import kr.kickon.api.global.common.entities.GambleSeason;
import kr.kickon.api.global.common.entities.GambleSeasonRanking;
import kr.kickon.api.global.common.entities.Team;
import kr.kickon.api.global.common.entities.User;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.error.handler.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
        import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class GambleSeasonRankingControllerTest {

    @InjectMocks
    private GambleSeasonRankingController gambleSeasonRankingController;

    @Mock
    private GambleSeasonRankingService gambleSeasonRankingService;

    @Mock
    private GambleSeasonService gambleSeasonService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private User mockUser;
    private GambleSeason mockGambleSeason;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(gambleSeasonRankingController)
                .setControllerAdvice(new GlobalExceptionHandler(new ObjectMapper()))
                .build();
        objectMapper = new ObjectMapper();

        mockUser = User.builder().id("user1").nickname("테스트 유저").build();
        mockGambleSeason = new GambleSeason();
        mockGambleSeason.setPk(1L);
        mockGambleSeason.setTitle("시즌1");
    }

    @Test
    @DisplayName("게임 시즌 랭킹 리스트 조회 성공")
    void getGambleSeasonRanking_success() throws Exception {
        // given
        GetGambleSeasonRankingRequestDTO paramDto = new GetGambleSeasonRankingRequestDTO();
        paramDto.setLeague(10L);

        // Mock user
        User mockUser = User.builder().id("user1").build();
        given(jwtTokenProvider.getUserFromSecurityContext()).willReturn(mockUser);

        // Mock recent operating season
        GambleSeason mockGambleSeason = GambleSeason.builder().pk(1L).title("시즌1").build();
        given(gambleSeasonService.findRecentOperatingSeasonByLeaguePk(paramDto.getLeague()))
                .willReturn(mockGambleSeason);

        // Mock GambleSeasonRanking 엔티티
        Team teamA = Team.builder().pk(1L).nameKr("팀A").logoUrl("logo1.png").build();
        Team teamB = Team.builder().pk(2L).nameKr("팀B").logoUrl("logo2.png").build();

        GambleSeasonRanking ranking1 = GambleSeasonRanking.builder()
                .rankOrder(1)
                .points(100_000) // 컨트롤러에서 100_000/1000 = 100.0
                .gameNum(10)
                .team(teamA)
                .build();

        GambleSeasonRanking ranking2 = GambleSeasonRanking.builder()
                .rankOrder(2)
                .points(80_000)
                .gameNum(10)
                .team(teamB)
                .build();

        List<GambleSeasonRanking> entityList = List.of(ranking1, ranking2);

        given(gambleSeasonRankingService.findRecentSeasonRankingByLeague(mockGambleSeason.getPk()))
                .willReturn(entityList);

        // when & then
        mockMvc.perform(get("/api/gamble-season-ranking")
                        .param("league", String.valueOf(paramDto.getLeague()))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("GET_SUCCESS"))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].rankOrder").value(1))
                .andExpect(jsonPath("$.data[0].teamName").value("팀A"))
                .andExpect(jsonPath("$.data[0].points").value(100.0))
                .andExpect(jsonPath("$.data[1].rankOrder").value(2))
                .andExpect(jsonPath("$.data[1].teamName").value("팀B"))
                .andExpect(jsonPath("$.data[1].points").value(80.0));
    }

    @Test
    @DisplayName("게임 시즌 랭킹 조회 실패 - 실제 시즌 NotFoundException")
    void getGambleSeasonRanking_notFound() throws Exception {
        // given
        GetGambleSeasonRankingRequestDTO paramDto = new GetGambleSeasonRankingRequestDTO();
        paramDto.setLeague(999L);

        given(jwtTokenProvider.getUserFromSecurityContext()).willReturn(mockUser);
        given(gambleSeasonService.findRecentOperatingSeasonByLeaguePk(paramDto.getLeague())).willReturn(null);

        // when & then
        mockMvc.perform(get("/api/gamble-season-ranking")
                        .param("league", String.valueOf(paramDto.getLeague()))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ResponseCode.NOT_FOUND_GAMBLE_SEASON.getCode()));
    }
}