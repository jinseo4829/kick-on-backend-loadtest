package kr.kickon.api.domain.shorts;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import kr.kickon.api.domain.shorts.dto.ShortsDTO;
import kr.kickon.api.domain.shorts.dto.ShortsDetailDTO;
import kr.kickon.api.domain.shorts.request.GetShortsRequest;
import kr.kickon.api.domain.shorts.response.GetShortsDetailResponse;
import kr.kickon.api.domain.shorts.response.GetShortsResponse;
import kr.kickon.api.global.common.PagedMetaDTO;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.entities.Shorts;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.common.enums.ShortsSortType;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/shorts")
@Tag(name = "쇼츠 관련")
@Slf4j
public class ShortsController {

  private final ShortsService shortsService;

  @GetMapping("/fixed")
  @Operation(summary = "고정 쇼츠 리스트 조회", description = "고정 쇼츠 리스트를 조회합니다. 1)48시간 조회수 2)48시간 기준 킥 수 3)최신순 기준으로 정렬 후 상위 4개만 반환합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "성공",
          content = @Content(schema = @Schema(implementation = GetShortsResponse.class))),
  })
  public ResponseEntity<ResponseDTO<List<ShortsDTO>>> getFixedShorts() {
    List<ShortsDTO> dto = shortsService.getFixedShorts();
    return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS, dto));
  }

  @GetMapping
  @Operation(summary = "쇼츠 리스트 조회", description = "쇼츠 리스트를 조회합니다. 최신순(CREATED_DESC)/인기순(POPULAR)/등록순(CREATED_ASC)으로 정렬 가능합니다. default는 최신순입니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "성공",
          content = @Content(schema = @Schema(implementation = GetShortsResponse.class))),
  })
  public ResponseEntity<ResponseDTO<List<ShortsDTO>>> getShorts(@Valid @ModelAttribute GetShortsRequest request) {
    Pageable pageable = request.toPageable();
    Page<ShortsDTO> shortsPage = shortsService.getShortsWithPagination(request, pageable);
    PagedMetaDTO meta = PagedMetaDTO.builder()
        .currentPage(shortsPage.getNumber() + 1)  // 1-based
        .pageSize(shortsPage.getSize())
        .totalItems(shortsPage.getTotalElements())
        .totalPages(shortsPage.getTotalPages())
        .hasNext(shortsPage.hasNext())
        .build();
    return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS, shortsPage.getContent(), meta));
  }

  @GetMapping("/{pk}")
  @Operation(summary = "쇼츠 상세 조회", description = "pk값으로 쇼츠를 조회합니다. 최신순, 인기순, 등록순으로 다음 영상 pk 하나를 가져옵니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "성공",
          content = @Content(schema = @Schema(implementation = GetShortsDetailResponse.class))),
  })
  public ResponseEntity<ResponseDTO<ShortsDetailDTO>> getShortsDetail(@PathVariable Long pk, @RequestParam(defaultValue = "CREATED_DESC") ShortsSortType sort) {
    Shorts file = shortsService.findByPk(pk);
    if (file == null) throw new NotFoundException(ResponseCode.NOT_FOUND_SHORTS);

    ShortsDetailDTO dto = shortsService.getShortsDetail(file, sort);
    return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS, dto));
  }
}
