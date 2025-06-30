package kr.kickon.api.admin.gambleSeason;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import kr.kickon.api.admin.gambleSeason.dto.GambleSeasonListDTO;
import kr.kickon.api.admin.gambleSeason.request.GambleSeasonFilterRequest;
import kr.kickon.api.admin.gambleSeason.response.GetGambleSeasonResponse;
import kr.kickon.api.global.common.PagedMetaDTO;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.enums.ResponseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
  @Operation(summary = "승부 예측 시즌 리스트 조회", description = "승부 예측 리스를 조회합니다. 각 filter 조건은 옵셔널 입니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "성공",
          content = @Content(schema = @Schema(implementation = GetGambleSeasonResponse.class))),
  })
  public ResponseEntity<ResponseDTO<List<GambleSeasonListDTO>>> getFilteredGambleSeasons(@Valid @ModelAttribute GambleSeasonFilterRequest request) {
    Pageable pageable = request.toPageable();
    Page<GambleSeasonListDTO> GambleSeasonPage = adminGambleSeasonService.findGambleSeasonByFilter(request, pageable);

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
}
