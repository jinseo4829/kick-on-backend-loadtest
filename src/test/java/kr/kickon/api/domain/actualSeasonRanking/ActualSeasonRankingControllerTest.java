package kr.kickon.api.domain.actualSeasonRanking;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.kickon.api.domain.actualSeason.ActualSeasonService;
import kr.kickon.api.domain.actualSeasonRanking.dto.GetActualSeasonRankingDTO;
import kr.kickon.api.domain.actualSeasonRanking.request.GetActualSeasonRankingRequestDTO;
import kr.kickon.api.global.auth.jwt.user.JwtTokenProvider;
import kr.kickon.api.global.common.entities.ActualSeason;
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
class ActualSeasonRankingControllerTest {

    @InjectMocks
    private ActualSeasonRankingController actualSeasonRankingController;

    @Mock
    private ActualSeasonRankingService actualSeasonRankingService;

    @Mock
    private ActualSeasonService actualSeasonService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private User mockUser;
    private ActualSeason mockActualSeason;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(actualSeasonRankingController)
                .setControllerAdvice(new GlobalExceptionHandler(new ObjectMapper()))
                .build();
        objectMapper = new ObjectMapper();

        mockUser = User.builder().id("user1").nickname("테스트 유저").build();
        mockActualSeason = new ActualSeason();
        mockActualSeason.setPk(1L);
        mockActualSeason.setTitle("시즌1");
    }

    @Test
    @DisplayName("실제 시즌 랭킹 리스트 조회 성공")
    void getActualSeasonRanking_success() throws Exception {
        // given
        GetActualSeasonRankingRequestDTO paramDto = new GetActualSeasonRankingRequestDTO();
        paramDto.setLeague(10L);

        given(jwtTokenProvider.getUserFromSecurityContext()).willReturn(mockUser);
        given(actualSeasonService.findRecentByLeaguePk(paramDto.getLeague())).willReturn(mockActualSeason);

        List<GetActualSeasonRankingDTO> rankingList = List.of(
                new GetActualSeasonRankingDTO(1, "logo1.png", "팀A", 10, 100, 12),
                new GetActualSeasonRankingDTO(2, "logo2.png", "팀B", 10, 80, 10)
        );

        given(actualSeasonRankingService.findRecentSeasonRankingByLeague(mockActualSeason.getPk())).willReturn(rankingList);

// when & then
        mockMvc.perform(get("/api/actual-season-ranking")
                        .param("league", String.valueOf(paramDto.getLeague()))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("GET_SUCCESS"))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].rankOrder").value(1))
                .andExpect(jsonPath("$.data[0].teamName").value("팀A"))
                .andExpect(jsonPath("$.data[0].points").value(100));
    }

    @Test
    @DisplayName("실제 시즌 랭킹 조회 실패 - 실제 시즌 NotFoundException")
    void getActualSeasonRanking_notFound() throws Exception {
        // given
        GetActualSeasonRankingRequestDTO paramDto = new GetActualSeasonRankingRequestDTO();
        paramDto.setLeague(999L);

        given(jwtTokenProvider.getUserFromSecurityContext()).willReturn(mockUser);
        given(actualSeasonService.findRecentByLeaguePk(paramDto.getLeague())).willReturn(null);

        // when & then
        mockMvc.perform(get("/api/actual-season-ranking")
                        .param("league", String.valueOf(paramDto.getLeague()))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ResponseCode.NOT_FOUND_ACTUAL_SEASON.getCode()));
    }
}
