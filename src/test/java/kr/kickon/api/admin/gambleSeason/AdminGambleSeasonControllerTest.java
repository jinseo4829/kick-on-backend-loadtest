package kr.kickon.api.admin.gambleSeason;

import kr.kickon.api.admin.gambleSeason.dto.GambleSeasonDetailDTO;
import kr.kickon.api.admin.gambleSeason.dto.SeasonListDTO;
import kr.kickon.api.admin.gambleSeason.request.CreateGambleSeasonRequest;
import kr.kickon.api.admin.gambleSeason.request.UpdateGambleSeasonRequest;
import kr.kickon.api.global.common.entities.GambleSeason;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.kickon.api.global.error.handler.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class AdminGambleSeasonControllerTest {

    @InjectMocks
    private AdminGambleSeasonController adminGambleSeasonController;

    @Mock
    private AdminGambleSeasonService adminGambleSeasonService;

    private ObjectMapper objectMapper;
    private MockMvc mockMvc;

    private GambleSeason mockGambleSeason;

    @BeforeEach
    public void init() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminGambleSeasonController)
                .setControllerAdvice(new GlobalExceptionHandler(new ObjectMapper()))
                .build();
        objectMapper = new ObjectMapper();

        mockGambleSeason = new GambleSeason();
        mockGambleSeason.setPk(1L);
        mockGambleSeason.setTitle("시즌1");
    }

    @Test
    @DisplayName("승부 예측 시즌 리스트 조회 성공")
    void getFilteredGambleSeasons_success() throws Exception {
        // given
        List<SeasonListDTO> seasonList = List.of(
                SeasonListDTO.builder().pk(1L).title("시즌1").build(),
                SeasonListDTO.builder().pk(2L).title("시즌2").build()
        );
        Page<SeasonListDTO> page = new PageImpl<>(seasonList);
        given(adminGambleSeasonService.getGambleSeasonListByFilter(any(), any(Pageable.class)))
                .willReturn(page);

        // when
        ResultActions resultActions = mockMvc.perform(get("/admin/gambleSeason")
                .param("page", "1")
                .param("size", "10")
                .accept(MediaType.APPLICATION_JSON));

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("GET_SUCCESS"))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].title").value("시즌1"));
    }

    @Test
    @DisplayName("승부 예측 시즌 상세 조회 성공")
    void getGambleSeasonDetail_success() throws Exception {
        // given
        given(adminGambleSeasonService.findByPk(1L)).willReturn(mockGambleSeason);
        GambleSeasonDetailDTO dto = GambleSeasonDetailDTO.builder().pk(1L).title("시즌1").build();
        given(adminGambleSeasonService.getGambleSeasonDetail(mockGambleSeason)).willReturn(dto);

        // when & then
        mockMvc.perform(get("/admin/gambleSeason/{pk}", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("GET_SUCCESS"))
                .andExpect(jsonPath("$.data.title").value("시즌1"));
    }

    @Test
    @DisplayName("승부 예측 시즌 상세 조회 실패 - NotFoundException")
    void getGambleSeasonDetail_notFound() throws Exception {
        // given
        given(adminGambleSeasonService.findByPk(999L)).willReturn(null);

        // when & then
        mockMvc.perform(get("/admin/gambleSeason/{pk}", 999L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND_GAMBLE_SEASON"));
    }

    @Test
    @DisplayName("승부 예측 시즌 생성 성공")
    void createGambleSeason_success() throws Exception {
        // given
        CreateGambleSeasonRequest request = new CreateGambleSeasonRequest();
        request.setTitle("시즌1");

        GambleSeasonDetailDTO responseDto = GambleSeasonDetailDTO.builder().pk(1L).title("시즌1").build();
        given(adminGambleSeasonService.createGambleSeason(any(CreateGambleSeasonRequest.class)))
                .willReturn(responseDto);

        // when & then
        mockMvc.perform(post("/admin/gambleSeason")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("GET_SUCCESS"))
                .andExpect(jsonPath("$.data.title").value("시즌1"));
    }

    @Test
    @DisplayName("승부 예측 시즌 수정 성공")
    void patchGambleSeason_success() throws Exception {
        // given
        UpdateGambleSeasonRequest request = new UpdateGambleSeasonRequest();
        request.setTitle("시즌1 수정");

        given(adminGambleSeasonService.findByPk(1L)).willReturn(mockGambleSeason);
        GambleSeasonDetailDTO responseDto = GambleSeasonDetailDTO.builder().pk(1L).title("시즌1 수정").build();
        given(adminGambleSeasonService.updateGambleSeason(mockGambleSeason, request)).willReturn(responseDto);

        // when & then
        mockMvc.perform(patch("/admin/gambleSeason/{pk}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("GET_SUCCESS"))
                .andExpect(jsonPath("$.data.title").value("시즌1 수정"));
    }

    @Test
    @DisplayName("승부 예측 시즌 수정 실패 - NotFoundException")
    void patchGambleSeason_notFound() throws Exception {
        // given
        UpdateGambleSeasonRequest request = new UpdateGambleSeasonRequest();
        request.setTitle("시즌1 수정");

        given(adminGambleSeasonService.findByPk(999L)).willReturn(null);

        // when & then
        mockMvc.perform(patch("/admin/gambleSeason/{pk}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND_GAMBLE_SEASON"));
    }
}
