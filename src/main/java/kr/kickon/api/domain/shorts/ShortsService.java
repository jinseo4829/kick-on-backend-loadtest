package kr.kickon.api.domain.shorts;

import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import kr.kickon.api.domain.awsFileReference.AwsFileReferenceService;
import kr.kickon.api.domain.board.BoardService;
import kr.kickon.api.domain.boardKick.BoardKickService;
import kr.kickon.api.domain.boardReply.BoardReplyService;
import kr.kickon.api.domain.boardViewHistory.BoardViewHistoryService;
import kr.kickon.api.domain.news.NewsService;
import kr.kickon.api.domain.newsKick.NewsKickService;
import kr.kickon.api.domain.newsReply.NewsReplyService;
import kr.kickon.api.domain.newsViewHistory.NewsViewHistoryService;
import kr.kickon.api.domain.shorts.dto.ShortsDTO;
import kr.kickon.api.domain.shorts.dto.ShortsDetailDTO;
import kr.kickon.api.domain.shorts.request.GetShortsRequest;
import kr.kickon.api.global.common.entities.AwsFileReference;
import kr.kickon.api.global.common.entities.Board;
import kr.kickon.api.global.common.entities.News;
import kr.kickon.api.global.common.entities.QAwsFileReference;
import kr.kickon.api.global.common.entities.QBoard;
import kr.kickon.api.global.common.entities.QBoardKick;
import kr.kickon.api.global.common.entities.QBoardViewHistory;
import kr.kickon.api.global.common.entities.QNews;
import kr.kickon.api.global.common.entities.QNewsKick;
import kr.kickon.api.global.common.entities.QNewsViewHistory;
import kr.kickon.api.global.common.entities.User;
import kr.kickon.api.global.common.enums.ShortsSortType;
import kr.kickon.api.global.common.enums.UsedInType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ShortsService {

  private final AwsFileReferenceService awsFileReferenceService;
  private final BoardViewHistoryService boardViewHistoryService;
  private final NewsViewHistoryService newsViewHistoryService;
  private final BoardKickService boardKickService;
  private final NewsKickService newsKickService;
  private final BoardService boardService;
  private final NewsService newsService;
  private final JPAQueryFactory queryFactory;
  private final BoardReplyService boardReplyService;
  private final NewsReplyService newsReplyService;

  // region 배너 쇼츠 리스트 조회
  /**
   * 배너 Shorts 영상 리스트를 조회합니다.
   * - AwsFileReference 엔티티 중 동영상 파일(MP4, MOV, AVI, MKV)만 필터링
   * - 1) 조회수 2) 킥 수 기준으로 내림차순 정렬 후 상위 4개만 반환합니다.
   * * @return ShortsDTO 리스트
   */
  public List<ShortsDTO> getFixedShorts() {
    return queryShorts(4);
  }
  // endregion

  // region 쇼츠 리스트 조회
  /**
   * Shorts 영상 리스트를 조회합니다.
   * @param request 정렬 기준, 페이징 크기
   * @param pageable 페이징
   * @return ShortsDTO 리스트
   */
  public Page<ShortsDTO> getShortsWithPagination(GetShortsRequest request, Pageable pageable) {
    QAwsFileReference awsFileReference = QAwsFileReference.awsFileReference;
    List<ShortsDTO> combined = queryShorts(null);

    Long totalCount = queryFactory
        .select(awsFileReference.count())
        .from(awsFileReference)
        .where(
            awsFileReference.s3Key.endsWithIgnoreCase(".mp4")
                .or(awsFileReference.s3Key.endsWithIgnoreCase(".mov"))
                .or(awsFileReference.s3Key.endsWithIgnoreCase(".avi"))
                .or(awsFileReference.s3Key.endsWithIgnoreCase(".mkv"))
        )
        .fetchOne();

    long total = totalCount != null ? totalCount : 0L;

    ShortsSortType sortType = request.getSort();
    if (sortType == null) {
      sortType = ShortsSortType.CREATED_DESC; // 기본 정렬 기준
    }
    // 정렬 조건
    Comparator<ShortsDTO> comparator = switch (sortType) {
      case CREATED_ASC -> Comparator.comparing(ShortsDTO::getCreatedAt);
      case POPULAR -> Comparator.comparingLong(ShortsDTO::getSortViewCount).reversed()
          .thenComparing(Comparator.comparingLong(ShortsDTO::getSortKickCount).reversed());
      default -> Comparator.comparing(ShortsDTO::getCreatedAt).reversed(); // 기본: 최신순
    };
    combined.sort(comparator);
    return new PageImpl<>(combined, pageable, total);
  }
  //endregion

  // region 쇼츠 리스트 조회
  /**
   * Shorts 영상 리스트를 조회하는 공통 로직입니다.
   * @param limit 조회 엔티티 수
   * @return ShortsDTO 리스트
   */
  private List<ShortsDTO> queryShorts(@Nullable Integer limit) {
    QAwsFileReference awsFileReference = QAwsFileReference.awsFileReference;
    QBoard board = QBoard.board;
    QNews news = QNews.news;
    QBoardViewHistory boardViewHistory = QBoardViewHistory.boardViewHistory;
    QBoardKick boardKick = QBoardKick.boardKick;
    QNewsViewHistory newsViewHistory = QNewsViewHistory.newsViewHistory;
    QNewsKick newsKick = QNewsKick.newsKick;

    // 게시글/뉴스 각각의 48시간 기준 조회수, 킥 수를 가져옵니다.
    LocalDateTime cutoff = LocalDateTime.now().minusHours(48);

    // Board Shorts
    List<ShortsDTO> boardShorts = queryFactory
        .select(Projections.constructor(ShortsDTO.class,
            awsFileReference.pk,
            awsFileReference.s3Key,
            awsFileReference.usedIn,
            awsFileReference.referencePk,
            board.title,
            ExpressionUtils.as(JPAExpressions
                .select(boardViewHistory.pk.count())
                .from(boardViewHistory)
                .where(boardViewHistory.board.pk.eq(board.pk)), "totalViewCount"),
            ExpressionUtils.as(JPAExpressions
                .select(boardKick.pk.count())
                .from(boardKick)
                .where(boardKick.board.pk.eq(board.pk)), "totalKickCount"),
            ExpressionUtils.as(JPAExpressions
                .select(boardViewHistory.pk.count())
                .from(boardViewHistory)
                .where(boardViewHistory.board.pk.eq(board.pk)
                    .and(boardViewHistory.createdAt.after(cutoff))), "recentViewCount"),
            ExpressionUtils.as(JPAExpressions
                    .select(boardKick.pk.count())
                    .from(boardKick)
                    .where(boardKick.board.pk.eq(board.pk).and(boardKick.createdAt.after(cutoff))),
                "recentKickCount"),
            awsFileReference.createdAt
        ))
        .from(awsFileReference)
        .leftJoin(board).on(awsFileReference.referencePk.eq(board.pk))
        .where(
            awsFileReference.usedIn.eq(UsedInType.BOARD),
            awsFileReference.s3Key.endsWithIgnoreCase(".mp4")
                .or(awsFileReference.s3Key.endsWithIgnoreCase(".mov"))
                .or(awsFileReference.s3Key.endsWithIgnoreCase(".avi"))
                .or(awsFileReference.s3Key.endsWithIgnoreCase(".mkv"))
        )
        .fetch();

    // News Shorts
    List<ShortsDTO> newsShorts = queryFactory
        .select(Projections.constructor(ShortsDTO.class,
            awsFileReference.pk,
            awsFileReference.s3Key,
            awsFileReference.usedIn,
            awsFileReference.referencePk,
            news.title,
            ExpressionUtils.as(JPAExpressions
                .select(newsViewHistory.pk.count())
                .from(newsViewHistory)
                .where(newsViewHistory.news.pk.eq(news.pk)), "totalViewCount"),
            ExpressionUtils.as(JPAExpressions
                .select(newsKick.pk.count())
                .from(newsKick)
                .where(newsKick.news.pk.eq(news.pk)), "totalKickCount"),
            ExpressionUtils.as(JPAExpressions
                .select(newsViewHistory.pk.count())
                .from(newsViewHistory)
                .where(newsViewHistory.news.pk.eq(news.pk)
                    .and(newsViewHistory.createdAt.after(cutoff))), "recentViewCount"),
            ExpressionUtils.as(JPAExpressions
                    .select(newsKick.pk.count())
                    .from(newsKick)
                    .where(newsKick.news.pk.eq(news.pk).and(newsKick.createdAt.after(cutoff))),
                "recentKickCount"),
            awsFileReference.createdAt
        ))
        .from(awsFileReference)
        .leftJoin(news).on(awsFileReference.referencePk.eq(news.pk))
        .where(
            awsFileReference.usedIn.eq(UsedInType.NEWS),
            awsFileReference.s3Key.endsWithIgnoreCase(".mp4")
                .or(awsFileReference.s3Key.endsWithIgnoreCase(".mov"))
                .or(awsFileReference.s3Key.endsWithIgnoreCase(".avi"))
                .or(awsFileReference.s3Key.endsWithIgnoreCase(".mkv"))
        )
        .fetch();

    // 게시글/뉴스를 unionAll로 통합
    List<ShortsDTO> combined = new ArrayList<>();
    combined.addAll(boardShorts);
    combined.addAll(newsShorts);

    if (limit != null) {
      return combined.stream()
          .sorted(
              Comparator.comparingLong(ShortsDTO::getSortViewCount).reversed()
                  .thenComparing(Comparator.comparingLong(ShortsDTO::getSortKickCount).reversed())
          )
          .limit(limit)
          .collect(Collectors.toList());
    }

    return combined;
  }
  //endregion

  // region 쇼츠 상세 조회
  /**
   *
   * @param file AwsFileReference 영상 엔티티
   * @return ShortsDetailDTO
   */
  @Transactional
  public ShortsDetailDTO getShortsDetail(AwsFileReference file){
    Long viewCount = 0L;
    Long kickCount = 0L;
    Long replyCount = 0L;
    String title = null;
    User user = null;

    if (file.getUsedIn() == UsedInType.BOARD) {
      viewCount = boardViewHistoryService.countViewsByBoardPk(file.getReferencePk());
      kickCount = boardKickService.countKicksByBoardPk(file.getReferencePk());
      replyCount = boardReplyService.countRepliesByBoardPk(file.getReferencePk());

      Board board = boardService.findByPk(file.getReferencePk());
      if (board != null) {
        title = board.getTitle();
        user = board.getUser();
      }
    } else if (file.getUsedIn() == UsedInType.NEWS) {
      viewCount = newsViewHistoryService.countViewsByNewsPk(file.getReferencePk());
      kickCount = newsKickService.countKicksByNewsPk(file.getReferencePk());
      replyCount = newsReplyService.countRepliesByNewsPk(file.getReferencePk());

      News news = newsService.findByPk(file.getReferencePk());
      if (news != null) {
        title = news.getTitle();
        user = news.getUser();
      }
    }

    return ShortsDetailDTO.fromEntity(file, viewCount, kickCount, replyCount, title, user);
  }
  //endregion
}