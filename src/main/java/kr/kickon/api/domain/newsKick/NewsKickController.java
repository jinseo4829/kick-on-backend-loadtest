package kr.kickon.api.domain.newsKick;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.kickon.api.domain.news.NewsService;
import kr.kickon.api.domain.newsKick.request.CreateNewsKickRequestDTO;
import kr.kickon.api.global.auth.jwt.user.JwtTokenProvider;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.entities.NewsKick;
import kr.kickon.api.global.common.entities.News;
import kr.kickon.api.global.common.entities.User;
import kr.kickon.api.global.common.enums.DataStatus;
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
@RequestMapping("/api/news-kick")
@Tag(name = "뉴스 킥 관련")
@Slf4j
public class NewsKickController {
    private final JwtTokenProvider jwtTokenProvider;
    private final UUIDGenerator uuidGenerator;
    private final NewsService newsService;
    private final NewsKickService newsKickService;

    @Operation(summary = "뉴스 킥 생성 및 삭제", description = "PK값 옵셔널인데 넘기면 그거 기반으로 삭제 할거임!")
    @PostMapping()
    public ResponseEntity<ResponseDTO<Void>> createNewsKick(@RequestBody @Valid CreateNewsKickRequestDTO body){
        User user = jwtTokenProvider.getUserFromSecurityContext();

        News news = newsService.findByPk(body.getNews());

        if(news==null) throw new NotFoundException(ResponseCode.NOT_FOUND_BOARD);


        // 게시글 킥 이미 있는지 체크
        NewsKick newsKick = newsKickService.findByNewsAndUser(news.getPk(), user.getPk());
        if(newsKick==null){
            String id = uuidGenerator.generateUniqueUUID(newsService::findById);
            newsKickService.save(NewsKick.builder()
                    .id(id)
                    .news(news)
                    .user(user).build());
        }else{
            newsKick.setStatus(newsKick.getStatus().equals(DataStatus.ACTIVATED) ? DataStatus.DEACTIVATED : DataStatus.ACTIVATED);
            newsKickService.save(newsKick);
        }
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS));
    }
}
