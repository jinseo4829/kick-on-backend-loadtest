package kr.kickon.api.domain.newsReply;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.kickon.api.domain.board.BoardService;
import kr.kickon.api.domain.board.dto.BoardDetailDTO;
import kr.kickon.api.domain.board.dto.BoardListDTO;
import kr.kickon.api.domain.board.dto.PaginatedBoardListDTO;
import kr.kickon.api.domain.board.request.CreateBoardRequestDTO;
import kr.kickon.api.domain.board.request.GetBoardsRequestDTO;
import kr.kickon.api.domain.board.response.GetBoardDetailResponse;
import kr.kickon.api.domain.board.response.GetBoardsResponse;
import kr.kickon.api.domain.board.response.GetHomeBoardsResponse;
import kr.kickon.api.domain.news.NewsService;
import kr.kickon.api.domain.newsReply.request.CreateNewsReplyRequestDTO;
import kr.kickon.api.domain.team.TeamService;
import kr.kickon.api.domain.userFavoriteTeam.UserFavoriteTeamService;
import kr.kickon.api.global.auth.jwt.JwtTokenProvider;
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

        newsReplyService.save(newsReply);
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS));
    }
}
