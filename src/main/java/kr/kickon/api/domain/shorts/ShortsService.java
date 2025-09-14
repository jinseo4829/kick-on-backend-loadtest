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

import java.sql.Timestamp;
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
import kr.kickon.api.domain.user.dto.BaseUserDTO;
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

      List<ShortsDTO> result = shortsRepository.findTop4ShortsByPopularity()
              .stream()
              .map(this::mapToDto)
              .toList();

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

      List<ShortsDTO> shorts = shortsRepository.findShorts()
              .stream()
              .map(this::mapToDto)
              .toList();

    ShortsSortType sortType = Optional.ofNullable(request.getSort()).orElse(ShortsSortType.CREATED_DESC);

    // 정렬 조건
    Comparator<ShortsDTO> comparator = switch (sortType) {
      case CREATED_ASC -> Comparator.comparing(ShortsDTO::getCreatedAt);
      case POPULAR -> Comparator.comparingLong(ShortsDTO::getRecentViewCount).reversed()
                        .thenComparing(Comparator.comparingLong(ShortsDTO::getRecentKickCount).reversed())
                        .thenComparing(Comparator.comparing(ShortsDTO::getCreatedAt).reversed()); // 최신순
        default -> Comparator.comparing(ShortsDTO::getCreatedAt).reversed(); // 기본: 최신순
    };

    List<ShortsDTO> sortedShorts = shorts.stream()
        .sorted(comparator)
        .toList();

    int start = (int) pageable.getOffset();
    int end = Math.min(start + pageable.getPageSize(), sortedShorts.size());
    List<ShortsDTO> pageContent = start > end ? Collections.emptyList() : sortedShorts.subList(start, end);

    return new PageImpl<>(pageContent, pageable, sortedShorts.size());
  }
  //endregion

    // region 쇼츠 DTO 변환
    /**
     * Shorts 영상 쿼리를 DTO로 매핑하는 로직입니다.
     * @param tuple 네이티브 쿼리에서 반환하는 Object 배열
     * @return ShortsDTO
     */
    private ShortsDTO mapToDto(Object[] tuple) {
        return new ShortsDTO(
                ((Number) tuple[0]).longValue(),   // shortsPk
                (String) tuple[1],                // videoUrl
                tuple[2] != null ? UsedInType.valueOf(tuple[2].toString()) : null, // usedIn
                tuple[3] != null ? ((Number) tuple[3]).longValue() : null, // referencePk
                (String) tuple[4],                // title
                ((Number) tuple[5]).longValue(),  // totalViewCount
                ((Number) tuple[6]).longValue(),  // totalKickCount
                ((Number) tuple[7]).longValue(),  // recentViewCount
                ((Number) tuple[8]).longValue(),  // recentKickCount
                ((Timestamp) tuple[9]).toLocalDateTime() // createdAt
        );
    }
  //endregion

  // region 쇼츠 상세 조회
  /**
   *
   * @param file 쇼츠 엔티티
   * @param sortType 정렬 기준
   * @param user 로그인한 사용자 엔티티
   * @return ShortsDetailDTO
   */
  @Transactional
  public ShortsDetailDTO getShortsDetail(Shorts file, ShortsSortType sortType, User user) {
      UsedInType usedInType = getUsedIn(file);

      Object[] row;
      if (usedInType == UsedInType.BOARD) {
          Object[] outer = shortsRepository.findBoardShortsDetail(file.getPk(), user.getPk());
          row = (Object[]) outer[0]; // 내부 Object[] 꺼내기
      } else {
          Object[] outer = shortsRepository.findNewsShortsDetail(file.getPk(), user.getPk());
          row = (Object[]) outer[0];
      }

      ShortsDetailDTO dto = ShortsDetailDTO.builder()
              .pk(((Number) row[0]).longValue())
              .videoUrl((String) row[1])
              .usedIn(row[2] != null ? UsedInType.valueOf(row[2].toString()) : null)
              .referencePk(row[3] != null ? ((Number) row[3]).longValue() : null)
              .title((String) row[4])
              .viewCount(((Number) row[5]).longValue())
              .kickCount(((Number) row[6]).longValue())
              .recentViewCount(((Number) row[7]).longValue())
              .recentKickCount(((Number) row[8]).longValue())
              .replyCount(((Number) row[9]).longValue())
              .createdAt(((Timestamp) row[10]).toLocalDateTime())
              .user(new BaseUserDTO(
                      row[11] != null ? row[11].toString() : null,   // userPk
                      (String) row[12],                              // userNickname
                      (String) row[13],                              // userProfileImageUrl
                      row[14] != null && ((Number) row[14]).longValue() > 0 // isReporter: 0/1 → boolean
              ))
              .isKicked(row[15] != null && ((Number) row[15]).longValue() > 0) // isKicked: 0/1 → boolean
              .build();

      if (sortType == ShortsSortType.POPULAR) {
          // 2. 전체 리스트 불러오기
          List<ShortsDTO> all = new java.util.ArrayList<>(shortsRepository.findShorts()
                  .stream()
                  .map(this::mapToDto)
                  .toList());

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