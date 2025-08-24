package kr.kickon.api.domain.newsKick;

import com.querydsl.core.types.dsl.BooleanExpression;

import kr.kickon.api.global.common.entities.News;
import kr.kickon.api.global.common.entities.NewsKick;
import kr.kickon.api.global.common.entities.QNewsKick;
import kr.kickon.api.global.common.enums.DataStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class NewsKickService{
    private final NewsKickRepository newsKickRepository;

    // region {findByPk} PK 기준으로 활성화된 뉴스 킥 조회
    /**
     * PK 기준으로 활성화된 뉴스 킥 조회
     */
    public NewsKick findByPk(Long pk) {
        BooleanExpression predicate = QNewsKick.newsKick.pk.eq(pk).and(QNewsKick.newsKick.status.eq(DataStatus.ACTIVATED));
        Optional<NewsKick> newsKickEntity = newsKickRepository.findOne(predicate);
        return newsKickEntity.orElse(null);
    }
    // endregion

    // region {findByNewsAndUser} 특정 뉴스와 유저 기준으로 활성화된 뉴스 킥 조회
    /**
     * 특정 뉴스와 유저 기준으로 활성화된 뉴스 킥 조회
     *
     * @param newsPk 뉴스 PK
     * @param userPk 유저 PK
     * @return 존재 시 NewsKick, 없으면 null
     */
    public NewsKick findByNewsAndUser(Long newsPk, Long userPk){
        BooleanExpression predicate = QNewsKick.newsKick.news.pk.eq(newsPk).and(QNewsKick.newsKick.user.pk.eq(userPk).and(QNewsKick.newsKick.status.eq(DataStatus.ACTIVATED)));
        Optional<NewsKick> newsKickEntity = newsKickRepository.findOne(predicate);
        return newsKickEntity.orElse(null);
    }
    // endregion

    // region {save} 뉴스 킥 저장
    /**
     * 뉴스 킥 저장
     */
    public void save(NewsKick newsKick) {
        newsKickRepository.save(newsKick);
    }
    // endregion

    // region 뉴스 킥 수 반환
    /**
     * News PK로 킥 수 계산
     *
     * @param news 뉴스 엔티티
     * @return Long 킥 수
     */
    public Long countKicksByNewsPk(News news) {
        return newsKickRepository.countByNews_PkAndStatus(news.getPk(), DataStatus.ACTIVATED);
    }
    // endregion
}
