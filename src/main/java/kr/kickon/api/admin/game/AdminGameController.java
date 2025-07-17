package kr.kickon.api.admin.game;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.kickon.api.admin.game.dto.GameDetailDTO;
import kr.kickon.api.admin.game.dto.GameListDTO;
import kr.kickon.api.admin.game.request.GameFilterRequest;
import kr.kickon.api.admin.game.request.GameUpdateRequest;
import kr.kickon.api.admin.game.response.GetGameDetailResponse;
import kr.kickon.api.admin.game.response.GetGamesResponse;
import kr.kickon.api.domain.game.GameService;
import kr.kickon.api.global.common.PagedMetaDTO;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.entities.Game;
import kr.kickon.api.global.common.enums.ResponseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/game")
@Tag(name = "게임")
@Slf4j
public class AdminGameController {
    private final GameService gameService;

    @GetMapping
    @Operation(summary = "게임 리스트 조회", description = "게임 리스트를 조회합니다. 각 filter 조건은 옵셔널 입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = GetGamesResponse.class))),
    })
    public ResponseEntity<ResponseDTO<List<GameListDTO>>> getFilteredGames(@Valid @ModelAttribute GameFilterRequest request) {
        Pageable pageable = request.toPageable();
        Page<Game> gamePage = gameService.getGameListByFilter(request, pageable);
        List<GameListDTO> dtos = gameService.fromGameList(gamePage.getContent());

        return ResponseEntity.ok(
                ResponseDTO.success(
                        ResponseCode.SUCCESS,
                        dtos,
                        new PagedMetaDTO(
                                gamePage.getNumber() + 1, // 0-based → 1-based
                                gamePage.getSize(),
                                gamePage.getTotalElements()
                        )
                )
        );
    }

    @GetMapping("/{gamePk}")
    @Operation(summary = "게임 상세 조회", description = "게임의 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = GetGameDetailResponse.class))),
    })
    public ResponseEntity<ResponseDTO<GameDetailDTO>> getGameDetail(@PathVariable Long gamePk) {
        GameDetailDTO response = gameService.getGameDetail(gamePk);
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS,response));
    }

    @PatchMapping("/{gamePk}")
    @Operation(summary = "게임 정보 수정", description = "특정 게임의 일부 정보를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = ResponseDTO.class))),
    })
    public ResponseEntity<ResponseDTO<Void>> updateGame(
            @PathVariable Long gamePk,
            @RequestBody @Valid GameUpdateRequest request) {
        gameService.updateGame(gamePk, request);
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS));
    }
}
