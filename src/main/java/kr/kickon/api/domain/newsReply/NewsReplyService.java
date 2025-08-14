package kr.kickon.api.domain.newsReply;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.transaction.Transactional;
import kr.kickon.api.domain.aws.AwsService;
import kr.kickon.api.domain.awsFileReference.AwsFileReferenceService;
import kr.kickon.api.domain.newsReply.dto.PaginatedNewsReplyListDTO;
import kr.kickon.api.domain.newsReply.dto.ReplyDTO;
import kr.kickon.api.domain.newsReplyKick.NewsReplyKickService;
import kr.kickon.api.domain.notification.NotificationService;
import kr.kickon.api.domain.teamReporter.TeamReporterService;
import kr.kickon.api.domain.user.dto.BaseUserDTO;
import kr.kickon.api.global.common.BaseService;
import kr.kickon.api.global.common.entities.*;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.common.enums.UsedInType;
import kr.kickon.api.global.util.UUIDGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import software.amazon.awssdk.services.s3.S3Client;

@Service
@Slf4j
@RequiredArgsConstructor
public class NewsReplyService implements BaseService<NewsReply> {
    private final NewsReplyRepository newsReplyRepository;
    private final JPAQueryFactory queryFactory;
    private final NewsReplyKickService newsReplyKickService;
    private final UUIDGenerator uuidGenerator;
    private final AwsFileReferenceService awsFileReferenceService;
    private final AwsService awsService;
    private final NotificationService notificationService;
    private final TeamReporterService teamReporterService;

    @Value("${spring.config.activate.on-profile}")
    private String env;

    // region {findById} UUID 기준으로 활성화된 뉴스 댓글 조회
    /**
     * UUID 기준으로 활성화된 뉴스 댓글 조회
     *
     * @param uuid UUID
     * @return NewsReply 또는 null
     */
    @Override
    public NewsReply findById(String uuid) {
        BooleanExpression predicate = QNewsReply.newsReply.id.eq(uuid).and(QNewsReply.newsReply.status.eq(DataStatus.ACTIVATED));
        Optional<NewsReply> newsReplyEntity = newsReplyRepository.findOne(predicate);
        return newsReplyEntity.orElse(null);
    }
    // endregion

    // region {findByPk} PK 기준으로 활성화된 뉴스 댓글 조회
    /**
     * PK 기준으로 활성화된 뉴스 댓글 조회
     *
     * @param pk 댓글 PK
     * @return NewsReply 또는 null
     */
    @Override
    public NewsReply findByPk(Long pk) {
        BooleanExpression predicate = QNewsReply.newsReply.pk.eq(pk).and(QNewsReply.newsReply.status.eq(DataStatus.ACTIVATED));
        Optional<NewsReply> newsReplyEntity = newsReplyRepository.findOne(predicate);
        return newsReplyEntity.orElse(null);
    }
    // endregion

    // region {createNewsReplyWithImages} 댓글 저장 및 이미지 키 사용 처리
    /**
     * 댓글 저장 및 이미지 키 사용 처리
     *
     * @param newsReply 댓글 엔티티
     * @param usedImageKeys 사용된 이미지 키 배열
     * @return 저장된 NewsReply
     */
    @Transactional
    public NewsReply createNewsReplyWithImages(NewsReply newsReply, String[] usedImageKeys) {
        NewsReply savedNewsReplyEntity = newsReplyRepository.save(newsReply);

        if (usedImageKeys != null) {
            List<String> fullKeys = Arrays.stream(usedImageKeys)
                    .map(key -> env + "/news-reply-files/" + key)
                    .collect(Collectors.toList());

            awsFileReferenceService.updateFilesAsUsed(
                    fullKeys,
                    UsedInType.NEWS_REPLY,
                    savedNewsReplyEntity.getPk()
            );
        }

        return savedNewsReplyEntity;
    }
    // endregion

    // region {getReplyListByNews} 뉴스 댓글 목록 조회 (페이징 또는 무한 스크롤)
    /**
     * 뉴스 댓글 목록 조회 (페이징 또는 무한 스크롤)
     *
     * @param newsPk 뉴스 PK
     * @param userPk 현재 사용자 PK (좋아요 여부 판단 등)
     * @param page 페이지 번호 (1부터 시작)
     * @param size 페이지당 개수
     * @param infiniteFlag 무한 스크롤 여부
     * @param lastReplyPk 마지막 댓글 PK (무한 스크롤 커서)
     * @return PaginatedNewsReplyListDTO
     */

