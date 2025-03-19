package kr.kickon.api.domain.newsReply;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.kickon.api.domain.news.dto.UserDTO;
import kr.kickon.api.domain.newsReply.dto.PaginatedNewsReplyListDTO;
import kr.kickon.api.domain.newsReply.dto.ReplyDTO;
import kr.kickon.api.domain.newsReplyKick.NewsReplyKickService;
import kr.kickon.api.global.common.BaseService;
import kr.kickon.api.global.common.entities.*;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.util.UUIDGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class NewsReplyService implements BaseService<NewsReply> {
    private final NewsReplyRepository newsReplyRepository;
    private final JPAQueryFactory queryFactory;
    private final NewsReplyKickService newsReplyKickService;
    private final UUIDGenerator uuidGenerator;

    @Override
    public NewsReply findById(String uuid) {
        BooleanExpression predicate = QNewsReply.newsReply.id.eq(uuid).and(QNewsReply.newsReply.status.eq(DataStatus.ACTIVATED));
        Optional<NewsReply> newsReply = newsReplyRepository.findOne(predicate);
        return newsReply.orElse(null);
    }

    @Override
    public NewsReply findByPk(Long pk) {
        BooleanExpression predicate = QNewsReply.newsReply.pk.eq(pk).and(QNewsReply.newsReply.status.eq(DataStatus.ACTIVATED));
        Optional<NewsReply> newsReply = newsReplyRepository.findOne(predicate);
        return newsReply.orElse(null);
    }


    public PaginatedNewsReplyListDTO getRepliesByNews(Long newsPk, Long userPk, Integer page, Integer size) {
        QNewsReply reply = QNewsReply.newsReply;
        QUser user = QUser.user;
        Integer offset = (page - 1) * size;
        Long total = queryFactory.select(reply.pk.countDistinct())
                .from(reply)
                .join(user).on(reply.user.pk.eq(user.pk))
                .where(reply.news.pk.eq(newsPk)
                        .and(reply.parentNewsReply.isNull())
                        .and(user.status.eq(DataStatus.ACTIVATED))
                        .and(reply.status.eq(DataStatus.ACTIVATED)))
                .fetchOne();

        List<Tuple> results = queryFactory.select(reply, user)
                .from(reply)
                .join(user).on(reply.user.pk.eq(user.pk))
                .where(reply.news.pk.eq(newsPk)
                        .and(reply.parentNewsReply.isNull())
                        .and(user.status.eq(DataStatus.ACTIVATED))
                        .and(reply.status.eq(DataStatus.ACTIVATED)))
                .offset(offset)
                .limit(size)
                .orderBy(reply.createdAt.asc())
                .fetch();

        List<ReplyDTO> replyList = results.stream()
                .map(tuple -> mapToReplyDTO(tuple, userPk))
                .toList();
        return new PaginatedNewsReplyListDTO(page, size, total, replyList);
    }

    private ReplyDTO mapToReplyDTO(Tuple tuple, Long userPk) {
        QNewsReply reply = QNewsReply.newsReply;
        QUser user = QUser.user;

        NewsReply parentReply = tuple.get(reply);
        User replyUser = tuple.get(user);
        NewsReplyKick myNewsReplyKick = null;
        if(userPk != null) {
            myNewsReplyKick = newsReplyKickService.findByNewsReplyAndUser(parentReply.getPk(), userPk);
        }
        Long kickCount = newsReplyKickService.countKicks(parentReply.getPk());
        return ReplyDTO.builder()
                .pk(parentReply.getPk())
                .contents(parentReply.getContents())
                .createdAt(parentReply.getCreatedAt())
                .user(UserDTO.builder()
                        .id(replyUser.getId())
                        .nickname(replyUser.getNickname())
                        .profileImageUrl(replyUser.getProfileImageUrl())
                        .build())
                .replies(parentReply.getParentNewsReply() != null ? null : getChildReplies(parentReply.getPk(), userPk))
                .isKicked(myNewsReplyKick!=null)
                .kickCount(kickCount)
                .build();
    }

    private List<ReplyDTO> getChildReplies(Long parentPk, Long userPk) {
        QNewsReply reply = QNewsReply.newsReply;
        QUser user = QUser.user;

        List<Tuple> results = queryFactory.select(reply, user)
                .from(reply)
                .join(user).on(reply.user.pk.eq(user.pk))
                .where(reply.parentNewsReply.pk.eq(parentPk)
                        .and(reply.status.eq(DataStatus.ACTIVATED)))
                .orderBy(reply.createdAt.asc())
                .fetch();

        return results.stream()
                .map(tuple -> mapToReplyDTO(tuple, userPk))
                .collect(Collectors.toList());
    }

    public void save(NewsReply newsReply) {
        newsReplyRepository.save(newsReply);
    }
}
