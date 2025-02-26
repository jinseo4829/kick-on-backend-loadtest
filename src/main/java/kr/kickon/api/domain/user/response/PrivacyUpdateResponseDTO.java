package kr.kickon.api.domain.user.response;

import kr.kickon.api.domain.user.request.PrivacyUpdateRequest;
import kr.kickon.api.global.common.ResponseDTO;


// 예시 Swagger Dto
public class PrivacyUpdateResponseDTO extends ResponseDTO<PrivacyUpdateRequest> {
    public PrivacyUpdateResponseDTO(PrivacyUpdateRequest data) {
        super.setData(data);
    }
}

