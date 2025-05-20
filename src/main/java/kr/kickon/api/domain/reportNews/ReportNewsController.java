package kr.kickon.api.domain.reportNews;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.kickon.api.domain.news.NewsService;
import kr.kickon.api.domain.reportNews.request.CreateReportNewsRequestDTO;
import kr.kickon.api.global.auth.jwt.user.JwtTokenProvider;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.entities.News;
import kr.kickon.api.global.common.entities.ReportNews;
import kr.kickon.api.global.common.entities.User;
import kr.kickon.api.global.common.enums.ReportStatus;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import kr.kickon.api.global.util.UUIDGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/report-news")
@Tag(name = "뉴스 신고 관련")
@Slf4j
public class ReportNewsController {
    private final JwtTokenProvider jwtTokenProvider;
    private final UUIDGenerator uuidGenerator;
    private final NewsService newsService;
    private final ReportNewsService reportNewsService;

    @Operation(summary = "뉴스 신고 생성", description = "뉴스 pk, 신고 사유 필수!")
    @PostMapping()
    public ResponseEntity<ResponseDTO<Void>> createReportNews(@RequestBody CreateReportNewsRequestDTO body){
        User user = jwtTokenProvider.getUserFromSecurityContext();

        News news = newsService.findByPk(body.getNews());

        if(news==null) throw new NotFoundException(ResponseCode.NOT_FOUND_BOARD);

        String id = uuidGenerator.generateUniqueUUID(reportNewsService::findById);
        reportNewsService.save(ReportNews.builder()
                .id(id)
                .reportedNews(news)
                .reportStatus(ReportStatus.REPORTED)
                .user(user)
                .reason(body.getReason())
                .build());
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS));
    }
}
