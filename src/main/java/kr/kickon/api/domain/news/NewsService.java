package kr.kickon.api.domain.news;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.kickon.api.domain.board.dto.BoardDetailDTO;
import kr.kickon.api.domain.board.dto.BoardListDTO;
import kr.kickon.api.domain.board.dto.PaginatedBoardListDTO;
import kr.kickon.api.domain.news.dto.*;
import kr.kickon.api.domain.newsKick.NewsKickService;
import kr.kickon.api.global.common.BaseService;
import kr.kickon.api.global.common.entities.*;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.util.UUIDGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class NewsService implements BaseService<News> {
    private final NewsRepository newsRepository;
    private final JPAQueryFactory queryFactory;
    private final UUIDGenerator uuidGenerator;
    private final NewsKickService newsKickService;

    @Override
    public News findById(String uuid) {
        BooleanExpression predicate = QNews.news.id.eq(uuid).and(QNews.news.status.eq(DataStatus.ACTIVATED));
        Optional<News> news = newsRepository.findOne(predicate);
        return news.orElse(null);
    }

    @Override
    public News findByPk(Long pk) {
        BooleanExpression predicate = QNews.news.pk.eq(pk).and(QNews.news.status.eq(DataStatus.ACTIVATED));
        Optional<News> news = newsRepository.findOne(predicate);
        return news.orElse(null);
    }

    public JPAQuery<Tuple> createNewsListDTOQuery(){
        QNews news = QNews.news;
        QNewsKick newsKick = QNewsKick.newsKick;
        QNewsViewHistory newsViewHistory = QNewsViewHistory.newsViewHistory;
        QNewsReply newsReply = QNewsReply.newsReply;
        QUser user = QUser.user;
        return queryFactory.select(news, user,
                        newsKick.pk.countDistinct().coalesce(0L).as("kickCount"),
                        newsViewHistory.pk.countDistinct().coalesce(0L).as("viewCount"),
                        newsReply.pk.countDistinct().coalesce(0L).as("replyCount"))
                .from(news)
                .join(user).on(news.user.pk.eq(user.pk))
                .leftJoin(newsKick).on(news.pk.eq(newsKick.news.pk).and(newsKick.status.eq(DataStatus.ACTIVATED)))
                .leftJoin(newsViewHistory).on(news.pk.eq(newsViewHistory.news.pk).and(newsViewHistory.status.eq(DataStatus.ACTIVATED)))
                .leftJoin(newsReply).on(news.pk.eq(newsReply.news.pk).and(newsReply.status.eq(DataStatus.ACTIVATED)))
                .where(news.status.eq(DataStatus.ACTIVATED)
                        .and(user.status.eq(DataStatus.ACTIVATED)));
    }

    public NewsListDTO tupleToNewsListDTO(Tuple tuple){
        QNews news = QNews.news;
        QUser user = QUser.user;
        News newsEntity = tuple.get(news);
        User userEntity = tuple.get(user);
        return NewsListDTO.builder()
                .pk(newsEntity.getPk())
                .title(newsEntity.getTitle())
                .content(newsEntity.getContents())
                .thumbnailUrl(newsEntity.getThumbnailUrl())
                .category(newsEntity.getCategory())
                .user(UserDTO.builder()
                        .id(userEntity.getId())
                        .nickname(userEntity.getNickname())
                        .profileImageUrl(userEntity.getProfileImageUrl())
                        .build())
                .createdAt(newsEntity.getCreatedAt())
                .likes(tuple.get(2, Long.class).intValue())
                .views(tuple.get(3, Long.class).intValue())
                .replies(tuple.get(4, Long.class).intValue())
                .build();
    }

    public NewsDetailDTO findNewsDeatailDTOByPk(Long boardPk, Long userPk) {
        QNews news = QNews.news;
        QUser user = QUser.user;
        Tuple result = createNewsListDTOQuery()
                .where(news.pk.eq(boardPk))
                .groupBy(news.pk, user.pk)
                .fetchOne();
        if(result == null) return null;

        NewsKick newsKick = newsKickService.findByBoardAndUser(result.get(news).getPk(),userPk);
        News newsEntity = result.get(news);
        User userEntity = result.get(user);

        System.out.println(result);
        return NewsDetailDTO.builder()
                .pk(newsEntity.getPk())
                .title(newsEntity.getTitle())
                .user(UserDTO.builder()
                        .id(userEntity.getId())
                        .nickname(userEntity.getNickname())
                        .profileImageUrl(userEntity.getProfileImageUrl())
                        .build())
                .createdAt(result.get(news.createdAt))
                .createdAt(newsEntity.getCreatedAt())
                .likes(result.get(2, Long.class).intValue())
                .views(result.get(3, Long.class).intValue())
                .replies(result.get(4, Long.class).intValue())
                .isKicked(newsKick!=null)
                .content(newsEntity.getContents())
                .build();
    }

    public List<NewsListDTO> findRecent3News() {
        QNews news = QNews.news;
        QUser user = QUser.user;
        List<Tuple> results = createNewsListDTOQuery()
                .groupBy(news.pk, user.pk)
                .orderBy(news.createdAt.desc())
                .limit(3)
                .fetch();
        return results.stream().map(this::tupleToNewsListDTO).toList();
    }

    public List<NewsListDTO> findRecent3NewsWithUserTeam(Long teamPk) {
        QNews news = QNews.news;
        QUser user = QUser.user;

        List<Tuple> results = createNewsListDTOQuery()
                .where(news.team.pk.eq(teamPk))
                .groupBy(news.pk, user.pk)
                .orderBy(news.createdAt.desc())
                .limit(3)
                .fetch();
        return results.stream().map(this::tupleToNewsListDTO).toList();
    }

    public List<HotNewsListDTO> findTop5HotNews() {
        QNews news = QNews.news;
        QNewsViewHistory newsViewHistory = QNewsViewHistory.newsViewHistory;

        List<Tuple> results = queryFactory.select(news,
                        newsViewHistory.pk.count().coalesce(0L).as("viewCount"))
                .from(news)
                .leftJoin(newsViewHistory).on(news.pk.eq(newsViewHistory.news.pk)
                        .and(newsViewHistory.status.eq(DataStatus.ACTIVATED)))
                .where(news.status.eq(DataStatus.ACTIVATED)
                        .and(news.createdAt.goe(LocalDateTime.now().minusDays(1)))) // 최근 24시간 이내 뉴스만 조회
                .groupBy(news.pk)
                .orderBy(newsViewHistory.pk.count().coalesce(0L).desc()) // 조회수 기준 내림차순 정렬
                .limit(5)
                .fetch();

        return results.stream().map(tuple -> {
            News newsEntity = tuple.get(news);
            return HotNewsListDTO.builder()
                    .pk(newsEntity.getPk())
                    .title(newsEntity.getTitle())
                    .thumbnailUrl(newsEntity.getThumbnailUrl())
                    .category(newsEntity.getCategory())
                    .views(tuple.get(1, Long.class).intValue()) // 조회수 가져오기
                    .build();
        }).toList();
    }

    public void save(News news){
        newsRepository.save(news);
    }

    public PaginatedNewsListDTO findNewsWithPagination(Long teamPk, int page, int size, String sortBy, Long leaguePk) {
        QNews news = QNews.news;
        QNewsViewHistory newsViewHistory = QNewsViewHistory.newsViewHistory;
        QUser user = QUser.user;
        Integer offset = (page - 1) * size;
        LocalDateTime hotThreshold = LocalDateTime.now().minusHours(48); // 최근 48시간 기준
        List<Long> teamPks = List.of();
        // ✅ 전체 게시글 수 계산
        JPAQuery<Long> totalQuery = queryFactory.select(news.pk.count())
                .from(news)
                .join(user).on(news.user.pk.eq(user.pk))
                .where(news.status.eq(DataStatus.ACTIVATED)
                        .and(user.status.eq(DataStatus.ACTIVATED)));
        if (teamPk != null) totalQuery.where(news.team.pk.eq(teamPk));
        if ("hot".equalsIgnoreCase(sortBy)) totalQuery.where(news.createdAt.goe(hotThreshold)); // hot일 때 48시간 내 필터링
        // ✅ 리그 기준 팀 필터링
        if (leaguePk != null) {
            QActualSeason actualSeason = QActualSeason.actualSeason;
            QActualSeasonTeam actualSeasonTeam = QActualSeasonTeam.actualSeasonTeam;

            teamPks = queryFactory.select(actualSeasonTeam.team.pk)
                    .from(actualSeasonTeam)
                    .join(actualSeason).on(actualSeasonTeam.actualSeason.pk.eq(actualSeason.pk))
                    .where(actualSeason.league.pk.eq(leaguePk).and(actualSeasonTeam.status.eq(DataStatus.ACTIVATED)).and(actualSeason.status.eq(DataStatus.ACTIVATED)).and(actualSeasonTeam.team.status.eq(DataStatus.ACTIVATED)))
                    .fetch();

            totalQuery.where(news.team.pk.in(teamPks));
        }
        Long totalCount = totalQuery.fetchOne();

        JPAQuery<Tuple> dataQuery = createNewsListDTOQuery()
                .groupBy(news.pk, user.pk)
                .offset(offset)
                .limit(size);
        if (leaguePk != null) dataQuery.where(news.team.pk.in(teamPks));

        // ✅ 정렬 기준에 따른 동적 처리
        if ("hot".equalsIgnoreCase(sortBy)) {
            dataQuery.where(news.createdAt.goe(hotThreshold)); // hot일 때 48시간 내 필터링
            dataQuery.orderBy(newsViewHistory.pk.count().coalesce(0L).desc(), news.createdAt.desc()); // 조회수 + 최신순
        } else {
            dataQuery.orderBy(news.createdAt.desc()); // 기본값: 최신순
        }



        List<Tuple> results = dataQuery.fetch();

        // ✅ DTO 변환
        List<NewsListDTO> newsList = results.stream().map(this::tupleToNewsListDTO).toList();

        // ✅ 메타데이터 포함한 결과 반환
        return new PaginatedNewsListDTO(page, size, totalCount, newsList);
    }
}
