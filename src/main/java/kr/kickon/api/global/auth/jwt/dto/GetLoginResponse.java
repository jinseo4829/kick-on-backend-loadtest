package kr.kickon.api.global.auth.jwt.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.global.common.ResponseDTO;

@Schema(description = "로그인 응답")
public class GetLoginResponse extends ResponseDTO<TokenDto> {
}
