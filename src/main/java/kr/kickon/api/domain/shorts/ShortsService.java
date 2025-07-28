package kr.kickon.api.domain.shorts;

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
import kr.kickon.api.global.common.entities.AwsFileReference;
import kr.kickon.api.global.common.entities.Board;
import kr.kickon.api.global.common.entities.News;
import kr.kickon.api.global.common.enums.UsedInType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

            log.info("BOARD - referencePk: {}, recentView: {}, recentKick: {}",
                file.getReferencePk(), recentViewCount, recentKickCount);
            Board board = boardService.findByPk(file.getReferencePk());
            if (board != null) {
              title = board.getTitle();
            }
          } else if (file.getUsedIn() == UsedInType.NEWS) {
            recentViewCount = newsViewHistoryService.countByNewsPkWithin48Hours(file.getReferencePk());
            recentKickCount = newsKickService.countByNewsPkWithin48Hours(file.getReferencePk());

            totalViewCount = newsViewHistoryService.countByNewsPk(file.getReferencePk());
            totalKickCount = newsKickService.countByNewsPk(file.getReferencePk());
            log.info("NEWS - referencePk: {}, recentView: {}, recentKick: {}",
                file.getReferencePk(), recentViewCount, recentKickCount);
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
}