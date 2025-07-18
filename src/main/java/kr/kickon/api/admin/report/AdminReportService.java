package kr.kickon.api.admin.report;

import com.mysema.commons.lang.Pair;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.transaction.Transactional;
import kr.kickon.api.admin.report.dto.AdminReportDetailDTO;
import kr.kickon.api.admin.report.dto.AdminReportItemDTO;
import kr.kickon.api.admin.report.request.UpdateReportStatusRequest;
import kr.kickon.api.domain.reportBoard.ReportBoardService;
import kr.kickon.api.domain.reportNews.ReportNewsService;
import kr.kickon.api.global.common.entities.*;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.error.exceptions.BadRequestException;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminReportService {
    private final JPAQueryFactory queryFactory;

    private ReportBoardService reportBoardService;
    private ReportNewsService reportNewsService;

    public List<AdminReportItemDTO> getReports(String type, String sort, int offset, int limit) {
        QReportBoard reportBoard = QReportBoard.reportBoard;
        QReportNews reportNews = QReportNews.reportNews;
        QUser user = QUser.user;
        QNews news = QNews.news;
        QBoard board = QBoard.board;
        // 1. 게시글 신고 목록 조회
        List<AdminReportItemDTO> boardReports = queryFactory
                .select(Projections.constructor(AdminReportItemDTO.class,
                        reportBoard.reportedBoard.pk,
                        Expressions.constant("BOARD"),
                        user.pk,
                        user.nickname,
                        board.title,
                        reportBoard.pk.count().as("reportCount"),
                        board.createdAt
                ))
                .from(reportBoard)
                .join(board).on(reportBoard.reportedBoard.pk.eq(board.pk))
                .join(user).on(board.user.pk.eq(user.pk))
                .where(reportBoard.status.eq(DataStatus.ACTIVATED)
                        .and(board.status.eq(DataStatus.ACTIVATED)))
                .groupBy(reportBoard.reportedBoard.pk, user.pk, user.nickname, board.title)
                .fetch();


        // 2. 뉴스 신고 목록 조회
        List<AdminReportItemDTO> newsReports = queryFactory
                .select(Projections.constructor(AdminReportItemDTO.class,
                        reportNews.reportedNews.pk,
                        Expressions.constant("NEWS"),
                        user.pk,
                        user.nickname,
                        news.title,
                        reportNews.pk.count().as("reportCount"),
                        news.createdAt
                ))
                .from(reportNews)
                .join(news).on(reportNews.reportedNews.pk.eq(news.pk))
                .join(user).on(news.user.pk.eq(user.pk))
                .where(reportNews.status.eq(DataStatus.ACTIVATED)
                        .and(news.status.eq(DataStatus.ACTIVATED)))
                .groupBy(reportNews.reportedNews.pk, user.pk, user.nickname, news.title)
                .fetch();

        // 3. 합치고 정렬
        Stream<AdminReportItemDTO> combined = Stream.concat(boardReports.stream(), newsReports.stream());

        List<AdminReportItemDTO> result = combined
                .filter(dto -> type == null || dto.getType().equals(type)) // type 필터
                .sorted((a, b) -> {
                    if (sort.equals("REPORT_COUNT")) {
                        return Long.compare(b.getReportCount(), a.getReportCount());
                    } else {
                        return b.getCreatedAt().compareTo(a.getCreatedAt());
                    }
                })
                .skip(offset)
                .limit(limit)
                .toList();
        return result;
    }

    public long countReports(String type) {
        QReportBoard reportBoard = QReportBoard.reportBoard;
        QReportNews reportNews = QReportNews.reportNews;
        QBoard board = QBoard.board;
        QNews news = QNews.news;

        long boardCount = 0;
        long newsCount = 0;

        if (type == null || type.equalsIgnoreCase("BOARD")) {
            boardCount = queryFactory
                    .select(reportBoard.reportedBoard.pk.countDistinct())
                    .from(reportBoard)
                    .join(board).on(reportBoard.reportedBoard.pk.eq(board.pk))
                    .where(
                            reportBoard.status.eq(DataStatus.ACTIVATED),
                            board.status.eq(DataStatus.ACTIVATED)
                    )
                    .fetchOne();
        }

        if (type == null || type.equalsIgnoreCase("NEWS")) {
            newsCount = queryFactory
                    .select(reportNews.reportedNews.pk.countDistinct())
                    .from(reportNews)
                    .join(news).on(reportNews.reportedNews.pk.eq(news.pk))
                    .where(
                            reportNews.status.eq(DataStatus.ACTIVATED),
                            news.status.eq(DataStatus.ACTIVATED)
                    )
                    .fetchOne();
        }

        return boardCount + newsCount;
    }

    public Pair<List<AdminReportDetailDTO>, Long> getReportDetailsByTarget(String type, Long targetPk, int offset, int limit) {
        QReportBoard reportBoard = QReportBoard.reportBoard;
        QReportNews reportNews = QReportNews.reportNews;
        QUser user = QUser.user;

        if (type.equalsIgnoreCase("BOARD")) {
            List<AdminReportDetailDTO> content = queryFactory
                    .select(Projections.constructor(AdminReportDetailDTO.class,
                            reportBoard.pk,
                            user.pk,
                            user.nickname,
                            reportBoard.reason,
                            reportBoard.reportStatus
                    ))
                    .from(reportBoard)
                    .join(user).on(reportBoard.user.pk.eq(user.pk))
                    .where(reportBoard.reportedBoard.pk.eq(targetPk).and(reportBoard.status.eq(DataStatus.ACTIVATED)))
                    .orderBy(reportBoard.createdAt.desc())
                    .offset(offset)
                    .limit(limit)
                    .fetch();

            Long total = queryFactory
                    .select(reportBoard.count())
                    .from(reportBoard)
                    .where(reportBoard.reportedBoard.pk.eq(targetPk).and(reportBoard.status.eq(DataStatus.ACTIVATED)))
                    .fetchOne();

            return Pair.of(content, total);
        } else {
            List<AdminReportDetailDTO> content = queryFactory
                    .select(Projections.constructor(AdminReportDetailDTO.class,
                            reportNews.pk,
                            user.pk,
                            user.nickname,
                            reportNews.reason,
                            reportNews.reportStatus
                    ))
                    .from(reportNews)
                    .join(user).on(reportNews.user.pk.eq(user.pk))
                    .where(reportNews.reportedNews.pk.eq(targetPk).and(reportNews.status.eq(DataStatus.ACTIVATED)))
                    .orderBy(reportNews.createdAt.desc())
                    .offset(offset)
                    .limit(limit)
                    .fetch();

            Long total = queryFactory
                    .select(reportNews.count())
                    .from(reportNews)
                    .where(reportNews.reportedNews.pk.eq(targetPk).and(reportNews.status.eq(DataStatus.ACTIVATED)))
                    .fetchOne();

            return Pair.of(content, total);
        }
    }

    @Transactional
    public void updateReportStatus(UpdateReportStatusRequest request) {
        String type = request.getType().toUpperCase();
        Long pk = request.getReportPk();

        if ("BOARD".equals(type)) {
            ReportBoard report = reportBoardService.findByPk(pk);
            if(report==null) throw new NotFoundException(ResponseCode.NOT_FOUND_REPORT_BOARD);
            report.setReportStatus(request.getReportStatus());
            reportBoardService.save(report);
        } else if ("NEWS".equals(type)) {
            ReportNews report = reportNewsService.findByPk(pk);
            if(report==null) throw new NotFoundException(ResponseCode.NOT_FOUND_REPORT_NEWS);
            report.setReportStatus(request.getReportStatus());
            reportNewsService.save(report);
        } else {
            throw new BadRequestException(ResponseCode.INVALID_REQUEST);
        }
    }
}