package kr.kickon.api.admin.userGameGamble;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.kickon.api.admin.userGameGamble.dto.UserGameGambleDTO;
import kr.kickon.api.admin.userGameGamble.response.GetUserGameGamblesResponse;
import kr.kickon.api.domain.userGameGamble.UserGameGambleService;
import kr.kickon.api.global.common.PagedMetaDTO;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.entities.UserGameGamble;
import kr.kickon.api.global.common.enums.ResponseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/user-game-gamble")
@Tag(name = "유저 승부예측")
@Slf4j
public class AdminUserGameGambleController {
    private final UserGameGambleService userGameGambleService;

    @GetMapping("/{gamePk}/gambles")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = GetUserGameGamblesResponse.class)))
    })
    @Operation(summary = "게임 예측 목록 조회", description = "특정 게임에 대한 유저들의 예측 목록을 조회합니다.")
    public ResponseEntity<ResponseDTO<List<UserGameGambleDTO>>> getUserGamblesByGame(
            @PathVariable Long gamePk,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<UserGameGamble> pageResult = userGameGambleService.getUserGamblesByGame(gamePk, page-1, size);
        List<UserGameGambleDTO> dtos = toUserGameGambleDTO(pageResult.getContent());
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS, dtos, new PagedMetaDTO(page, size, (long) pageResult.getTotalPages())));
    }

    public List<UserGameGambleDTO> toUserGameGambleDTO(List<UserGameGamble> userGameGambles) {
        return userGameGambles
        .stream()
                .map(gamble -> UserGameGambleDTO.builder()
                        .pk(gamble.getPk())
                        .predictedHomeScore(gamble.getPredictedHomeScore())
                        .predictedAwayScore(gamble.getPredictedAwayScore())
                        .gambleStatus(gamble.getGambleStatus())
                        .user(UserGameGambleDTO.UserSummary.builder()
                                .pk(gamble.getUser().getPk())
                                .nickname(gamble.getUser().getNickname())
                                .build())
                        .supportingTeam(UserGameGambleDTO.TeamSummary.builder()
                                .pk(gamble.getSupportingTeam().getPk())
                                .nameKr(gamble.getSupportingTeam().getNameKr())
                                .nameEn(gamble.getSupportingTeam().getNameEn())
                                .build())
                        .build())
                .toList();
    }
}
