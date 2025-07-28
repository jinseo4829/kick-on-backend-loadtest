package kr.kickon.api.admin.gambleSeason;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import kr.kickon.api.admin.gambleSeason.dto.GambleSeasonDetailDTO;
import kr.kickon.api.admin.gambleSeason.dto.SeasonListDTO;
import kr.kickon.api.admin.gambleSeason.request.CreateGambleSeasonRequest;
import kr.kickon.api.admin.gambleSeason.request.GambleSeasonFilterRequest;
import kr.kickon.api.admin.gambleSeason.request.UpdateGambleSeasonRequest;
import kr.kickon.api.admin.gambleSeason.response.GetGambleSeasonDetailResponse;
import kr.kickon.api.admin.gambleSeason.response.GetGambleSeasonResponse;
import kr.kickon.api.global.common.PagedMetaDTO;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.entities.GambleSeason;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/gambleSeason")
@Tag(name = "승부 예측 시즌 관리")
@Slf4j
public class AdminGambleSeasonController {
  private final AdminGambleSeasonService adminGambleSeasonService;
  @GetMapping
  @Operation(summary = "승부 예측 시즌 리스트 조회", description = "승부 예측 리스트를 조회합니다. 각 filter 조건은 옵셔널 입니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "성공",
          content = @Content(schema = @Schema(implementation = GetGambleSeasonResponse.class))),
  })
  public ResponseEntity<ResponseDTO<List<SeasonListDTO>>> getFilteredGambleSeasons(@Valid @ModelAttribute GambleSeasonFilterRequest request) {
    Pageable pageable = request.toPageable();
    Page<SeasonListDTO> GambleSeasonPage = adminGambleSeasonService.getGambleSeasonListByFilter(request, pageable);

    return ResponseEntity.ok(
        ResponseDTO.success(
            ResponseCode.SUCCESS,
            GambleSeasonPage.getContent(),
            new PagedMetaDTO(
                GambleSeasonPage.getNumber() + 1,
                GambleSeasonPage.getSize(),
                GambleSeasonPage.getTotalElements()
            )
        )
    );
  }

  @GetMapping("/{pk}")
  @Operation(summary = "승부 예측 시즌 상세 조회", description = "pk값으로 승부 예측 시즌의 상세 정보를 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "성공",
          content = @Content(schema = @Schema(implementation = GetGambleSeasonDetailResponse.class))),
  })
  public ResponseEntity<ResponseDTO<GambleSeasonDetailDTO>> getGambleSeasonDetail(@PathVariable Long pk) {
    GambleSeason gambleSeason = adminGambleSeasonService.findByPk(pk);
    if (gambleSeason == null) throw new NotFoundException(ResponseCode.NOT_FOUND_GAMBLE_SEASON);
    GambleSeasonDetailDTO dto = adminGambleSeasonService.getGambleSeasonDetail(gambleSeason);

    return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS, dto));
  }

  @PostMapping
  @Operation(summary = "승부 예측 시즌 생성", description = "승부 예측 시즌을 생성합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "성공",
          content = @Content(schema = @Schema(implementation = GetGambleSeasonDetailResponse.class))),
  })
  public ResponseEntity<ResponseDTO<GambleSeasonDetailDTO>> createGambleSeason(@RequestBody CreateGambleSeasonRequest request) {
    GambleSeasonDetailDTO responseDto = adminGambleSeasonService.createGambleSeason(request);
    return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS, responseDto));
  }

  @PatchMapping("/{pk}")
  @Operation(summary = "승부 예측 시즌 수정", description = "pk값으로 승부 예측 시즌을 수정합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "성공",
          content = @Content(schema = @Schema(implementation = GetGambleSeasonDetailResponse.class))),
  })
  public ResponseEntity<ResponseDTO<GambleSeasonDetailDTO>> patchGambleSeason(@PathVariable Long pk,
      @RequestBody UpdateGambleSeasonRequest request) {
    GambleSeason gambleSeason = adminGambleSeasonService.findByPk(pk);
    if (gambleSeason == null) throw new NotFoundException(ResponseCode.NOT_FOUND_GAMBLE_SEASON);
    GambleSeasonDetailDTO responseDto = adminGambleSeasonService.updateGambleSeason(gambleSeason, request);
    return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS, responseDto));
  }
}
