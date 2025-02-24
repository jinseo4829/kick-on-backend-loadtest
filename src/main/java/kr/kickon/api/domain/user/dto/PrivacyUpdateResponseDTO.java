package kr.kickon.api.domain.user.dto;

import kr.kickon.api.global.common.ResponseDTO;


// 예시 Swagger Dto
public class PrivacyUpdateResponseDTO extends ResponseDTO<PrivacyUpdateRequest> {
    public PrivacyUpdateResponseDTO(PrivacyUpdateRequest data) {
        super.setData(data);
    }
}
