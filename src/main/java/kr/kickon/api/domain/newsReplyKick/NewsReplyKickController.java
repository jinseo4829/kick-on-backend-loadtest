package kr.kickon.api.domain.newsReplyKick;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.kickon.api.domain.newsReply.NewsReplyService;
import kr.kickon.api.domain.newsReplyKick.NewsReplyKickService;
import kr.kickon.api.domain.newsReplyKick.request.CreateNewsReplyKickRequestDTO;
import kr.kickon.api.global.auth.jwt.JwtTokenProvider;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.entities.NewsReply;
import kr.kickon.api.global.common.entities.NewsReplyKick;
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
@RequestMapping("/api/news-reply-kick")
@Tag(name = "뉴스 댓글 킥 관련")
@Slf4j
public class NewsReplyKickController {
    private final JwtTokenProvider jwtTokenProvider;
    private final UUIDGenerator uuidGenerator;
    private final NewsReplyService newsReplyService;
    private final NewsReplyKickService newsReplyKickService;

    @Operation(summary = "뉴스 댓글 킥 생성 및 삭제", description = "댓글 PK값 옵셔널인데 넘기면 그거 기반으로 삭제 할거임!")
    @PostMapping()
    public ResponseEntity<ResponseDTO<Void>> createNewsReplyKick(@RequestBody CreateNewsReplyKickRequestDTO body){
        User user = jwtTokenProvider.getUserFromSecurityContext();

        NewsReply newsReply = newsReplyService.findByPk(body.getReply());

        if(newsReply==null) throw new NotFoundException(ResponseCode.NOT_FOUND_NEWS_REPLY);

        String id = uuidGenerator.generateUniqueUUID(newsReplyService::findById);
        newsReplyKickService.save(NewsReplyKick.builder()
                .id(id)
                .newsReply(newsReply)
                .user(user).build());
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS));
    }
}
