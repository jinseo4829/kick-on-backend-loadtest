package kr.kickon.api.domain.newsKick;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.kickon.api.global.common.BaseService;
import kr.kickon.api.global.common.entities.BoardKick;
import kr.kickon.api.global.common.entities.NewsKick;
import kr.kickon.api.global.common.entities.QBoardKick;
import kr.kickon.api.global.common.entities.QNewsKick;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.util.UUIDGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class NewsKickService implements BaseService<NewsKick> {
    private final NewsKickRepository newsKickRepository;
    private final JPAQueryFactory queryFactory;
    private final UUIDGenerator uuidGenerator;

    @Override
    public NewsKick findById(String uuid) {
        BooleanExpression predicate = QNewsKick.newsKick.id.eq(uuid).and(QNewsKick.newsKick.status.eq(DataStatus.ACTIVATED));
        Optional<NewsKick> newsKick = newsKickRepository.findOne(predicate);
        return newsKick.orElse(null);
    }

    @Override
    public NewsKick findByPk(Long pk) {
        BooleanExpression predicate = QNewsKick.newsKick.pk.eq(pk).and(QNewsKick.newsKick.status.eq(DataStatus.ACTIVATED));
        Optional<NewsKick> newsKick = newsKickRepository.findOne(predicate);
        return newsKick.orElse(null);
    }

    public NewsKick findByNewsAndUser(Long newsPk, Long userPk){
        BooleanExpression predicate = QNewsKick.newsKick.news.pk.eq(newsPk).and(QNewsKick.newsKick.user.pk.eq(userPk));
        Optional<NewsKick> newsKick = newsKickRepository.findOne(predicate);
        return newsKick.orElse(null);
    }

    public void save(NewsKick newsKick) {
        newsKickRepository.save(newsKick);
    }
}
