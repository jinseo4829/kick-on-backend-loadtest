package kr.kickon.api.domain.newsViewHistory;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.kickon.api.global.common.BaseService;
import kr.kickon.api.global.common.entities.NewsViewHistory;
import kr.kickon.api.global.common.entities.QNewsViewHistory;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.util.UUIDGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class NewsViewHistoryService implements BaseService<NewsViewHistory> {
    private final NewsViewHistoryRepository newsViewHistoryRepository;
    private final JPAQueryFactory queryFactory;
    private final UUIDGenerator uuidGenerator;

    @Override
    public NewsViewHistory findById(String uuid) {
        BooleanExpression predicate = QNewsViewHistory.newsViewHistory.id.eq(uuid).and(QNewsViewHistory.newsViewHistory.status.eq(DataStatus.ACTIVATED));
        Optional<NewsViewHistory> newsViewHistory = newsViewHistoryRepository.findOne(predicate);
        return newsViewHistory.orElse(null);
    }

    @Override
    public NewsViewHistory findByPk(Long pk) {
        BooleanExpression predicate = QNewsViewHistory.newsViewHistory.pk.eq(pk).and(QNewsViewHistory.newsViewHistory.status.eq(DataStatus.ACTIVATED));
        Optional<NewsViewHistory> newsViewHistory = newsViewHistoryRepository.findOne(predicate);
        return newsViewHistory.orElse(null);
    }

    public void save(NewsViewHistory newsViewHistory) {
        newsViewHistoryRepository.save(newsViewHistory);
    }
}
