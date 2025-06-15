package kr.kickon.api.admin.news;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.kickon.api.admin.news.response.AdminGetNewsDetailResponse;
import kr.kickon.api.domain.news.NewsService;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.entities.News;
import kr.kickon.api.global.common.enums.ResponseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/news")
@Tag(name = "뉴스")
@Slf4j
public class AdminNewsController {
    private final NewsService newsService;

    @Operation(summary = "뉴스 상세 조회", description = "뉴스 PK 값으로 뉴스 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = AdminGetNewsDetailResponse.class))),
    })
    @GetMapping("/{newsPk}")
    public ResponseEntity<ResponseDTO<News>> getNewsDetail(@PathVariable Long newsPk){
        News news = newsService.findByPk(newsPk);
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS, news));
    }
}