    public PaginatedNewsReplyListDTO getReplyListByNews(Long newsPk, Long userPk, Integer page, Integer size, Boolean infiniteFlag, Long lastReplyPk) {
        QNewsReply reply = QNewsReply.newsReply;
        QUser user = QUser.user;
        Integer offset = (page - 1) * size;
        Long total = queryFactory.select(reply.pk.countDistinct())
                .from(reply)
                .join(user).on(reply.user.pk.eq(user.pk))
                .where(reply.news.pk.eq(newsPk)
                        .and(reply.parentNewsReply.isNull())
                        .and(user.status.eq(DataStatus.ACTIVATED))
                        .and(reply.status.eq(DataStatus.ACTIVATED))
                        )
                .fetchOne();

        JPAQuery<Tuple> dataQuery = queryFactory.select(reply, user)
                .from(reply)
                .join(user).on(reply.user.pk.eq(user.pk))
                .where(reply.news.pk.eq(newsPk)
                        .and(reply.parentNewsReply.isNull())
                        .and(user.status.eq(DataStatus.ACTIVATED))
                        .and(reply.status.eq(DataStatus.ACTIVATED)))
                .orderBy(reply.createdAt.desc());

        List<Tuple> results;

        if(infiniteFlag!=null && infiniteFlag){
            // 무한 스크롤일 때
            dataQuery.limit(size + 1); // → hasNext 판단용
            if (lastReplyPk != null && lastReplyPk > 0) {
                dataQuery.where(reply.pk.lt(lastReplyPk));
            }

            results = dataQuery.fetch();
            // ✅ hasNext 처리
            boolean hasNext = results.size() > size;
            if (hasNext) {
                results = results.subList(0, size); // 초과분 잘라내기
            }
            // ✅ DTO 변환
            List<ReplyDTO> boardList = results.stream().map(tuple -> fromTupleAndUserPkToReplyDTO(tuple, userPk)).toList();

            // ✅ 메타데이터 포함한 결과 반환
            return new PaginatedNewsReplyListDTO(boardList, hasNext);
        }else{
            // 일반 페이지 네이션
            dataQuery.offset(offset)
                    .limit(size);
            results = dataQuery.fetch();
            // ✅ DTO 변환
            List<ReplyDTO> boardList = results.stream().map(tuple -> fromTupleAndUserPkToReplyDTO(tuple, userPk)).toList();

            // ✅ 메타데이터 포함한 결과 반환
            return new PaginatedNewsReplyListDTO(page, size, total, boardList);
        }
    }
    // endregion

    // region {fromTupleAndUserPkToReplyDTO} Tuple과 사용자 PK를 기반으로 ReplyDTO 변환
    /**
     * Tuple과 사용자 PK를 기반으로 ReplyDTO 변환
     *
     * @param tuple QueryDSL Tuple
     * @param userPk 현재 사용자 PK
     * @return ReplyDTO
     */
    private ReplyDTO fromTupleAndUserPkToReplyDTO(Tuple tuple, Long userPk) {
        QNewsReply reply = QNewsReply.newsReply;
        QUser user = QUser.user;

        NewsReply parentNewsReplyEntity = tuple.get(reply);
        User replyUserEntity = tuple.get(user);
        NewsReplyKick myNewsReplyKickEntity = null;
        if(userPk != null) {
            myNewsReplyKickEntity = newsReplyKickService.findByNewsReplyAndUser(parentNewsReplyEntity.getPk(), userPk);
        }
        Long kickCount = newsReplyKickService.getNewsReplyKickCount(parentNewsReplyEntity.getPk());
        TeamReporter teamReporter = teamReporterService.findByUserId(parentNewsReplyEntity.getUser().getId());
        return ReplyDTO.builder()
                .pk(parentNewsReplyEntity.getPk())
                .contents(parentNewsReplyEntity.getContents())
                .createdAt(parentNewsReplyEntity.getCreatedAt())
                .user(BaseUserDTO.builder()
                        .id(replyUserEntity.getId())
                        .nickname(replyUserEntity.getNickname())
                        .profileImageUrl(replyUserEntity.getProfileImageUrl())
                        .isReporter(teamReporter != null)
                        .build())
                .replies(parentNewsReplyEntity.getParentNewsReply() != null ? null : getChildReplyList(parentNewsReplyEntity.getPk(), userPk))
                .isKicked(myNewsReplyKickEntity!=null)
                .kickCount(kickCount)
                .build();
    }
    // endregion

