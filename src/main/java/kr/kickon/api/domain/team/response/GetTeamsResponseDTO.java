package kr.kickon.api.domain.team.response;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.domain.team.dto.TeamDTO;
import kr.kickon.api.global.common.ResponseDTO;

import java.util.List;

@Schema(description = "팀 리스트 조회 응답")
public class GetTeamsResponseDTO extends ResponseDTO<List<TeamDTO>> {
}
