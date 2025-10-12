package kr.kickon.api.domain.newsReply;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.kickon.api.domain.news.NewsService;
import kr.kickon.api.domain.newsReply.dto.PaginatedNewsReplyListDTO;
import kr.kickon.api.domain.newsReply.dto.ReplyDTO;
import kr.kickon.api.domain.newsReply.request.CreateNewsReplyRequest;
import kr.kickon.api.domain.newsReply.request.GetNewsRepliesRequest;
import kr.kickon.api.domain.newsReply.request.PatchNewsReplyRequest;
import kr.kickon.api.domain.newsReply.response.GetNewsRepliesResponse;
import kr.kickon.api.domain.user.UserService;
import kr.kickon.api.domain.userFavoriteTeam.UserFavoriteTeamService;
import kr.kickon.api.global.auth.jwt.user.JwtTokenProvider;
import kr.kickon.api.global.common.PagedMetaDTO;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.entities.*;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.error.exceptions.ForbiddenException;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/news-reply")
@Tag(name = "뉴스 댓글")
@Slf4j
public class NewsReplyController {
    private final JwtTokenProvider jwtTokenProvider;
    private final NewsReplyService newsReplyService;
    private final UserFavoriteTeamService userFavoriteTeamService;
    private final NewsService newsService;
    private final UserService userService;

    @Operation(summary = "뉴스 댓글 생성", description = "회원가입한 유저만 뉴스 댓글 생성 가능")
    @PostMapping()
    public ResponseEntity<ResponseDTO<ReplyDTO>> createNewsReply(@Valid @RequestBody CreateNewsReplyRequest request){
        User user = jwtTokenProvider.getUserFromSecurityContext();
        user = userService.findByPk(user.getPk());
        News news = newsService.findByPk(request.getNews());
        if(news == null) throw new NotFoundException(ResponseCode.NOT_FOUND_NEWS);
        List<UserFavoriteTeam> userFavoriteTeams = userFavoriteTeamService.findAllByUserPk(user.getPk());

        if (news.getTeam() != null) {
            boolean hasTeam = userFavoriteTeams.stream()
                    .anyMatch(uft -> uft.getTeam().getPk().equals(news.getTeam().getPk()));
            if (!hasTeam) {
                throw new ForbiddenException(ResponseCode.FORBIDDEN);
            }
        }
        NewsReply.NewsReplyBuilder newsReplyBuilder = NewsReply.builder()
                .user(user)
                .contents(request.getContents())
                .news(news);

        NewsReply parentNewsReply = null;

        if (request.getParentReply() != null) {
            parentNewsReply = newsReplyService.findByPk(request.getParentReply());
            if (parentNewsReply == null) throw new NotFoundException(ResponseCode.NOT_FOUND_NEWS);
            newsReplyBuilder.parentNewsReply(parentNewsReply);
        }

        NewsReply newsReply = newsReplyBuilder.build();

        newsReply = newsReplyService.createNewsReplyWithImages(newsReply, request.getUsedImageKeys());

        newsReplyService.sendReplyNotification(news, parentNewsReply, user);

        // 생성된 댓글을 ReplyDTO로 변환하여 반환
        ReplyDTO replyDTO = newsReplyService.convertToReplyDTO(newsReply, user.getPk());
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS, replyDTO));
    }


    @Operation(summary = "뉴스 댓글 리스트 조회", description = "뉴스 댓글 페이징 처리해서 전달")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = GetNewsRepliesResponse.class))),
    })
    @GetMapping()
    public ResponseEntity<ResponseDTO<List<ReplyDTO>>> getNewsReplies(@Valid GetNewsRepliesRequest query){
        User user = jwtTokenProvider.getUserFromSecurityContext();
        News newsData = newsService.findByPk(query.getNews());
        if(newsData == null) throw new NotFoundException(ResponseCode.NOT_FOUND_NEWS);
        PaginatedNewsReplyListDTO paginatedReplyListDTO = newsReplyService.getReplyListByNews(query.getNews(),user!=null ? user.getPk() : null, query.getPage(), query.getSize(), query.getInfinite() != null ? query.getInfinite() : null, query.getLastReply());
        if(paginatedReplyListDTO.getHasNext()!=null){
            return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS,paginatedReplyListDTO.getReplyList(),
                    new PagedMetaDTO(paginatedReplyListDTO.getHasNext())));
        }else{
            return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS,paginatedReplyListDTO.getReplyList(),
                    new PagedMetaDTO(paginatedReplyListDTO.getCurrentPage(), paginatedReplyListDTO.getPageSize(), paginatedReplyListDTO.getTotalItems())));
        }
    }

    @Operation(summary = "뉴스 댓글 삭제", description = "뉴스 댓글 PK값으로 댓글 삭제")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공"),
    })
    @DeleteMapping("/{newsReplyPk}")
    public ResponseEntity<ResponseDTO> deleteNewsReplies(@PathVariable Long newsReplyPk){
        User user = jwtTokenProvider.getUserFromSecurityContext();
        NewsReply newsReplyData = newsReplyService.findByPk(newsReplyPk);
        if(newsReplyData == null) throw new NotFoundException(ResponseCode.NOT_FOUND_NEWS_REPLY);
        if (!newsReplyData.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException(ResponseCode.FORBIDDEN);
        }        newsReplyService.deleteNewsReply(newsReplyData);
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS));
    }

    @Operation(summary = "뉴스 댓글 수정", description = "뉴스 댓글 PK값으로 댓글 수정")
    @PatchMapping("/{newsReplyPk}")
    public ResponseEntity<ResponseDTO<ReplyDTO>> patchNewsReply(@PathVariable Long newsReplyPk,
        @Valid @RequestBody PatchNewsReplyRequest request){
        User user = jwtTokenProvider.getUserFromSecurityContext();
        NewsReply newsReplyData = newsReplyService.findByPk(newsReplyPk);
        if(newsReplyData == null) throw new NotFoundException(ResponseCode.NOT_FOUND_NEWS_REPLY);
        if (!newsReplyData.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException(ResponseCode.FORBIDDEN);
        }
        newsReplyData.setContents(request.getContents());

        newsReplyService.updateNewsReply(newsReplyData);
        
        // 수정된 댓글을 ReplyDTO로 변환하여 반환
        ReplyDTO replyDTO = newsReplyService.convertToReplyDTO(newsReplyData, user.getPk());
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS, replyDTO));
    }
}
