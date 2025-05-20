package kr.kickon.api.admin.auth.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.admin.auth.dto.LoginResponseDTO;
import kr.kickon.api.global.common.ResponseDTO;

@Schema(name = "PostLoginResponse", description = "로그인 응답")
public class PostLoginResponse extends ResponseDTO<LoginResponseDTO> {
}
