package kr.kickon.api.admin.actualSeason;

import kr.kickon.api.admin.actualSeason.dto.ActualSeasonDetailDTO;
import kr.kickon.api.admin.gambleSeason.dto.SeasonListDTO;
import kr.kickon.api.admin.actualSeason.request.UpdateActualSeasonRequest;
import kr.kickon.api.global.common.entities.ActualSeason;
import kr.kickon.api.global.error.handler.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class AdminActualSeasonControllerTest {

    @InjectMocks
    private AdminActualSeasonController adminActualSeasonController;

    @Mock
    private AdminActualSeasonService adminActualSeasonService;

    private ObjectMapper objectMapper;
    private MockMvc mockMvc;

    private ActualSeason mockActualSeason;

    @BeforeEach
    public void init() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminActualSeasonController)
                .setControllerAdvice(new GlobalExceptionHandler(new ObjectMapper()))
                .build();
        objectMapper = new ObjectMapper();

        mockActualSeason = new ActualSeason();
        mockActualSeason.setPk(1L);
        mockActualSeason.setTitle("시즌1");
    }

    @Test
    @DisplayName("실제 시즌 리스트 조회 성공")
    void getFilteredActualSeasons_success() throws Exception {
        // given
        List<SeasonListDTO> seasonList = List.of(
                SeasonListDTO.builder().pk(1L).title("시즌1").build(),
                SeasonListDTO.builder().pk(2L).title("시즌2").build()
        );
        Page<SeasonListDTO> page = new PageImpl<>(seasonList);
        given(adminActualSeasonService.getActualSeasonListByFilter(any(), any(Pageable.class)))
                .willReturn(page);

        // when
        ResultActions resultActions = mockMvc.perform(get("/admin/actualSeason")
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
    @DisplayName("실제 시즌 상세 조회 성공")
    void getActualSeasonDetail_success() throws Exception {
        // given
        given(adminActualSeasonService.findByPk(1L)).willReturn(mockActualSeason);
        ActualSeasonDetailDTO dto = ActualSeasonDetailDTO.builder().pk(1L).title("시즌1").build();
        given(adminActualSeasonService.getActualSeasonDetail(mockActualSeason)).willReturn(dto);

        // when & then
        mockMvc.perform(get("/admin/actualSeason/{pk}", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("GET_SUCCESS"))
                .andExpect(jsonPath("$.data.title").value("시즌1"));
    }

    @Test
    @DisplayName("실제 시즌 상세 조회 실패 - NotFoundException")
    void getActualSeasonDetail_notFound() throws Exception {
        // given
        given(adminActualSeasonService.findByPk(999L)).willReturn(null);

        // when & then
        mockMvc.perform(get("/admin/actualSeason/{pk}", 999L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND_ACTUAL_SEASON"));
    }

    @Test
    @DisplayName("실제 시즌 수정 성공")
    void patchActualSeason_success() throws Exception {
        // given
        UpdateActualSeasonRequest request = new UpdateActualSeasonRequest();
        request.setTitle("시즌1 수정");

        given(adminActualSeasonService.findByPk(1L)).willReturn(mockActualSeason);
        ActualSeasonDetailDTO responseDto = ActualSeasonDetailDTO.builder().pk(1L).title("시즌1 수정").build();
        given(adminActualSeasonService.updateActualSeason(mockActualSeason, request)).willReturn(responseDto);

        // when & then
        mockMvc.perform(patch("/admin/actualSeason/{pk}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("GET_SUCCESS"))
                .andExpect(jsonPath("$.data.title").value("시즌1 수정"));
    }

    @Test
    @DisplayName("실제 시즌 수정 실패 - NotFoundException")
    void patchActualSeason_notFound() throws Exception {
        // given
        UpdateActualSeasonRequest request = new UpdateActualSeasonRequest();
        request.setTitle("시즌1 수정");

        given(adminActualSeasonService.findByPk(999L)).willReturn(null);

        // when & then
        mockMvc.perform(patch("/admin/actualSeason/{pk}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND_ACTUAL_SEASON"));
    }

    @Test
    @DisplayName("실제 시즌 삭제 성공")
    void deleteActualSeason_success() throws Exception {
        // given
        given(adminActualSeasonService.findByPk(1L)).willReturn(mockActualSeason);
        doNothing().when(adminActualSeasonService).deleteActualSeason(mockActualSeason);

        // when & then
        mockMvc.perform(delete("/admin/actualSeason/{pk}", 1L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("GET_SUCCESS"));
    }

    @Test
    @DisplayName("실제 시즌 삭제 실패 - NotFoundException")
    void deleteActualSeason_notFound() throws Exception {
        // given
        given(adminActualSeasonService.findByPk(999L)).willReturn(null);

        // when & then
        mockMvc.perform(delete("/admin/actualSeason/{pk}", 999L)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND_ACTUAL_SEASON"));
    }
}