    // region {getChildReplyList} 부모 댓글에 대한 자식 댓글 목록 조회
    /**
     * 부모 댓글에 대한 자식 댓글 목록 조회
     *
     * @param parentPk 부모 댓글 PK
     * @param userPk 현재 사용자 PK
     * @return List of ReplyDTO
     */
    private List<ReplyDTO> getChildReplyList(Long parentPk, Long userPk) {
        QNewsReply reply = QNewsReply.newsReply;
        QUser user = QUser.user;

        List<Tuple> results = queryFactory.select(reply, user)
                .from(reply)
                .join(user).on(reply.user.pk.eq(user.pk))
                .where(reply.parentNewsReply.pk.eq(parentPk)
                        .and(reply.status.eq(DataStatus.ACTIVATED)))
                .orderBy(reply.createdAt.desc())
                .fetch();

        return results.stream()
                .map(tuple -> fromTupleAndUserPkToReplyDTO(tuple, userPk))
                .collect(Collectors.toList());
    }
    // endregion

    // region {saveNewsReply} 댓글 저장
    /**
     * 댓글 저장
     *
     * @param newsReply 댓글 엔티티
     */
    public void save(NewsReply newsReply) {
        newsReplyRepository.save(newsReply);
    }
    // endregion

    // region {deleteNewsReply} 댓글 삭제 (상태만 비활성화 처리)
    /**
     * 댓글 삭제 (상태만 비활성화 처리)
     *
     * @param newsReply 삭제할 댓글
     */
    @Transactional
    public void deleteNewsReply(NewsReply newsReply) {
        newsReply.setStatus(DataStatus.DEACTIVATED);
        newsReplyRepository.save(newsReply);

        //이미지 삭제
        List<AwsFileReference> references = awsFileReferenceService.findbyNewsReplyPk(newsReply.getPk());
        try (S3Client s3 = S3Client.builder().build()) {
            for (AwsFileReference file : references) {
                awsService.deleteFileFromS3AndDb(s3, file);
            }
        }
    }
    // endregion

    // region {updateNewsReply} 댓글 내용 수정
    /**
     * 댓글 내용 수정
     *
     * @param newsReply 수정된 댓글
     * @return 저장된 댓글 엔티티
     */
    @Transactional
    public NewsReply updateNewsReply(NewsReply newsReply) {
        NewsReply savedNewsReplyEntity = newsReplyRepository.save(newsReply);

        return savedNewsReplyEntity;
    }
    // endregion

    // region 뉴스 댓글 수 반환
    /**
     * news PK로 댓글 수 계산
     *
     * @param news 뉴스 엔티티
     * @return Long 댓글 수
     */
    public Long countRepliesByNewsPk(News news) {
        return newsReplyRepository.countByNews_PkAndStatus(news.getPk(), DataStatus.ACTIVATED);
    }
    // endregion

    public void sendReplyNotification(News news, NewsReply parent, User writer) {
        String redirectUrl = "/news/" + news.getPk();

        // 뉴스 글에 대한 댓글 (부모가 없음)
        if (parent == null && !news.getUser().equals(writer)) {
            notificationService.sendNotification(
                    news.getUser(),
                    "NEWS_REPLY",
                    writer.getNickname() + "님이 회원님의 뉴스에 댓글을 달았어요.",
                    redirectUrl
            );
        }

        // 대댓글
        if (parent != null && !parent.getUser().equals(writer)) {
            notificationService.sendNotification(
                    parent.getUser(),
                    "NEWS_REPLY_REPLY",
                    writer.getNickname() + "님이 답글을 달았어요.",
                    redirectUrl + "?replyPk=" + parent.getPk()
            );
        }
    }

}

