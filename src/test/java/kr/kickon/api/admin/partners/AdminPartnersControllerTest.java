package kr.kickon.api.admin.partners;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.kickon.api.admin.partners.dto.PartnersDetailDTO;
import kr.kickon.api.admin.partners.dto.PartnersListDTO;
import kr.kickon.api.admin.partners.request.CreatePartnersRequest;
import kr.kickon.api.admin.partners.request.UpdatePartnersRequest;
import kr.kickon.api.global.common.entities.Partners;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminPartnersControllerTest {

    @InjectMocks
    private AdminPartnersController adminPartnersController;

    @Mock
    private AdminPartnersService adminPartnersService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminPartnersController)
                .setControllerAdvice(new GlobalExceptionHandler(new ObjectMapper()))
                .build();
        objectMapper = new ObjectMapper();
    }

    //region 파트너스 리스트 조회
    @Test
    @DisplayName("파트너스 리스트 조회 성공")
    void getPartnersList_success() throws Exception {
        // given
        PartnersListDTO dto = new PartnersListDTO();
        dto.setName("파트너A");

        Page<PartnersListDTO> mockPage = new PageImpl<>(List.of(dto));

        when(adminPartnersService.getPartnersListByFilter(any(), any())).thenReturn(mockPage);

        // when & then
        mockMvc.perform(get("/admin/partners")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("GET_SUCCESS"))
                .andExpect(jsonPath("$.data[0].name").value("파트너A"));
    }
    //endregion

    //region 파트너스 상세 조회
    @Test
    @DisplayName("파트너스 상세 조회 성공")
    void getPartnersDetail_success() throws Exception {
        // given
        Partners partners = new Partners();
        PartnersDetailDTO detailDTO = new PartnersDetailDTO();
        detailDTO.setName("파트너B");

        when(adminPartnersService.findByPk(1L)).thenReturn(partners);
        when(adminPartnersService.getPartnersDetail(partners)).thenReturn(detailDTO);

        // when & then
        mockMvc.perform(get("/admin/partners/{pk}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("GET_SUCCESS"))
                .andExpect(jsonPath("$.data.name").value("파트너B"));
    }

    @Test
    @DisplayName("파트너스 상세 조회 실패 - NotFound")
    void getPartnersDetail_notFound() throws Exception {
        // given
        when(adminPartnersService.findByPk(999L)).thenReturn(null);

        // when & then
        mockMvc.perform(get("/admin/partners/{pk}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND_PARTNERS"));
    }
    //endregion

    //region 파트너스 생성
    @Test
    @DisplayName("파트너스 생성 성공")
    void createPartners_success() throws Exception {
        // given
        CreatePartnersRequest request = new CreatePartnersRequest();
        request.setTeamPk(1L);
        request.setUserPk(2L);
        request.setPartnersEmail("test@example.com");

        PartnersDetailDTO responseDTO = new PartnersDetailDTO();
        responseDTO.setName("파트너C");

        when(adminPartnersService.createPartners(any(CreatePartnersRequest.class)))
                .thenReturn(responseDTO);

        // when & then
        mockMvc.perform(post("/admin/partners")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("GET_SUCCESS"))
                .andExpect(jsonPath("$.data.name").value("파트너C"));
    }

    @Test
    @DisplayName("파트너스 생성 실패 - 유효성 검증 실패")
    void createPartners_validationFail() throws Exception {
        // given
        CreatePartnersRequest request = new CreatePartnersRequest();
        // teamPk, userPk 누락

        // when & then
        mockMvc.perform(post("/admin/partners")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }
    //endregion

    //region 파트너스 삭제
    @Test
    @DisplayName("파트너스 삭제 성공")
    void deletePartners_success() throws Exception {
        // given
        Partners partners = new Partners();
        when(adminPartnersService.findByPk(1L)).thenReturn(partners);

        // when & then
        mockMvc.perform(delete("/admin/partners/{pk}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("GET_SUCCESS"));

        verify(adminPartnersService, times(1)).deletePartners(partners);
    }

    @Test
    @DisplayName("파트너스 삭제 실패 - NotFound")
    void deletePartners_notFound() throws Exception {
        // given
        when(adminPartnersService.findByPk(999L)).thenReturn(null);

        // when & then
        mockMvc.perform(delete("/admin/partners/{pk}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND_PARTNERS"));

        verify(adminPartnersService, never()).deletePartners(any());
    }
    //endregion

    //region 파트너스 수정
    @Test
    @DisplayName("파트너스 수정 성공")
    void patchPartners_success() throws Exception {
        // given
        Partners partners = new Partners();
        UpdatePartnersRequest request = new UpdatePartnersRequest();
        request.setName("새 이름");

        PartnersDetailDTO responseDTO = new PartnersDetailDTO();
        responseDTO.setName("새 이름");

        when(adminPartnersService.findByPk(1L)).thenReturn(partners);
        when(adminPartnersService.updatePartners(eq(partners), any(UpdatePartnersRequest.class)))
                .thenReturn(responseDTO);

        // when & then
        mockMvc.perform(patch("/admin/partners/{pk}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("GET_SUCCESS"))
                .andExpect(jsonPath("$.data.name").value("새 이름"));
    }

    @Test
    @DisplayName("파트너스 수정 실패 - NotFound")
    void patchPartners_notFound() throws Exception {
        // given
        UpdatePartnersRequest request = new UpdatePartnersRequest();
        request.setName("수정 시도");

        when(adminPartnersService.findByPk(999L)).thenReturn(null);

        // when & then
        mockMvc.perform(patch("/admin/partners/{pk}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND_PARTNERS"));

        verify(adminPartnersService, never()).updatePartners(any(), any());
    }
    //endregion
}
