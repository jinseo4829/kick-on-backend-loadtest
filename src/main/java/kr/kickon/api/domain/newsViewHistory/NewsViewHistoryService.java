package kr.kickon.api.domain.newsViewHistory;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
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

    // region {findById} UUID로 조회 이력 조회 (활성 상태만)
    /**
     * UUID로 조회 이력 조회 (활성 상태만)
     *
     * @param uuid 조회 이력 UUID
     * @return NewsViewHistory 또는 null
     */
    @Override
    public NewsViewHistory findById(String uuid) {
        BooleanExpression predicate = QNewsViewHistory.newsViewHistory.id.eq(uuid).and(QNewsViewHistory.newsViewHistory.status.eq(DataStatus.ACTIVATED));
        Optional<NewsViewHistory> newsViewHistory = newsViewHistoryRepository.findOne(predicate);
        return newsViewHistory.orElse(null);
    }
    // endregion

    // region {findByPk} PK로 조회 이력 조회 (활성 상태만)
    /**
     * PK로 조회 이력 조회 (활성 상태만)
     *
     * @param pk 조회 이력 PK
     * @return NewsViewHistory 또는 null
     */
    @Override
    public NewsViewHistory findByPk(Long pk) {
        BooleanExpression predicate = QNewsViewHistory.newsViewHistory.pk.eq(pk).and(QNewsViewHistory.newsViewHistory.status.eq(DataStatus.ACTIVATED));
        Optional<NewsViewHistory> newsViewHistory = newsViewHistoryRepository.findOne(predicate);
        return newsViewHistory.orElse(null);
    }
    // endregion

    // region {save} 뉴스 조회 이력 저장
    /**
     * 뉴스 조회 이력 저장
     *
     * @param newsViewHistory 저장할 조회 이력 엔티티
     */
    public void save(NewsViewHistory newsViewHistory) {
        newsViewHistoryRepository.save(newsViewHistory);
    }
    // endregion

    // region 뉴스 조회수 반환
    /**
     * News PK로 조회수 계산
     *
     * @param newsPk
     * @return Long 조회수
     */
    public Long countViewsByNewsPk(Long newsPk) {
        return newsViewHistoryRepository.countByNews_Pk(newsPk);
    }
    // endregion
}
