package kr.kickon.api.domain.shorts;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.kickon.api.domain.shorts.dto.ShortsDTO;
import kr.kickon.api.domain.shorts.dto.ShortsDetailDTO;
import kr.kickon.api.domain.shorts.request.GetShortsRequest;
import kr.kickon.api.global.common.entities.Shorts;
import kr.kickon.api.global.common.enums.ShortsSortType;
import kr.kickon.api.global.common.enums.ShortsType;
import kr.kickon.api.global.error.handler.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ShortsControllerTest {

    @InjectMocks
    private ShortsController shortsController;

    @Mock
    private ShortsService shortsService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(shortsController)
                .setControllerAdvice(new GlobalExceptionHandler(new ObjectMapper()))
                .build();
        objectMapper = new ObjectMapper();
    }

    // region 고정 쇼츠 조회
    @Test
    @DisplayName("고정 쇼츠 리스트 조회 성공")
    void getFixedShorts_success() throws Exception {
        List<ShortsDTO> shortsList = List.of(
                ShortsDTO.builder().pk(1L).title("Short 1").build(),
                ShortsDTO.builder().pk(2L).title("Short 2").build()
        );
        when(shortsService.getFixedShorts()).thenReturn(shortsList);

        mockMvc.perform(get("/api/shorts/fixed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("GET_SUCCESS"))
                .andExpect(jsonPath("$.data[0].pk").value(1L))
                .andExpect(jsonPath("$.data[1].title").value("Short 2"));

        verify(shortsService, times(1)).getFixedShorts();
    }
    // endregion

    // region 쇼츠 리스트 조회
    @Test
    @DisplayName("쇼츠 리스트 조회 성공 - 페이징 적용")
    void getShorts_success() throws Exception {
        GetShortsRequest request = new GetShortsRequest();
        request.setPage(1);
        request.setSize(2);
        request.setSort(ShortsSortType.CREATED_DESC);

        List<ShortsDTO> content = List.of(
                ShortsDTO.builder().pk(1L).title("Short 1").build(),
                ShortsDTO.builder().pk(2L).title("Short 2").build()
        );
        Page<ShortsDTO> page = new PageImpl<>(content, PageRequest.of(0, 2), 5);

        when(shortsService.getShortsWithPagination(any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/shorts")
                        .param("page", "1")
                        .param("size", "2")
                        .param("sort", "CREATED_DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("GET_SUCCESS"))
                .andExpect(jsonPath("$.data[0].pk").value(1L))
                .andExpect(jsonPath("$.data[1].title").value("Short 2"))
                .andExpect(jsonPath("$.meta.currentPage").value(1))
                .andExpect(jsonPath("$.meta.pageSize").value(2))
                .andExpect(jsonPath("$.meta.totalItems").value(5));

        verify(shortsService, times(1)).getShortsWithPagination(any(), any());
    }
    // endregion

    // region 쇼츠 상세 조회
    @Test
    @DisplayName("쇼츠 상세 조회 성공")
    void getShortsDetail_success() throws Exception {
        Long pk = 1L;
        Shorts shorts = Shorts.builder().pk(pk).type(ShortsType.AWS_FILE).build();
        ShortsDetailDTO dto = ShortsDetailDTO.builder().pk(pk).title("Short 1").build();

        when(shortsService.findByPk(pk)).thenReturn(shorts);
        when(shortsService.getShortsDetail(shorts, ShortsSortType.CREATED_DESC)).thenReturn(dto);

        mockMvc.perform(get("/api/shorts/{pk}", pk)
                        .param("sort", "CREATED_DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("GET_SUCCESS"))
                .andExpect(jsonPath("$.data.pk").value(1L))
                .andExpect(jsonPath("$.data.title").value("Short 1"));

        verify(shortsService, times(1)).findByPk(pk);
        verify(shortsService, times(1)).getShortsDetail(shorts, ShortsSortType.CREATED_DESC);
    }

    @Test
    @DisplayName("쇼츠 상세 조회 실패 - NotFoundException")
    void getShortsDetail_notFound() throws Exception {
        Long pk = 999L;
        when(shortsService.findByPk(pk)).thenReturn(null);

        mockMvc.perform(get("/api/shorts/{pk}", pk))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND_SHORTS"));

        verify(shortsService, times(1)).findByPk(pk);
        verify(shortsService, never()).getShortsDetail(any(), any());
    }
    // endregion
}
