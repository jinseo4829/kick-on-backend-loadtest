package kr.kickon.api.domain.newsViewHistory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.kickon.api.domain.news.NewsService;
import kr.kickon.api.domain.newsViewHistory.request.CreateNewsViewHistory;
import kr.kickon.api.global.auth.jwt.user.JwtTokenProvider;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.entities.News;
import kr.kickon.api.global.common.entities.NewsViewHistory;
import kr.kickon.api.global.common.entities.User;
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
@RequestMapping("/api/news-view-history")
@Tag(name = "뉴스 뷰 관련")
@Slf4j
public class NewsViewHistoryController {
    private final JwtTokenProvider jwtTokenProvider;
    private final NewsViewHistoryService newsViewHistoryService;
    private final UUIDGenerator uuidGenerator;
    private final NewsService newsService;

    @Operation(summary = "뉴스 뷰 생성", description = "비회원도 생성 가능")
    @PostMapping()
    public ResponseEntity<ResponseDTO<Void>> createNewsView(@RequestBody CreateNewsViewHistory body){
        User user = jwtTokenProvider.getUserFromSecurityContext();

        News news = newsService.findByPk(body.getNews());

        if(news==null) throw new NotFoundException(ResponseCode.NOT_FOUND_BOARD);

        String id = uuidGenerator.generateUniqueUUID(newsViewHistoryService::findById);

        NewsViewHistory.NewsViewHistoryBuilder builder = NewsViewHistory.builder()
                .id(id)
                .news(news);

        if(user!=null) builder.user(user);
        newsViewHistoryService.save(builder.build());
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS));
    }
}
