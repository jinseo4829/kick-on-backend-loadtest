package kr.kickon.api.domain.user.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.domain.user.dto.GetUserMeDTO;
import kr.kickon.api.global.common.ResponseDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(name = "GetUserMeResponseDTO", description = "내 정보 조회 성공 response dto")
public class GetUserMeResponseDTO extends ResponseDTO<GetUserMeDTO> {
    public GetUserMeResponseDTO(GetUserMeDTO data) {
        super.setData(data);
    }
}
