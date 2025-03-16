package kr.kickon.api.domain.news;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.kickon.api.domain.news.dto.HotNewsListDTO;
import kr.kickon.api.domain.news.dto.NewsListDTO;
import kr.kickon.api.domain.news.request.CreateNewsDTO;
import kr.kickon.api.domain.news.response.GetHomeNewsResponse;
import kr.kickon.api.domain.news.response.GetHotNewsResponse;
import kr.kickon.api.domain.team.TeamService;
import kr.kickon.api.domain.userFavoriteTeam.UserFavoriteTeamService;
import kr.kickon.api.global.auth.jwt.JwtTokenProvider;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.entities.News;
import kr.kickon.api.global.common.entities.Team;
import kr.kickon.api.global.common.entities.User;
import kr.kickon.api.global.common.entities.UserFavoriteTeam;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import kr.kickon.api.global.util.UUIDGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    private final TeamService teamService;
    private final UUIDGenerator uuidGenerator;

    @Operation(summary = "홈화면 함께 볼만한 뉴스 리스트 조회", description = "응원팀이 있다면 관련 최신 게시글 기준으로 3개 리스트 반환")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = GetHomeNewsResponse.class))),
    })
    @GetMapping("/home")
    public ResponseEntity<ResponseDTO<List<NewsListDTO>>> getHomeNews() {
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

    @Operation(summary = "조회수 Top 5 뉴스 리스트 조회", description = "24시간 이내 생성된 게시글 중 조회수가 가장 높은 뉴스 5개 반환")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = GetHotNewsResponse.class))),
    })
    @GetMapping("/hot")
    public ResponseEntity<ResponseDTO<List<HotNewsListDTO>>> getHotNews() {
        List<HotNewsListDTO> news = newsService.findTop5HotNews();
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS, news));
    }

    @Operation(summary = "뉴스 생성", description = "회원가입한 유저만 뉴스 생성 가능")
    @PostMapping()
    public ResponseEntity<ResponseDTO<Void>> createNews(@Valid @RequestBody CreateNewsDTO request){
        User user = jwtTokenProvider.getUserFromSecurityContext();

        String id = uuidGenerator.generateUniqueUUID(newsService::findById);



        News news = News.builder()
                .id(id)
                .user(user)
                .category(request.getCategory())
                .contents(request.getContents())
                .title(request.getTitle()).build();
        if(request.getThumbnailUrl()!=null) news.setThumbnailUrl(request.getThumbnailUrl());
        if(request.getTeam()!=null){
            Team team = teamService.findByPk(request.getTeam());
            if(team==null) throw new NotFoundException(ResponseCode.NOT_FOUND_TEAM);
            news.setTeam(team);
        }

        newsService.save(news);
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS));
    }
}

