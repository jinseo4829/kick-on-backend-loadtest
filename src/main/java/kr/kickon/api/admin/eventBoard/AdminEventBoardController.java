package kr.kickon.api.admin.eventBoard;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.kickon.api.admin.eventBoard.request.CreateEventBoardRequest;
import kr.kickon.api.admin.eventBoard.request.UpdateBannerOrderRequest;
import kr.kickon.api.admin.eventBoard.request.UpdateEventBoardRequest;
import kr.kickon.api.admin.eventBoard.response.GetEventBoardsResponse;
import kr.kickon.api.domain.actualSeasonRanking.response.GetActualSeasonRankingResponse;
import kr.kickon.api.domain.eventBoard.EventBoardService;
import kr.kickon.api.global.common.PagedMetaDTO;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.entities.EventBoard;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.error.exceptions.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/event-board")
@Tag(name = "이벤트 배너")
@Slf4j
public class AdminEventBoardController {
    private final EventBoardService eventBoardService;
    @Operation(summary = "이벤트 배너 순서 조정", description = "order는 확실하게 unique해야함...!!!")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = ResponseDTO.class))),
    })
    @PatchMapping("/order")
    public ResponseEntity<ResponseDTO<Void>> updateOrder(@RequestBody @Valid UpdateBannerOrderRequest request) {
        eventBoardService.updateOrder(request.getOrders());
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS));
    }

    @Operation(summary = "이벤트 배너 삭제", description = "pk 기준으로 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = ResponseDTO.class))),
    })
    @DeleteMapping("/{pk}")
    public ResponseEntity<ResponseDTO<Void>> delete(@PathVariable Long pk) {
        eventBoardService.softDelete(pk);
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS));
    }

    @Operation(summary = "이벤트 배너 수정", description = "각 값은 옵셔널로 요청하는 것만 수정 예정")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = ResponseDTO.class))),
    })
    @PatchMapping("/{pk}")
    public ResponseEntity<ResponseDTO<Void>> update(@PathVariable Long pk, @RequestBody @Valid UpdateEventBoardRequest request) {
        if(request.getIsDisplayed() && request.getOrderNum()==null) throw new BadRequestException(ResponseCode.INVALID_ADMIN_EVENT_BOARD_UPDATE_REQUEST);
        eventBoardService.update(pk, request);
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS));
    }

    @Operation(summary = "이벤트 배너 리스트 조회", description = "isDisplayed 기준으로 분기처리 (boolean)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = GetEventBoardsResponse.class))),
    })
    @GetMapping
    public ResponseEntity<ResponseDTO<List<EventBoard>>> list(@RequestParam(required = false) Boolean isDisplayed,
                                                              @RequestParam(defaultValue = "1") int page,
                                                              @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page-1, size);

        Page<EventBoard> result = eventBoardService.list(isDisplayed, pageable);
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS, result.getContent(),new PagedMetaDTO(
                result.getNumber() + 1, // 0-based → 1-based
                result.getSize(),
                result.getTotalElements()
        )));
    }

    @Operation(summary = "이벤트 배너 생성")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = ResponseDTO.class))),
    })
    @PostMapping
    public ResponseEntity<ResponseDTO<Void>> create(@RequestBody @Valid CreateEventBoardRequest request) {
        eventBoardService.create(request);
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS));
    }
}
