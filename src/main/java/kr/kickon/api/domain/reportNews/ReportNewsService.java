package kr.kickon.api.domain.reportNews;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.kickon.api.global.common.BaseService;
import kr.kickon.api.global.common.entities.QReportNews;
import kr.kickon.api.global.common.entities.ReportNews;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.util.UUIDGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReportNewsService implements BaseService<ReportNews> {
    private final ReportNewsRepository reportNewsRepository;
    private final JPAQueryFactory queryFactory;
    private final UUIDGenerator uuidGenerator;

    @Override
    public ReportNews findById(String uuid) {
        BooleanExpression predicate = QReportNews.reportNews.id.eq(uuid).and(QReportNews.reportNews.status.eq(DataStatus.ACTIVATED));
        Optional<ReportNews> reportNews = reportNewsRepository.findOne(predicate);
        return reportNews.orElse(null);
    }

    @Override
    public ReportNews findByPk(Long pk) {
        BooleanExpression predicate = QReportNews.reportNews.pk.eq(pk).and(QReportNews.reportNews.status.eq(DataStatus.ACTIVATED));
        Optional<ReportNews> reportNews = reportNewsRepository.findOne(predicate);
        return reportNews.orElse(null);
    }

    public void save(ReportNews reportNews) {
        reportNewsRepository.save(reportNews);
    }
}
