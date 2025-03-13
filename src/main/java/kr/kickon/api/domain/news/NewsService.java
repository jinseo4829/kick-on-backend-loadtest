package kr.kickon.api.domain.news;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.kickon.api.domain.news.dto.NewsListDTO;
import kr.kickon.api.domain.news.dto.UserDTO;
import kr.kickon.api.global.common.BaseService;
import kr.kickon.api.global.common.entities.*;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.util.UUIDGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class NewsService implements BaseService<News> {
    private final NewsRepository newsRepository;
    private final JPAQueryFactory queryFactory;
    private final UUIDGenerator uuidGenerator;

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

    public List<NewsListDTO> findRecent3News() {
        QNews news = QNews.news;
        QNewsKick newsKick = QNewsKick.newsKick;
        QNewsViewHistory newsViewHistory = QNewsViewHistory.newsViewHistory;
        QNewsReply newsReply = QNewsReply.newsReply;
        QUser user = QUser.user;

        List<Tuple> results = queryFactory.select(news, user,
                        newsKick.pk.count().coalesce(0L).as("kickCount"),
                        newsViewHistory.pk.count().coalesce(0L).as("viewCount"),
                        newsReply.pk.count().coalesce(0L).as("replyCount"))
                .from(news)
                .join(user).on(news.user.pk.eq(user.pk))
                .leftJoin(newsKick).on(news.pk.eq(newsKick.news.pk).and(newsKick.status.eq(DataStatus.ACTIVATED)))
                .leftJoin(newsViewHistory).on(news.pk.eq(newsViewHistory.news.pk).and(newsViewHistory.status.eq(DataStatus.ACTIVATED)))
                .leftJoin(newsReply).on(news.pk.eq(newsReply.news.pk).and(newsReply.status.eq(DataStatus.ACTIVATED)))
                .where(news.status.eq(DataStatus.ACTIVATED)
                        .and(user.status.eq(DataStatus.ACTIVATED)))
                .groupBy(news.pk, user.pk)
                .orderBy(news.createdAt.desc())
                .limit(3)
                .fetch();
        return results.stream().map(tuple -> {
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
                    .views(tuple.get(2, Long.class).intValue())
                    .likes(tuple.get(3, Long.class).intValue())
                    .replies(tuple.get(4, Long.class).intValue())
                    .build();
        }).toList();
    }

    public List<NewsListDTO> findRecent3NewsWithUserTeam(Long teamPk) {
        QNews news = QNews.news;
        QNewsKick newsKick = QNewsKick.newsKick;
        QNewsViewHistory newsViewHistory = QNewsViewHistory.newsViewHistory;
        QNewsReply newsReply = QNewsReply.newsReply;
        QUser user = QUser.user;

        List<Tuple> results = queryFactory.select(news, user,
                        newsKick.pk.count().coalesce(0L).as("kickCount"),
                        newsViewHistory.pk.count().coalesce(0L).as("viewCount"),
                        newsReply.pk.count().coalesce(0L).as("replyCount"))
                .from(news)
                .join(user).on(news.user.pk.eq(user.pk))
                .leftJoin(newsKick).on(news.pk.eq(newsKick.news.pk).and(newsKick.status.eq(DataStatus.ACTIVATED)))
                .leftJoin(newsViewHistory).on(news.pk.eq(newsViewHistory.news.pk).and(newsViewHistory.status.eq(DataStatus.ACTIVATED)))
                .leftJoin(newsReply).on(news.pk.eq(newsReply.news.pk).and(newsReply.status.eq(DataStatus.ACTIVATED)))
                .where(news.status.eq(DataStatus.ACTIVATED)
                        .and(user.status.eq(DataStatus.ACTIVATED))
                        .and(news.team.pk.eq(teamPk)))
                .groupBy(news.pk, user.pk)
                .orderBy(news.createdAt.desc())
                .limit(3)
                .fetch();
        return results.stream().map(tuple -> {
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
                    .views(tuple.get(2, Long.class).intValue())
                    .likes(tuple.get(3, Long.class).intValue())
                    .replies(tuple.get(4, Long.class).intValue())
                    .build();
        }).toList();
    }
}
