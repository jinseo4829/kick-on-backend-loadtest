package kr.kickon.api.domain.news;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.kickon.api.domain.league.LeagueService;
import kr.kickon.api.domain.news.dto.*;
import kr.kickon.api.domain.news.request.CreateNewsRequest;
import kr.kickon.api.domain.news.request.GetNewsRequest;
import kr.kickon.api.domain.news.response.GetHomeNewsResponse;
import kr.kickon.api.domain.news.response.GetHotNewsResponse;
import kr.kickon.api.domain.news.response.GetNewsDetailResponse;
import kr.kickon.api.domain.news.response.GetNewsResponse;
import kr.kickon.api.domain.team.TeamService;
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
import java.util.Set;
import java.util.stream.Collectors;


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
    private final LeagueService leagueService;

    @Operation(summary = "홈화면 함께 볼만한 뉴스 리스트 조회", description = "응원팀이 있다면 관련 최신 게시글 기준으로 3개 리스트 반환")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = GetHomeNewsResponse.class))),
    })
    @GetMapping("/home")
    public ResponseEntity<ResponseDTO<List<NewsListDTO>>> getHomeNews(@RequestParam(required = false) String type) {
        User user = jwtTokenProvider.getUserFromSecurityContext();
        List<NewsListDTO>news=null;
        if(user==null){
            news = newsService.getRecent3NewsList();
        }else{
            List<UserFavoriteTeam> favoriteTeams = userFavoriteTeamService.findTop3ByUserPkOrderByPriorityNumAsc(user.getPk());

            if (favoriteTeams == null || favoriteTeams.isEmpty()) {
                news = newsService.getRecent3NewsList();
            } else {
                Set<Long> teamPks = favoriteTeams.stream()
                        .map(fav -> fav.getTeam().getPk())
                        .collect(Collectors.toSet());

                // 중복 제거 포함된 뉴스 3개까지 조회 (최대 3개 팀 기준)
                news = newsService.getRecent3NewsListWithUserTeam(teamPks, 3);
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
        List<HotNewsListDTO> news = newsService.getTop5HotNewsList();
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS, news));
    }

    @Operation(summary = "뉴스 생성", description = "회원가입한 유저만 뉴스 생성 가능")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = GetNewsDetailResponse.class))),
    })
    @PostMapping()
    public ResponseEntity<ResponseDTO<NewsDetailDTO>> createNews(@Valid @RequestBody CreateNewsRequest request){
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

        News newsCreated = newsService.createNewsWithMedia(news, request.getUsedImageKeys(),
            request.getUsedVideoKeys(), request.getEmbeddedLinks());

        NewsDetailDTO newsDetailDTO = newsService.getNewsDetailDTOByPk(newsCreated.getPk(),user);
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS,newsDetailDTO));
    }

    @Operation(summary = "뉴스 리스트 조회", description = "페이징 처리 적용하여 뉴스 리스트 조회")
    @GetMapping()
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = GetNewsResponse.class))),
    })
    public ResponseEntity<ResponseDTO<List<NewsListDTO>>> getNews(@Valid @ModelAttribute GetNewsRequest query) {
        User user = jwtTokenProvider.getUserFromSecurityContext();
        if(query.getTeam()!=null){
            Team team = teamService.findByPk(query.getTeam());
            if(team==null) throw new NotFoundException(ResponseCode.NOT_FOUND_TEAM);
        }

        if(query.getLeague()!=null){
            League league = leagueService.findByPk(query.getLeague());
            if(league==null) throw new NotFoundException(ResponseCode.NOT_FOUND_LEAGUE);
        }

        // infinite == true → 무한스크롤: hasNext 반환
        // 무한 스크롤 처리
        PaginatedNewsListDTO news = newsService.getNewsListWithPagination(query.getTeam() != null ? query.getTeam() : null, query.getPage(), query.getSize(),query.getOrder(), query.getLeague(), query.getInfinite() != null ? query.getInfinite() : null, query.getLastNews(), query.getLastViewCount());
        if(news.getHasNext()!=null){
            return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS, news.getNewsList(), new PagedMetaDTO(news.getHasNext())));
        }else{
            return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS, news.getNewsList(), new PagedMetaDTO(news.getCurrentPage(), news.getPageSize(), news.getTotalItems())));
        }
    }

    @Operation(summary = "뉴스 상세 조회", description = "뉴스 PK 값으로 게시글 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = GetNewsDetailResponse.class))),
    })
    @GetMapping("/{newsPk}")
    public ResponseEntity<ResponseDTO<NewsDetailDTO>> getBoardDetail(@PathVariable Long newsPk){
        User user = jwtTokenProvider.getUserFromSecurityContext();
        NewsDetailDTO newsDetailDTO = newsService.getNewsDetailDTOByPk(newsPk,user);
        if(newsDetailDTO==null) throw new NotFoundException(ResponseCode.NOT_FOUND_NEWS);
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS, newsDetailDTO));
    }

    @Operation(summary = "뉴스 삭제", description = "뉴스 PK 값으로 뉴스 삭제")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공"),
    })
    @DeleteMapping("/{newsPk}")
    public ResponseEntity<ResponseDTO> deleteBoard(@PathVariable Long newsPk){
        User user = jwtTokenProvider.getUserFromSecurityContext();
        News news = newsService.findByPk(newsPk);
        if(news==null) throw new NotFoundException(ResponseCode.NOT_FOUND_NEWS);
        if (!news.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException(ResponseCode.FORBIDDEN);
        }
        newsService.deleteNews(news);
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS));
    }

    @Operation(summary = "뉴스 수정", description = "뉴스 PK값으로 뉴스 수정")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공",
            content = @Content(schema = @Schema(implementation = GetNewsDetailResponse.class))),
    })
    @PatchMapping("/{newsPk}")
    public ResponseEntity<ResponseDTO<NewsDetailDTO>> patchNews(@PathVariable Long newsPk,
        @Valid @RequestBody CreateNewsRequest request){
        User user = jwtTokenProvider.getUserFromSecurityContext();
        News newsData = newsService.findByPk(newsPk);
        if(newsData == null) throw new NotFoundException(ResponseCode.NOT_FOUND_NEWS);
        if (!newsData.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException(ResponseCode.FORBIDDEN);
        }
        newsData.setContents(request.getContents());
        newsData.setTitle(request.getTitle());
        newsData.setCategory(request.getCategory());
        if(request.getThumbnailUrl()!=null) newsData.setThumbnailUrl(request.getThumbnailUrl());

        if(request.getTeam()!=null){
            Team team  = teamService.findByPk(request.getTeam());
            if(team==null) throw new NotFoundException(ResponseCode.NOT_FOUND_TEAM);
            newsData.setTeam(team);
        }else{
            newsData.setTeam(null);
        }
        newsService.updateNews(newsData, request.getUsedImageKeys(), request.getUsedVideoKeys(), request.getEmbeddedLinks());
        NewsDetailDTO newsDetailDTO = newsService.getNewsDetailDTOByPk(newsPk,user);
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS, newsDetailDTO));
    }
}

