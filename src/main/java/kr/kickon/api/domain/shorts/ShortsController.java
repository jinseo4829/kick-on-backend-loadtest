package kr.kickon.api.domain.shorts;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import kr.kickon.api.domain.awsFileReference.AwsFileReferenceService;
import kr.kickon.api.domain.shorts.dto.ShortsDTO;
import kr.kickon.api.domain.shorts.dto.ShortsDetailDTO;
import kr.kickon.api.domain.shorts.request.GetShortsRequest;
import kr.kickon.api.domain.shorts.response.GetShortsDetailResponse;
import kr.kickon.api.domain.shorts.response.GetShortsResponse;
import kr.kickon.api.global.common.PagedMetaDTO;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.entities.AwsFileReference;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.error.exceptions.BadRequestException;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/shorts")
@Tag(name = "쇼츠 관련")
@Slf4j
public class ShortsController {

  private final ShortsService shortsService;
  private final AwsFileReferenceService awsFileReferenceService;

  @GetMapping("/fixed")
  @Operation(summary = "고정 쇼츠 리스트 조회", description = "고정 쇼츠 리스트를 조회합니다. 1)48시간 조회수 2)48시간 기준 킥 수 기준으로 정렬 후 상위 4개만 반환합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "성공",
          content = @Content(schema = @Schema(implementation = GetShortsResponse.class))),
  })
  public ResponseEntity<ResponseDTO<List<ShortsDTO>>> getFixedShorts() {
    List<ShortsDTO> dto = shortsService.getFixedShorts();
    return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS, dto));
  }

  @GetMapping
  @Operation(summary = "쇼츠 리스트 조회", description = "쇼츠 리스트를 조회합니다. 최신순/인기순/등록순으로 정렬 가능합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "성공",
          content = @Content(schema = @Schema(implementation = GetShortsResponse.class))),
  })
  public ResponseEntity<ResponseDTO<List<ShortsDTO>>> getShorts(@Valid @ModelAttribute GetShortsRequest request) {
    Pageable pageable = request.toPageable();
    Page<ShortsDTO> shortsPage = shortsService.getShorts(request, pageable);
    return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS, shortsPage.getContent(),
        new PagedMetaDTO(
        shortsPage.getNumber() + 1,
        shortsPage.getSize(),
        shortsPage.getTotalElements()
    )));
  }

  @GetMapping("/{pk}")
  @Operation(summary = "쇼츠 상세 조회", description = "pk값으로 쇼츠를 조회합니다. 영상 파일만 조회 가능합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "성공",
          content = @Content(schema = @Schema(implementation = GetShortsDetailResponse.class))),
  })
  public ResponseEntity<ResponseDTO<ShortsDetailDTO>> getShortsDetail(@PathVariable Long pk) {
    AwsFileReference file = awsFileReferenceService.findByPk(pk);
    if (file == null) throw new NotFoundException(ResponseCode.NOT_FOUND_AWS_FILE);
    if (!file.getS3Key().matches(".*\\.(mp4|mov|avi|mkv)$")) {
      throw new BadRequestException(ResponseCode.INVALID_REQUEST);
    }

    ShortsDetailDTO dto = shortsService.getShortsDetail(file);
    return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS, dto));
  }
}
