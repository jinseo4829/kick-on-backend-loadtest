package kr.kickon.api.domain.reportBoard;

import com.querydsl.core.types.dsl.BooleanExpression;
import kr.kickon.api.global.common.entities.ReportBoard;
import kr.kickon.api.global.common.entities.QReportBoard;
import kr.kickon.api.global.common.enums.DataStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReportBoardService {
    private final ReportBoardRepository reportBoardRepository;

    public ReportBoard findByPk(Long pk) {
        BooleanExpression predicate = QReportBoard.reportBoard.pk.eq(pk).and(QReportBoard.reportBoard.status.eq(DataStatus.ACTIVATED));
        Optional<ReportBoard> reportBoard = reportBoardRepository.findOne(predicate);
        return reportBoard.orElse(null);
    }

    public void save(ReportBoard reportBoard) {
        reportBoardRepository.save(reportBoard);
    }
}
