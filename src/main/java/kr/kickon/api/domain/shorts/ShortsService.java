package kr.kickon.api.domain.shorts;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.*;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.annotation.Nullable;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
import org.springframework.util.StopWatch;

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
      StopWatch stopWatch = new StopWatch();
      stopWatch.start();

      List<ShortsDTO> result = queryShorts(4);

      stopWatch.stop();
      log.info("getFixedShorts() 실행 시간: {} ms", stopWatch.getTotalTimeMillis());

      return result;
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
        .where(shorts.status.eq(DataStatus.ACTIVATED))
        .fetchOne();

    long total = totalCount != null ? totalCount : 0L;

    ShortsSortType sortType = Optional.ofNullable(request.getSort()).orElse(ShortsSortType.CREATED_DESC);

    // 정렬 조건
    Comparator<ShortsDTO> comparator = switch (sortType) {
      case CREATED_ASC -> Comparator.comparing(ShortsDTO::getCreatedAt);
      case POPULAR -> Comparator.comparingLong(ShortsDTO::getRecentViewCount).reversed()
                        .thenComparing(Comparator.comparingLong(ShortsDTO::getRecentKickCount).reversed())
                        .thenComparing(Comparator.comparing(ShortsDTO::getCreatedAt).reversed()); // 최신순
        default -> Comparator.comparing(ShortsDTO::getCreatedAt).reversed(); // 기본: 최신순
    };

    List<ShortsDTO> sortedShorts = combined.stream()
        .sorted(comparator)
        .toList();

    int start = (int) pageable.getOffset();
    int end = Math.min(start + pageable.getPageSize(), sortedShorts.size());
    List<ShortsDTO> pageContent = start > end ? Collections.emptyList() : sortedShorts.subList(start, end);
    return new PageImpl<>(pageContent, pageable, total);
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

      // 1. Board 관련 조회수/킥 수 집계
      Expression<Tuple> boardAggregates = JPAExpressions
              .select(
                      boardViewHistory.board.pk.as("boardPk"),
                      boardViewHistory.pk.count().as("totalViewCount"),
                      JPAExpressions.select(boardViewHistory.pk.count())
                              .from(boardViewHistory)
                              .where(boardViewHistory.board.pk.eq(board.pk)
                                      .and(boardViewHistory.createdAt.after(cutoff))
                              .as("recentViewCount")),
                      boardKick.pk.count().as("totalKickCount"),
                      JPAExpressions.select(boardKick.pk.count())
                              .from(boardKick)
                              .where(boardKick.board.pk.eq(board.pk)
                                      .and(boardKick.status.eq(DataStatus.ACTIVATED))
                                      .and(boardKick.createdAt.after(cutoff))
                              .as("recentKickCount")
              ))
              .from(board)
              .leftJoin(boardViewHistory).on(boardViewHistory.board.pk.eq(board.pk))
              .leftJoin(boardKick).on(boardKick.board.pk.eq(board.pk))
              .groupBy(board.pk);

      // 2. News 관련 조회수/킥 수 집계
      Expression<Tuple> newsAggregates = JPAExpressions
              .select(
                      newsViewHistory.news.pk.as("newsPk"),
                      newsViewHistory.pk.count().as("totalViewCount"),
                      JPAExpressions.select(newsViewHistory.pk.count())
                              .from(newsViewHistory)
                              .where(newsViewHistory.news.pk.eq(news.pk)
                                      .and(newsViewHistory.createdAt.after(cutoff))
                              .as("recentViewCount")),
                      newsKick.pk.count().as("totalKickCount"),
                      JPAExpressions.select(newsKick.pk.count())
                              .from(newsKick)
                              .where(newsKick.news.pk.eq(news.pk)
                                      .and(newsKick.status.eq(DataStatus.ACTIVATED))
                                      .and(newsKick.createdAt.after(cutoff))
                              .as("recentKickCount")
              ))
              .from(news)
              .leftJoin(newsViewHistory).on(newsViewHistory.news.pk.eq(news.pk))
              .leftJoin(newsKick).on(newsKick.news.pk.eq(news.pk))
              .groupBy(news.pk);

    // 공통 expression
    Expression<String> videoUrlExpression = new CaseBuilder()
        .when(shorts.type.eq(ShortsType.AWS_FILE))
        .then(Expressions.stringTemplate(
            "CONCAT('https://kickon-files-bucket.s3.ap-northeast-2.amazonaws.com/', {0})",
            awsFileReference.s3Key))
        .otherwise(
            Expressions.stringTemplate(
                "REPLACE({0}, 'youtube.com/embed/', 'youtube.com/watch?v=')",
                embeddedLink.url
            ));

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
            .where(boardKick.board.pk.eq(board.pk)
                .and(boardKick.status.eq(DataStatus.ACTIVATED))))
        .when((shorts.type.eq(ShortsType.AWS_FILE)
            .and(awsFileReference.usedIn.eq(UsedInType.NEWS)))
            .or(shorts.type.eq(ShortsType.EMBEDDED_LINK)
                .and(embeddedLink.usedIn.eq(UsedInType.NEWS))))
        .then(JPAExpressions.select(newsKick.pk.count())
            .from(newsKick)
            .where(newsKick.news.pk.eq(news.pk)
                .and(newsKick.status.eq(DataStatus.ACTIVATED))))
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
                .and(boardKick.createdAt.after(cutoff))
            .and(boardKick.status.eq(DataStatus.ACTIVATED))))
        .when((shorts.type.eq(ShortsType.AWS_FILE)
            .and(awsFileReference.usedIn.eq(UsedInType.NEWS)))
            .or(shorts.type.eq(ShortsType.EMBEDDED_LINK)
                .and(embeddedLink.usedIn.eq(UsedInType.NEWS))))
        .then(JPAExpressions.select(newsKick.pk.count())
            .from(newsKick)
            .where(newsKick.news.pk.eq(news.pk)
                .and(newsKick.createdAt.after(cutoff))
            .and(newsKick.status.eq(DataStatus.ACTIVATED))))
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
            .leftJoin(boardViewHistory).on(
                    board.pk.eq(boardViewHistory.board.pk)
            )
            .leftJoin(newsViewHistory).on(
                    news.pk.eq(newsViewHistory.news.pk)
            )
            .leftJoin(boardKick).on(
                    board.pk.eq(boardKick.board.pk).and(boardKick.status.eq(DataStatus.ACTIVATED))
            )
            .leftJoin(newsKick).on(
                    news.pk.eq(newsKick.news.pk).and(newsKick.status.eq(DataStatus.ACTIVATED))
            )
            .where(shorts.status.eq(DataStatus.ACTIVATED))
            .groupBy(
                    shorts.pk, awsFileReference.pk, embeddedLink.pk, board.pk, news.pk
            )
    .fetch();

    if (limit != null) {
      return result.stream()
          .sorted(
              Comparator.comparingLong(ShortsDTO::getRecentViewCount).reversed()
                  .thenComparing(Comparator.comparingLong(ShortsDTO::getRecentKickCount).reversed())
                  .thenComparing(Comparator.comparing(ShortsDTO::getCreatedAt).reversed()) // 최신순
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
  public ShortsDetailDTO getShortsDetail(Shorts file, ShortsSortType sortType) {
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
        .otherwise(
            Expressions.stringTemplate(
                "REPLACE({0}, 'youtube.com/embed/', 'youtube.com/watch?v=')",
                embeddedLink.url
            ));

    Expression<UsedInType> usedInExpression = new CaseBuilder()
        .when(shorts.type.eq(ShortsType.AWS_FILE))
        .then(awsFileReference.usedIn)
        .otherwise(embeddedLink.usedIn);

    // 공통 서브쿼리
    Expression<Long> totalViewCount;
    Expression<Long> totalKickCount;
    Expression<Long> recentViewCount;
    Expression<Long> recentKickCount;
    Expression<Long> totalReplyCount;
    Expression<User> userExpression;
    Expression<String> titleExpression;
    Expression<Long> referencePkExpression;
    Expression<Boolean> isKicked;

    LocalDateTime cutoff = LocalDateTime.now().minusHours(48);

    if (usedInType == UsedInType.BOARD) {
      totalViewCount = JPAExpressions.select(boardViewHistory.pk.count().coalesce(0L))
          .from(boardViewHistory)
          .where(boardViewHistory.board.pk.eq(board.pk));
      totalKickCount = JPAExpressions.select(boardKick.pk.count().coalesce(0L))
          .from(boardKick)
          .where(boardKick.board.pk.eq(board.pk)
              .and(boardKick.status.eq(DataStatus.ACTIVATED)));
      totalReplyCount = JPAExpressions.select(boardReply.pk.count().coalesce(0L))
          .from(boardReply)
          .where(boardReply.board.pk.eq(board.pk)
              .and(boardReply.status.eq(DataStatus.ACTIVATED)));
      recentViewCount = JPAExpressions.select(boardViewHistory.pk.count().coalesce(0L))
              .from(boardViewHistory)
              .where(boardViewHistory.board.pk.eq(board.pk)
                      .and(boardViewHistory.createdAt.after(cutoff)));
      recentKickCount = JPAExpressions.select(boardKick.pk.count().coalesce(0L))
              .from(boardKick)
              .where(boardKick.board.pk.eq(board.pk)
                      .and(boardKick.createdAt.after(cutoff)));
      userExpression = board.user;
      titleExpression = board.title;
      referencePkExpression = board.pk;
      isKicked = JPAExpressions
          .selectOne()
          .from(boardKick)
          .where(boardKick.board.pk.eq(board.pk)
              .and(boardKick.status.eq(DataStatus.ACTIVATED)))
          .exists();
    } else { // NEWS
      totalViewCount = JPAExpressions.select(newsViewHistory.pk.count().coalesce(0L))
          .from(newsViewHistory)
          .where(newsViewHistory.news.pk.eq(news.pk));
      totalKickCount = JPAExpressions.select(newsKick.pk.count().coalesce(0L))
          .from(newsKick)
          .where(newsKick.news.pk.eq(news.pk)
              .and(newsKick.status.eq(DataStatus.ACTIVATED)));
      totalReplyCount = JPAExpressions.select(newsReply.pk.count().coalesce(0L))
          .from(newsReply)
          .where(newsReply.news.pk.eq(news.pk)
              .and(newsReply.status.eq(DataStatus.ACTIVATED)));
      recentViewCount = JPAExpressions.select(newsViewHistory.pk.count().coalesce(0L))
                .from(newsViewHistory)
                .where(newsViewHistory.news.pk.eq(news.pk)
                        .and(newsViewHistory.createdAt.after(cutoff)));
      recentKickCount = JPAExpressions.select(newsKick.pk.count().coalesce(0L))
                .from(newsKick)
                .where(newsKick.news.pk.eq(news.pk)
                        .and(newsKick.createdAt.after(cutoff)));
      userExpression = news.user;
      titleExpression = news.title;
      referencePkExpression = news.pk;
      isKicked = JPAExpressions
          .selectOne()
          .from(newsKick)
          .where(newsKick.news.pk.eq(news.pk)
          .and(newsKick.status.eq(DataStatus.ACTIVATED)))
          .exists();
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
            recentViewCount,
            recentKickCount,
            totalReplyCount,
            shorts.createdAt,
            userExpression,
            isKicked
        ))
        .from(shorts)
        .leftJoin(awsFileReference).on(shorts.type.eq(ShortsType.AWS_FILE)
           .and(shorts.referencePk.eq(awsFileReference.pk)))
        .leftJoin(embeddedLink).on(shorts.type.eq(ShortsType.EMBEDDED_LINK)
           .and(shorts.referencePk.eq(embeddedLink.pk)))
        .where(shorts.status.eq(DataStatus.ACTIVATED)
        .and(shorts.pk.eq(file.getPk())));

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

      ShortsDetailDTO dto = query.fetchOne();
      if (sortType == ShortsSortType.POPULAR) {
          // 2. 전체 리스트 불러오기
          List<ShortsDTO> all = queryShorts(null);

          // 3. Comparator 적용
          all.sort(
                  Comparator.comparingLong(ShortsDTO::getRecentViewCount).reversed()
                          .thenComparing(Comparator.comparingLong(ShortsDTO::getRecentKickCount).reversed())
                          .thenComparing(Comparator.comparing(ShortsDTO::getCreatedAt).reversed()) // 최신순
          );

          // 4. 현재 idx 찾고, 그 다음 pk 꺼내기
          int idx = IntStream.range(0, all.size())
                  .filter(i -> all.get(i).getPk().equals(file.getPk()))
                  .findFirst()
                  .orElse(-1);

          if (idx != -1 && idx + 1 < all.size()) {
              dto.setNextPk(all.get(idx + 1).getPk());
          } else {
              dto.setNextPk(null); // 마지막 영상이면 없음
          }
      } else if (sortType == ShortsSortType.CREATED_DESC) {
          // createdAt DESC로 뒤에 있는 쇼츠 찾기
          Shorts next = queryFactory
                  .selectFrom(QShorts.shorts)
                  .where(QShorts.shorts.status.eq(DataStatus.ACTIVATED)
                          .and(QShorts.shorts.createdAt.before(file.getCreatedAt())))
                  .orderBy(QShorts.shorts.createdAt.desc())
                  .fetchFirst();
          dto.setNextPk(next != null ? next.getPk() : null);

      } else if (sortType == ShortsSortType.CREATED_ASC) {
          // createdAt ASC로 뒤에 있는 쇼츠 찾기
          Shorts next = queryFactory
                  .selectFrom(QShorts.shorts)
                  .where(QShorts.shorts.status.eq(DataStatus.ACTIVATED)
                          .and(QShorts.shorts.createdAt.after(file.getCreatedAt())))
                  .orderBy(QShorts.shorts.createdAt.asc())
                  .fetchFirst();
          dto.setNextPk(next != null ? next.getPk() : null);
      }

      return dto;
  }
  //endregion

  @Transactional
  public void save(ShortsType type, Long referencePk) {
    Shorts shorts = Shorts.builder()
        .type(type)
        .referencePk(referencePk)
        .build();
    shortsRepository.save(shorts);
  }

  @Transactional
  public void deleteByReferencePkAndType(Long referencePk, ShortsType type) {
    Shorts shorts = shortsRepository.findByReferencePkAndType(referencePk, type);
    shorts.setStatus(DataStatus.DEACTIVATED);
    shortsRepository.save(shorts);
  }
}