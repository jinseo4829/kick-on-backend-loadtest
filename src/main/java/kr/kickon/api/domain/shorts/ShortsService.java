package kr.kickon.api.domain.shorts;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import kr.kickon.api.domain.awsFileReference.AwsFileReferenceService;
import kr.kickon.api.domain.embeddedLink.EmbeddedLinkService;
import kr.kickon.api.domain.shorts.dto.ShortsDTO;
import kr.kickon.api.domain.shorts.dto.ShortsDetailDTO;
import kr.kickon.api.domain.shorts.request.GetShortsRequest;
import kr.kickon.api.global.common.entities.QAwsFileReference;
import kr.kickon.api.global.common.entities.QBoard;
import kr.kickon.api.global.common.entities.QBoardKick;
import kr.kickon.api.global.common.entities.QBoardReply;
import kr.kickon.api.global.common.entities.QBoardViewHistory;
import kr.kickon.api.global.common.entities.QEmbeddedLink;
import kr.kickon.api.global.common.entities.QNews;
import kr.kickon.api.global.common.entities.QNewsKick;
import kr.kickon.api.global.common.entities.QNewsReply;
import kr.kickon.api.global.common.entities.QNewsViewHistory;
import kr.kickon.api.global.common.entities.QShorts;
import kr.kickon.api.global.common.entities.Shorts;
import kr.kickon.api.global.common.entities.User;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.common.enums.ShortsSortType;
import kr.kickon.api.global.common.enums.ShortsType;
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

  private final JPAQueryFactory queryFactory;
  private final ShortsRepository shortsRepository;
  private final AwsFileReferenceService awsFileReferenceService;
  private final EmbeddedLinkService embeddedLinkService;

  public Shorts findByPk(Long pk) {
    BooleanExpression predicate = QShorts.shorts.pk.eq(pk).and(QShorts.shorts.status.eq(
        DataStatus.ACTIVATED));
    Optional<Shorts> shorts = shortsRepository.findOne(predicate);
    return shorts.orElse(null);
  }

  public UsedInType getUsedIn(Shorts shorts) {
    if (shorts.getType() == ShortsType.AWS_FILE) {
      return awsFileReferenceService.findByPk(shorts.getReferencePk())
          .getUsedIn();
    } else {
      return embeddedLinkService.findByPk(shorts.getReferencePk())
          .getUsedIn();
    }
  }

  // region 배너 쇼츠 리스트 조회
  /**
   * 배너 Shorts 영상 리스트를 조회합니다.
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
    QShorts shorts = QShorts.shorts;
    List<ShortsDTO> combined = queryShorts(null);

    Long totalCount = queryFactory
        .select(shorts.count())
        .from(shorts)
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
    QEmbeddedLink embeddedLink = QEmbeddedLink.embeddedLink;
    QShorts shorts = QShorts.shorts;

    // 게시글/뉴스 각각의 48시간 기준 조회수, 킥 수를 가져옵니다.
    LocalDateTime cutoff = LocalDateTime.now().minusHours(48);

    // 공통 expression
    Expression<String> videoUrlExpression = new CaseBuilder()
        .when(shorts.type.eq(ShortsType.AWS_FILE))
        .then(Expressions.stringTemplate(
            "CONCAT('https://kickon-files-bucket.s3.ap-northeast-2.amazonaws.com/', {0})",
            awsFileReference.s3Key))
        .otherwise(embeddedLink.url);

    Expression<UsedInType> usedInExpression = new CaseBuilder()
        .when(shorts.type.eq(ShortsType.AWS_FILE))
        .then(awsFileReference.usedIn)
        .otherwise(embeddedLink.usedIn);

    NumberExpression<Long> referencePkExpression = new CaseBuilder()
        .when((shorts.type.eq(ShortsType.AWS_FILE)
            .and(awsFileReference.usedIn.eq(UsedInType.BOARD)))
            .or(shorts.type.eq(ShortsType.EMBEDDED_LINK)
                .and(embeddedLink.usedIn.eq(UsedInType.BOARD))))
        .then(board.pk)
        .when((shorts.type.eq(ShortsType.AWS_FILE)
            .and(awsFileReference.usedIn.eq(UsedInType.NEWS)))
            .or(shorts.type.eq(ShortsType.EMBEDDED_LINK)
                .and(embeddedLink.usedIn.eq(UsedInType.NEWS))))
        .then(news.pk)
        .otherwise((NumberExpression<Long>) null);

    // CASE: title
    StringExpression titleExpression = new CaseBuilder()
        .when((shorts.type.eq(ShortsType.AWS_FILE)
            .and(awsFileReference.usedIn.eq(UsedInType.BOARD)))
            .or(shorts.type.eq(ShortsType.EMBEDDED_LINK)
                .and(embeddedLink.usedIn.eq(UsedInType.BOARD))))
        .then(board.title)
        .when((shorts.type.eq(ShortsType.AWS_FILE)
            .and(awsFileReference.usedIn.eq(UsedInType.NEWS)))
            .or(shorts.type.eq(ShortsType.EMBEDDED_LINK)
                .and(embeddedLink.usedIn.eq(UsedInType.NEWS))))
        .then(news.title)
        .otherwise((StringExpression) null);

    // CASE: totalViewCount
    Expression<Long> totalViewCountExpression = new CaseBuilder()
        .when((shorts.type.eq(ShortsType.AWS_FILE)
            .and(awsFileReference.usedIn.eq(UsedInType.BOARD)))
            .or(shorts.type.eq(ShortsType.EMBEDDED_LINK)
                .and(embeddedLink.usedIn.eq(UsedInType.BOARD))))
        .then(JPAExpressions.select(boardViewHistory.pk.count())
            .from(boardViewHistory)
            .where(boardViewHistory.board.pk.eq(board.pk)))
        .when((shorts.type.eq(ShortsType.AWS_FILE)
            .and(awsFileReference.usedIn.eq(UsedInType.NEWS)))
            .or(shorts.type.eq(ShortsType.EMBEDDED_LINK)
                .and(embeddedLink.usedIn.eq(UsedInType.NEWS))))
        .then(JPAExpressions.select(newsViewHistory.pk.count())
            .from(newsViewHistory)
            .where(newsViewHistory.news.pk.eq(news.pk)))
        .otherwise(0L);

    // CASE: totalKickCount
    Expression<Long> totalKickCountExpression = new CaseBuilder()
        .when((shorts.type.eq(ShortsType.AWS_FILE)
            .and(awsFileReference.usedIn.eq(UsedInType.BOARD)))
            .or(shorts.type.eq(ShortsType.EMBEDDED_LINK)
                .and(embeddedLink.usedIn.eq(UsedInType.BOARD))))
        .then(JPAExpressions.select(boardKick.pk.count())
            .from(boardKick)
            .where(boardKick.board.pk.eq(board.pk)))
        .when((shorts.type.eq(ShortsType.AWS_FILE)
            .and(awsFileReference.usedIn.eq(UsedInType.NEWS)))
            .or(shorts.type.eq(ShortsType.EMBEDDED_LINK)
                .and(embeddedLink.usedIn.eq(UsedInType.NEWS))))
        .then(JPAExpressions.select(newsKick.pk.count())
            .from(newsKick)
            .where(newsKick.news.pk.eq(news.pk)))
        .otherwise(0L);

    // CASE: recentViewCount
    Expression<Long> recentViewCountExpression = new CaseBuilder()
        .when((shorts.type.eq(ShortsType.AWS_FILE)
            .and(awsFileReference.usedIn.eq(UsedInType.BOARD)))
            .or(shorts.type.eq(ShortsType.EMBEDDED_LINK)
                .and(embeddedLink.usedIn.eq(UsedInType.BOARD))))
        .then(JPAExpressions.select(boardViewHistory.pk.count())
            .from(boardViewHistory)
            .where(boardViewHistory.board.pk.eq(board.pk)
                .and(boardViewHistory.createdAt.after(cutoff))))
        .when((shorts.type.eq(ShortsType.AWS_FILE)
            .and(awsFileReference.usedIn.eq(UsedInType.NEWS)))
            .or(shorts.type.eq(ShortsType.EMBEDDED_LINK)
                .and(embeddedLink.usedIn.eq(UsedInType.NEWS))))
        .then(JPAExpressions.select(newsViewHistory.pk.count())
            .from(newsViewHistory)
            .where(newsViewHistory.news.pk.eq(news.pk)
                .and(newsViewHistory.createdAt.after(cutoff))))
        .otherwise(0L);

    // CASE: recentKickCount
    Expression<Long> recentKickCountExpression = new CaseBuilder()
        .when((shorts.type.eq(ShortsType.AWS_FILE)
            .and(awsFileReference.usedIn.eq(UsedInType.BOARD)))
            .or(shorts.type.eq(ShortsType.EMBEDDED_LINK)
                .and(embeddedLink.usedIn.eq(UsedInType.BOARD))))
        .then(JPAExpressions.select(boardKick.pk.count())
            .from(boardKick)
            .where(boardKick.board.pk.eq(board.pk)
                .and(boardKick.createdAt.after(cutoff))))
        .when((shorts.type.eq(ShortsType.AWS_FILE)
            .and(awsFileReference.usedIn.eq(UsedInType.NEWS)))
            .or(shorts.type.eq(ShortsType.EMBEDDED_LINK)
                .and(embeddedLink.usedIn.eq(UsedInType.NEWS))))
        .then(JPAExpressions.select(newsKick.pk.count())
            .from(newsKick)
            .where(newsKick.news.pk.eq(news.pk)
                .and(newsKick.createdAt.after(cutoff))))
        .otherwise(0L);

    List<ShortsDTO> result = queryFactory
        .select(Projections.constructor(ShortsDTO.class,
            shorts.pk,
            videoUrlExpression,
            usedInExpression,
            referencePkExpression,
            titleExpression,
            totalViewCountExpression,
            totalKickCountExpression,
            recentViewCountExpression,
            recentKickCountExpression,
            shorts.createdAt
        ))
        .from(shorts)
        .leftJoin(awsFileReference).on(shorts.type.eq(ShortsType.AWS_FILE)
            .and(shorts.referencePk.eq(awsFileReference.pk)))
        .leftJoin(embeddedLink).on(shorts.type.eq(ShortsType.EMBEDDED_LINK)
            .and(shorts.referencePk.eq(embeddedLink.pk)))
        .leftJoin(board).on(shorts.type.eq(ShortsType.AWS_FILE)
            .and(awsFileReference.referencePk.eq(board.pk))
            .or(shorts.type.eq(ShortsType.EMBEDDED_LINK)
                .and(embeddedLink.referencePk.eq(board.pk))))
        .leftJoin(news).on(shorts.type.eq(ShortsType.AWS_FILE)
            .and(awsFileReference.referencePk.eq(news.pk))
            .or(shorts.type.eq(ShortsType.EMBEDDED_LINK)
                .and(embeddedLink.referencePk.eq(news.pk))))
        .fetch();


    if (limit != null) {
      return result.stream()
          .sorted(
              Comparator.comparingLong(ShortsDTO::getSortViewCount).reversed()
                  .thenComparing(Comparator.comparingLong(ShortsDTO::getSortKickCount).reversed())
          )
          .limit(limit)
          .collect(Collectors.toList());
    }

    return result;
  }
  //endregion

  // region 쇼츠 상세 조회
  /**
   *
   * @param file AwsFileReference 영상 엔티티
   * @return ShortsDetailDTO
   */
  @Transactional
  public ShortsDetailDTO getShortsDetail(Shorts file) {
    QAwsFileReference awsFileReference = QAwsFileReference.awsFileReference;
    QBoard board = QBoard.board;
    QNews news = QNews.news;
    QBoardViewHistory boardViewHistory = QBoardViewHistory.boardViewHistory;
    QBoardKick boardKick = QBoardKick.boardKick;
    QBoardReply boardReply = QBoardReply.boardReply;
    QNewsViewHistory newsViewHistory = QNewsViewHistory.newsViewHistory;
    QNewsKick newsKick = QNewsKick.newsKick;
    QNewsReply newsReply = QNewsReply.newsReply;
    QEmbeddedLink embeddedLink = QEmbeddedLink.embeddedLink;
    QShorts shorts = QShorts.shorts;
    UsedInType usedInType = getUsedIn(file);

    // 공통 expression
    Expression<String> videoUrlExpression = new CaseBuilder()
        .when(shorts.type.eq(ShortsType.AWS_FILE))
        .then(Expressions.stringTemplate(
            "CONCAT('https://kickon-files-bucket.s3.ap-northeast-2.amazonaws.com/', {0})",
            awsFileReference.s3Key))
        .otherwise(embeddedLink.url);

    Expression<UsedInType> usedInExpression = new CaseBuilder()
        .when(shorts.type.eq(ShortsType.AWS_FILE))
        .then(awsFileReference.usedIn)
        .otherwise(embeddedLink.usedIn);

    // 공통 서브쿼리
    Expression<Long> totalViewCount;
    Expression<Long> totalKickCount;
    Expression<Long> totalReplyCount;
    Expression<User> userExpression;
    Expression<String> titleExpression;
    Expression<Long> referencePkExpression;

    if (usedInType == UsedInType.BOARD) {
      totalViewCount = JPAExpressions.select(boardViewHistory.pk.count().coalesce(0L))
          .from(boardViewHistory)
          .where(boardViewHistory.board.pk.eq(board.pk));
      totalKickCount = JPAExpressions.select(boardKick.pk.count().coalesce(0L))
          .from(boardKick)
          .where(boardKick.board.pk.eq(board.pk));
      totalReplyCount = JPAExpressions.select(boardReply.pk.count().coalesce(0L))
          .from(boardReply)
          .where(boardReply.board.pk.eq(board.pk));
      userExpression = board.user;
      titleExpression = board.title;
      referencePkExpression = board.pk;
    } else { // NEWS
      totalViewCount = JPAExpressions.select(newsViewHistory.pk.count().coalesce(0L))
          .from(newsViewHistory)
          .where(newsViewHistory.news.pk.eq(news.pk));
      totalKickCount = JPAExpressions.select(newsKick.pk.count().coalesce(0L))
          .from(newsKick)
          .where(newsKick.news.pk.eq(news.pk));
      totalReplyCount = JPAExpressions.select(newsReply.pk.count().coalesce(0L))
          .from(newsReply)
          .where(newsReply.news.pk.eq(news.pk));
      userExpression = news.user;
      titleExpression = news.title;
      referencePkExpression = news.pk;
    }

    JPAQuery<ShortsDetailDTO> query = queryFactory
        .select(Projections.constructor(ShortsDetailDTO.class,
            shorts.pk,
            videoUrlExpression,
            usedInExpression,
            referencePkExpression,
            titleExpression,
            totalViewCount,
            totalKickCount,
            totalReplyCount,
            shorts.createdAt,
            userExpression
        ))
        .from(shorts)
        .where(shorts.pk.eq(file.getPk()));

    query.leftJoin(awsFileReference).on(shorts.type.eq(ShortsType.AWS_FILE)
        .and(shorts.referencePk.eq(awsFileReference.pk)));
    query.leftJoin(embeddedLink).on(shorts.type.eq(ShortsType.EMBEDDED_LINK)
        .and(shorts.referencePk.eq(embeddedLink.pk)));

    if (usedInType == UsedInType.BOARD) {
      query.leftJoin(board).on(shorts.type.eq(ShortsType.AWS_FILE)
          .and(awsFileReference.referencePk.eq(board.pk))
          .or(shorts.type.eq(ShortsType.EMBEDDED_LINK)
              .and(embeddedLink.referencePk.eq(board.pk))));
    } else {
      query.leftJoin(news).on(shorts.type.eq(ShortsType.AWS_FILE)
          .and(awsFileReference.referencePk.eq(news.pk))
          .or(shorts.type.eq(ShortsType.EMBEDDED_LINK)
              .and(embeddedLink.referencePk.eq(news.pk))));
    }

    return query.fetchOne();
  }
  //endregion

  @Transactional
  public void save(ShortsType type, Long referencePk) {
    Shorts shorts = Shorts.builder()
        .id(UUID.randomUUID().toString())
        .type(type)
        .referencePk(referencePk)
        .build();
    shortsRepository.save(shorts);
  }

}