package kr.kickon.api.domain.shorts;

import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import kr.kickon.api.domain.awsFileReference.AwsFileReferenceService;
import kr.kickon.api.domain.board.BoardService;
import kr.kickon.api.domain.boardKick.BoardKickService;
import kr.kickon.api.domain.boardViewHistory.BoardViewHistoryService;
import kr.kickon.api.domain.news.NewsService;
import kr.kickon.api.domain.newsKick.NewsKickService;
import kr.kickon.api.domain.newsViewHistory.NewsViewHistoryService;
import kr.kickon.api.domain.shorts.dto.ShortsDTO;
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
import kr.kickon.api.global.common.enums.UsedInType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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

  // region 고정 쇼츠 리스트 조회
  /**
   * 고정된 Shorts 영상 리스트를 조회합니다.
   * - AwsFileReference 엔티티 중 동영상 파일(MP4, MOV, AVI, MKV)만 필터링
   * - 게시글/뉴스 타입에 따라 각각의 48시간 기준 조회수를 가져옵니다.
   * - 조회수 기준으로 내림차순 정렬 후 상위 4개만 반환합니다.
   * - 조회수가 같은 경우 킥 수 기준
   */
  public List<ShortsDTO> getFixedShorts() {
    List<AwsFileReference> allVideos = awsFileReferenceService.findAll()
        .stream()
        .filter(file -> file.getS3Key().matches(".*\\.(mp4|mov|avi|mkv)$"))
        .filter(file -> file.getReferencePk() != null)
        .toList();

    return allVideos.stream()
        .map(file -> {
          Long recentViewCount = 0L;
          Long recentKickCount = 0L;
          Long totalViewCount = 0L;
          Long totalKickCount = 0L;
          String title = null;

          if (file.getUsedIn() == UsedInType.BOARD) {
            recentViewCount = boardViewHistoryService.countByBoardPkWithin48Hours(file.getReferencePk());
            recentKickCount = boardKickService.countByBoardPkWithin48Hours(file.getReferencePk());

            totalViewCount = boardViewHistoryService.countByBoardPk(file.getReferencePk());
            totalKickCount = boardKickService.countByBoardPk(file.getReferencePk());

            Board board = boardService.findByPk(file.getReferencePk());
            if (board != null) {
              title = board.getTitle();
            }
          } else if (file.getUsedIn() == UsedInType.NEWS) {
            recentViewCount = newsViewHistoryService.countByNewsPkWithin48Hours(file.getReferencePk());
            recentKickCount = newsKickService.countByNewsPkWithin48Hours(file.getReferencePk());

            totalViewCount = newsViewHistoryService.countByNewsPk(file.getReferencePk());
            totalKickCount = newsKickService.countByNewsPk(file.getReferencePk());

            News news = newsService.findByPk(file.getReferencePk());
            if (news != null) {
              title = news.getTitle();
            }
          }

          ShortsDTO dto = ShortsDTO.fromEntity(file, totalViewCount, totalKickCount, title);
          dto.setSortViewCount(recentViewCount);  // 정렬용 값
          dto.setSortKickCount(recentKickCount);
          return dto;
        })
        .sorted(
            Comparator.comparingLong(ShortsDTO::getSortViewCount).reversed()
                .thenComparing(Comparator.comparingLong(ShortsDTO::getSortKickCount).reversed())
        )
        .limit(4)
        .collect(Collectors.toList());
  }
  // endregion

  // region 쇼츠 리스트 조회
  /**
   * Shorts 영상 리스트를 조회합니다.
   * - AwsFileReference 엔티티 중 동영상 파일(MP4, MOV, AVI, MKV)만 필터링
   * - 게시글/뉴스 타입에 따라 각각의 48시간 기준 조회수를 가져옵니다.
   * - 게시글/뉴스를 unionAll로 통합합니다.
   * - 조회수가 같은 경우 킥 수 기준
   */
  public Page<ShortsDTO> getShorts(GetShortsRequest request, Pageable pageable) {
    QAwsFileReference afr = QAwsFileReference.awsFileReference;
    QBoard b = QBoard.board;
    QNews n = QNews.news;
    QBoardViewHistory bv = QBoardViewHistory.boardViewHistory;
    QBoardKick bk = QBoardKick.boardKick;
    QNewsViewHistory nv = QNewsViewHistory.newsViewHistory;
    QNewsKick nk = QNewsKick.newsKick;

    LocalDateTime cutoff = LocalDateTime.now().minusHours(48);

    // Board Shorts
    List<ShortsDTO> boardShorts = queryFactory
        .select(Projections.constructor(ShortsDTO.class,
            afr.pk,
            afr.s3Key,
            afr.usedIn,
            afr.referencePk,
            b.title,
            ExpressionUtils.as(JPAExpressions
                .select(bv.pk.count())
                .from(bv)
                .where(bv.board.pk.eq(b.pk)), "totalViewCount"),
            ExpressionUtils.as(JPAExpressions
                .select(bk.pk.count())
                .from(bk)
                .where(bk.board.pk.eq(b.pk)), "totalKickCount"),
            ExpressionUtils.as(JPAExpressions
                .select(bv.pk.count())
                .from(bv)
                .where(bv.board.pk.eq(b.pk).and(bv.createdAt.after(cutoff))), "recentViewCount"),
            ExpressionUtils.as(JPAExpressions
                .select(bk.pk.count())
                .from(bk)
                .where(bk.board.pk.eq(b.pk).and(bk.createdAt.after(cutoff))), "recentKickCount"),
            afr.createdAt
            ))
        .from(afr)
        .leftJoin(b).on(afr.referencePk.eq(b.pk))
        .where(
            afr.usedIn.eq(UsedInType.BOARD),
            afr.s3Key.endsWithIgnoreCase(".mp4")
                .or(afr.s3Key.endsWithIgnoreCase(".mov"))
                .or(afr.s3Key.endsWithIgnoreCase(".avi"))
                .or(afr.s3Key.endsWithIgnoreCase(".mkv"))
        )
        .fetch();

    // News Shorts
    List<ShortsDTO> newsShorts = queryFactory
        .select(Projections.constructor(ShortsDTO.class,
            afr.pk,
            afr.s3Key,
            afr.usedIn,
            afr.referencePk,
            n.title,
            ExpressionUtils.as(JPAExpressions
                .select(nv.pk.count())
                .from(nv)
                .where(nv.news.pk.eq(n.pk)), "totalViewCount"),
            ExpressionUtils.as(JPAExpressions
                .select(nk.pk.count())
                .from(nk)
                .where(nk.news.pk.eq(n.pk)), "totalKickCount"),
            ExpressionUtils.as(JPAExpressions
                .select(nv.pk.count())
                .from(nv)
                .where(nv.news.pk.eq(n.pk).and(nv.createdAt.after(cutoff))), "recentViewCount"),
            ExpressionUtils.as(JPAExpressions
                .select(nk.pk.count())
                .from(nk)
                .where(nk.news.pk.eq(n.pk).and(nk.createdAt.after(cutoff))), "recentKickCount"),
            afr.createdAt
            ))
        .from(afr)
        .leftJoin(n).on(afr.referencePk.eq(n.pk))
        .where(
            afr.usedIn.eq(UsedInType.NEWS),
            afr.s3Key.endsWithIgnoreCase(".mp4")
                .or(afr.s3Key.endsWithIgnoreCase(".mov"))
                .or(afr.s3Key.endsWithIgnoreCase(".avi"))
                .or(afr.s3Key.endsWithIgnoreCase(".mkv"))
        )
        .fetch();

    // Combine and sort
    List<ShortsDTO> combined = new ArrayList<>();
    combined.addAll(boardShorts);
    combined.addAll(newsShorts);

    combined = trimS3KeyPrefix(combined);

    Long totalCount = queryFactory
        .select(afr.count())
        .from(afr)
        .where(
            afr.s3Key.endsWithIgnoreCase(".mp4")
                .or(afr.s3Key.endsWithIgnoreCase(".mov"))
                .or(afr.s3Key.endsWithIgnoreCase(".avi"))
                .or(afr.s3Key.endsWithIgnoreCase(".mkv"))
        )
        .fetchOne();

    long total = totalCount != null ? totalCount : 0L;

    // 정렬 조건
    Comparator<ShortsDTO> comparator = switch (request.getSort()) {
      case CREATED_ASC -> Comparator.comparing(ShortsDTO::getCreatedAt);
      case POPULAR -> Comparator.comparingLong(ShortsDTO::getSortViewCount).reversed()
          .thenComparing(Comparator.comparingLong(ShortsDTO::getSortKickCount).reversed());
      default -> Comparator.comparing(ShortsDTO::getCreatedAt).reversed(); // 기본: 최신순

    };
    combined.sort(comparator);
    return new PageImpl<>(combined, pageable, total);
  }
  //endregion

  private List<ShortsDTO> trimS3KeyPrefix(List<ShortsDTO> list) {
    return list.stream()
        .peek(dto -> {
          String s3Key = dto.getS3Key();
          if (s3Key != null) {
            String trimmed = s3Key
                .replaceFirst("^dev/board-files/", "")
                .replaceFirst("^dev/news-files/", "")
                .replaceFirst("^local/board-files/", "")
                .replaceFirst("^local/news-files/", "");
            dto.setS3Key(trimmed);
          }
        })
        .collect(Collectors.toList());
  }

}