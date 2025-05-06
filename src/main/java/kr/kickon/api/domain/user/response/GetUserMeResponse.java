package kr.kickon.api.domain.user.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.domain.user.dto.UserMeDto;
import kr.kickon.api.global.common.ResponseDTO;

@Schema(name = "GetUserMeResponse", description = "내 정보 조회 응답")
public class GetUserMeResponse extends ResponseDTO<UserMeDto> {
}