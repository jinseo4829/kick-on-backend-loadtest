package kr.kickon.api.domain.news;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.kickon.api.domain.actualSeasonRanking.response.GetActualSeasonRankingResponse;
import kr.kickon.api.domain.news.dto.NewsListDTO;
import kr.kickon.api.domain.news.response.GetHomeNewsResponse;
import kr.kickon.api.domain.userFavoriteTeam.UserFavoriteTeamService;
import kr.kickon.api.global.auth.jwt.JwtTokenProvider;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.entities.User;
import kr.kickon.api.global.common.entities.UserFavoriteTeam;
import kr.kickon.api.global.common.enums.ResponseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/news")
@Tag(name = "뉴스")
@Slf4j
public class NewsController {
    private final NewsService newsService;
    private final UserFavoriteTeamService userFavoriteTeamService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "홈화면 함께 볼만한 뉴스 리스트 조회", description = "응원팀이 있다면 관련 최신 게시글 기준으로 3개 리스트 반환")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = GetHomeNewsResponse.class))),
    })
    @GetMapping("/home")
    public ResponseEntity<ResponseDTO<List<NewsListDTO>>> getEventBoards() {
        User user = jwtTokenProvider.getUserFromSecurityContext();
        List<NewsListDTO>news=null;
        if(user==null){
            news = newsService.findRecent3News();
        }else{
            UserFavoriteTeam userFavoriteTeam = userFavoriteTeamService.findByUserPk(user.getPk());
            if(userFavoriteTeam==null){
                news = newsService.findRecent3News();
            }else{
                news = newsService.findRecent3NewsWithUserTeam(userFavoriteTeam.getTeam().getPk());
            }
        }

        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS, news));
    }
}
