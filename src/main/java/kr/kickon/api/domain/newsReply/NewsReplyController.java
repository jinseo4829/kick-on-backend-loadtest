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
import kr.kickon.api.domain.newsReply.request.CreateNewsReplyRequestDTO;
import kr.kickon.api.domain.newsReply.request.GetNewsRepliesRequestDTO;
import kr.kickon.api.domain.newsReply.request.PatchNewsReplyRequestDTO;
import kr.kickon.api.domain.newsReply.response.GetNewsRepliesResponseDTO;
import kr.kickon.api.domain.userFavoriteTeam.UserFavoriteTeamService;
import kr.kickon.api.global.auth.jwt.user.JwtTokenProvider;
import kr.kickon.api.global.common.PagedMetaDTO;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.entities.*;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.error.exceptions.ForbiddenException;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import kr.kickon.api.global.util.UUIDGenerator;
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
    private final UUIDGenerator uuidGenerator;
    private final NewsService newsService;

    @Operation(summary = "뉴스 댓글 생성", description = "회원가입한 유저만 뉴스 댓글 생성 가능")
    @PostMapping()
    public ResponseEntity<ResponseDTO<Void>> createNewsReply(@Valid @RequestBody CreateNewsReplyRequestDTO request){
        User user = jwtTokenProvider.getUserFromSecurityContext();
        News news = newsService.findByPk(request.getNews());
        if(news == null) throw new NotFoundException(ResponseCode.NOT_FOUND_NEWS);
        if(news.getTeam()!= null){
            UserFavoriteTeam userFavoriteTeam = userFavoriteTeamService.findByUserPk(user.getPk());
            if(userFavoriteTeam==null) throw new ForbiddenException(ResponseCode.FORBIDDEN);
            if(!userFavoriteTeam.getTeam().getPk().equals(news.getTeam().getPk())) throw new ForbiddenException(ResponseCode.FORBIDDEN);
        }
        String id = uuidGenerator.generateUniqueUUID(newsReplyService::findById);
        NewsReply.NewsReplyBuilder newsReplyBuilder = NewsReply.builder()
                .id(id)
                .user(user)
                .contents(request.getContents())
                .news(news);

        if(request.getParentReply()!=null) {
            NewsReply parentNewsReply = newsReplyService.findByPk(request.getParentReply());
            if(parentNewsReply == null) throw new NotFoundException(ResponseCode.NOT_FOUND_NEWS);
            newsReplyBuilder.parentNewsReply(parentNewsReply);
        }

        NewsReply newsReply = newsReplyBuilder.build();

        newsReply = newsReplyService.createNewsReplyWithImages(newsReply, request.getUsedImageKeys());
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS));
    }


    @Operation(summary = "뉴스 댓글 리스트 조회", description = "뉴스 댓글 페이징 처리해서 전달")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = GetNewsRepliesResponseDTO.class))),
    })
    @GetMapping()
    public ResponseEntity<ResponseDTO<List<ReplyDTO>>> getNewsReplies(@Valid GetNewsRepliesRequestDTO query){
        User user = jwtTokenProvider.getUserFromSecurityContext();
        News newsData = newsService.findByPk(query.getNews());
        if(newsData == null) throw new NotFoundException(ResponseCode.NOT_FOUND_NEWS);
        PaginatedNewsReplyListDTO paginatedReplyListDTO = newsReplyService.getRepliesByNews(query.getNews(),user!=null ? user.getPk() : null, query.getPage(), query.getSize(), query.getInfinite() != null ? query.getInfinite() : null, query.getLastReply());
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
    public ResponseEntity<ResponseDTO<Void>> patchNewsReply(@PathVariable Long newsReplyPk,
        @Valid @RequestBody PatchNewsReplyRequestDTO request){
        User user = jwtTokenProvider.getUserFromSecurityContext();
        NewsReply newsReplyData = newsReplyService.findByPk(newsReplyPk);
        if(newsReplyData == null) throw new NotFoundException(ResponseCode.NOT_FOUND_NEWS_REPLY);
        if (!newsReplyData.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException(ResponseCode.FORBIDDEN);
        }
        newsReplyData.setContents(request.getContents());

        newsReplyService.patchNewsReply(newsReplyData);
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS));
    }
}
