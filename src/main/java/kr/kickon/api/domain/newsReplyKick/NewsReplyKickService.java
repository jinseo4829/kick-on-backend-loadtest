package kr.kickon.api.domain.newsReplyKick;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.kickon.api.global.common.BaseService;
import kr.kickon.api.global.common.entities.NewsReplyKick;
import kr.kickon.api.global.common.entities.QNewsReplyKick;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.util.UUIDGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class NewsReplyKickService implements BaseService<NewsReplyKick> {
    private final NewsReplyKickRepository newsReplyKickRepository;
    private final JPAQueryFactory queryFactory;
    private final UUIDGenerator uuidGenerator;

    // region {findById} UUID로 NewsReplyKick 조회 (활성 상태만)
    /**
     * UUID로 NewsReplyKick 조회 (활성 상태만)
     *
     * @param uuid 식별자
     * @return NewsReplyKick 또는 null
     */
    @Override
    public NewsReplyKick findById(String uuid) {
        BooleanExpression predicate = QNewsReplyKick.newsReplyKick.id.eq(uuid).and(QNewsReplyKick.newsReplyKick.status.eq(DataStatus.ACTIVATED));
        Optional<NewsReplyKick> newsReplyKick = newsReplyKickRepository.findOne(predicate);
        return newsReplyKick.orElse(null);
    }
    // endregion

    // region {findByPk} PK로 NewsReplyKick 조회 (활성 상태만)
    /**
     * PK로 NewsReplyKick 조회 (활성 상태만)
     *
     * @param pk 기본키
     * @return NewsReplyKick 또는 null
     */
    @Override
    public NewsReplyKick findByPk(Long pk) {
        BooleanExpression predicate = QNewsReplyKick.newsReplyKick.pk.eq(pk).and(QNewsReplyKick.newsReplyKick.status.eq(DataStatus.ACTIVATED));
        Optional<NewsReplyKick> newsReplyKick = newsReplyKickRepository.findOne(predicate);
        return newsReplyKick.orElse(null);
    }
    // endregion

    // region {findByNewsReplyAndUser}
    /**
     * 댓글 PK와 사용자 PK로 NewsReplyKick 조회
     *
     * @param newsReplyPk 댓글 PK
     * @param userPk 사용자 PK
     * @return NewsReplyKick 또는 null
     */
    public NewsReplyKick findByNewsReplyAndUser(Long newsReplyPk, Long userPk){
        BooleanExpression predicate = QNewsReplyKick.newsReplyKick.newsReply.pk.eq(newsReplyPk).and(QNewsReplyKick.newsReplyKick.user.pk.eq(userPk));
        Optional<NewsReplyKick> newsReplyKick = newsReplyKickRepository.findOne(predicate);
        return newsReplyKick.orElse(null);
    }
    // endregion

    // region {getNewsReplyKickCount} 특정 댓글의 활성화된 킥 수 조회
    /**
     * 특정 댓글의 활성화된 킥 수 조회
     *
     * @param newsReplyPk 댓글 PK
     * @return 킥 수
     */
    public Long getNewsReplyKickCount(Long newsReplyPk) {
        // 조건에 맞는 newsReplyKick의 개수를 반환
        QNewsReplyKick newsReplyKick = QNewsReplyKick.newsReplyKick;
        Long num = queryFactory
                .select(newsReplyKick.countDistinct())
                .from(newsReplyKick)
                .where(newsReplyKick.newsReply.pk.eq(newsReplyPk)
                        .and(newsReplyKick.status.eq(DataStatus.ACTIVATED))
                        .and(newsReplyKick.user.status.eq(DataStatus.ACTIVATED)))
                .fetchOne();  // 해당 조건에 맞는 레코드 개수를 반환
        return  num;
    }
    // endregion

    // region {save} NewsReplyKick 저장
    /**
     * NewsReplyKick 저장
     *
     * @param newsReplyKick 저장할 엔티티
     */
    public void save(NewsReplyKick newsReplyKick) {
        newsReplyKickRepository.save(newsReplyKick);
    }
    // endregion
}
