package kr.kickon.api.domain.newsReply;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.kickon.api.global.common.BaseService;
import kr.kickon.api.global.common.entities.NewsKick;
import kr.kickon.api.global.common.entities.NewsReply;
import kr.kickon.api.global.common.entities.QNewsKick;
import kr.kickon.api.global.common.entities.QNewsReply;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.util.UUIDGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class NewsReplyService implements BaseService<NewsReply> {
    private final NewsReplyRepository newsReplyRepository;
    private final JPAQueryFactory queryFactory;
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

    public void save(NewsReply newsReply) {
        newsReplyRepository.save(newsReply);
    }
}
