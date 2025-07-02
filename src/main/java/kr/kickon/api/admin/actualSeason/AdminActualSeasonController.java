package kr.kickon.api.admin.actualSeason;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import kr.kickon.api.admin.actualSeason.dto.ActualSeasonDetailDTO;
import kr.kickon.api.admin.actualSeason.request.ActualSeasonFilterRequest;
import kr.kickon.api.admin.actualSeason.request.PatchActualSeasonRequestDTO;
import kr.kickon.api.admin.actualSeason.response.GetActualSeasonDetailResponse;
import kr.kickon.api.admin.gambleSeason.dto.GambleSeasonDetailDTO;
import kr.kickon.api.admin.gambleSeason.dto.GambleSeasonListDTO;
import kr.kickon.api.admin.gambleSeason.request.PatchGambleSeasonRequestDTO;
import kr.kickon.api.admin.gambleSeason.response.GetGambleSeasonDetailResponse;
import kr.kickon.api.admin.gambleSeason.response.GetGambleSeasonResponse;
import kr.kickon.api.global.common.PagedMetaDTO;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.entities.ActualSeason;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/actualSeason")
@Tag(name = "실제 시즌 관리")
@Slf4j
public class AdminActualSeasonController {
  private final AdminActualSeasonService adminActualSeasonService;
  @GetMapping
  @Operation(summary = "실제 시즌 리스트 조회", description = "실제 시즌 리스트를 조회합니다. 각 filter 조건은 옵셔널 입니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "성공",
          content = @Content(schema = @Schema(implementation = GetGambleSeasonResponse.class))),
  })
  public ResponseEntity<ResponseDTO<List<GambleSeasonListDTO>>> getFilteredActualSeasons(@Valid @ModelAttribute ActualSeasonFilterRequest request) {
    Pageable pageable = request.toPageable();
    Page<GambleSeasonListDTO> ActualSeasonPage = adminActualSeasonService.findActualSeasonByFilter(request, pageable);

    return ResponseEntity.ok(
        ResponseDTO.success(
            ResponseCode.SUCCESS,
            ActualSeasonPage.getContent(),
            new PagedMetaDTO(
                ActualSeasonPage.getNumber() + 1,
                ActualSeasonPage.getSize(),
                ActualSeasonPage.getTotalElements()
            )
        )
    );
  }

  @GetMapping("/{pk}")
  @Operation(summary = "실제 시즌 상세 조회", description = "pk값으로 실제 시즌의 상세 정보를 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "성공",
          content = @Content(schema = @Schema(implementation = GetActualSeasonDetailResponse.class))),
  })
  public ResponseEntity<ResponseDTO<ActualSeasonDetailDTO>> getActualSeasonDetail(@PathVariable Long pk) {
    ActualSeason actualSeason = adminActualSeasonService.findByPk(pk);
    if (actualSeason == null) throw new NotFoundException(ResponseCode.NOT_FOUND_ACTUAL_SEASON);
    ActualSeasonDetailDTO dto = adminActualSeasonService.getActualSeasonDetail(actualSeason);

    return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS, dto));
  }

  @PatchMapping("/{pk}")
  @Operation(summary = "시즌 수정", description = "pk값으로 시즌을 수정합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "성공",
          content = @Content(schema = @Schema(implementation = GetActualSeasonDetailResponse.class))),
  })
  public ResponseEntity<ResponseDTO<ActualSeasonDetailDTO>> patchActualSeason(@PathVariable Long pk,
      @RequestBody PatchActualSeasonRequestDTO request) {
    ActualSeason actualSeason = adminActualSeasonService.findByPk(pk);
    if (actualSeason == null) throw new NotFoundException(ResponseCode.NOT_FOUND_ACTUAL_SEASON);
    ActualSeasonDetailDTO responseDto = adminActualSeasonService.patchActualSeason(actualSeason, request);
    return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS, responseDto));
  }
}
