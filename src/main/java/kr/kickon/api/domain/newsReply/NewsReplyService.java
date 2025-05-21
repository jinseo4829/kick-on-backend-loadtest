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

    @Value("${spring.config.activate.on-profile}")
    private String env;

    @Override
    public NewsReply findById(String uuid) {
        BooleanExpression predicate = QNewsReply.newsReply.id.eq(uuid).and(QNewsReply.newsReply.status.eq(DataStatus.ACTIVATED));
        Optional<NewsReply> newsReply = newsReplyRepository.findOne(predicate);
        return newsReply.orElse(null);
    }

    @Override
    public NewsReply findByPk(Long pk) {
        BooleanExpression predicate = QNewsReply.newsReply.pk.eq(pk).and(QNewsReply.newsReply.status.eq(DataStatus.ACTIVATED));
        Optional<NewsReply> newsReply = newsReplyRepository.findOne(predicate);
        return newsReply.orElse(null);
    }

    @Transactional
    public NewsReply createNewsReplyWithImages(NewsReply newsReply, String[] usedImageKeys) {
        NewsReply saved = newsReplyRepository.save(newsReply);

        if (usedImageKeys != null) {
            List<String> fullKeys = Arrays.stream(usedImageKeys)
                    .map(key -> env + "/news-reply-files/" + key)
                    .collect(Collectors.toList());

            awsFileReferenceService.updateFilesAsUsed(
                    fullKeys,
                    UsedInType.NEWS_REPLY,
                    saved.getPk()
            );
        }

        return saved;
    }

    public PaginatedNewsReplyListDTO getRepliesByNews(Long newsPk, Long userPk, Integer page, Integer size, Boolean infiniteFlag, Long lastReplyPk) {
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
            List<ReplyDTO> boardList = results.stream().map(tuple -> mapToReplyDTO(tuple, userPk)).toList();

            // ✅ 메타데이터 포함한 결과 반환
            return new PaginatedNewsReplyListDTO(boardList, hasNext);
        }else{
            // 일반 페이지 네이션
            dataQuery.offset(offset)
                    .limit(size);
            results = dataQuery.fetch();
            // ✅ DTO 변환
            List<ReplyDTO> boardList = results.stream().map(tuple -> mapToReplyDTO(tuple, userPk)).toList();

            // ✅ 메타데이터 포함한 결과 반환
            return new PaginatedNewsReplyListDTO(page, size, total, boardList);
        }
    }

    private ReplyDTO mapToReplyDTO(Tuple tuple, Long userPk) {
        QNewsReply reply = QNewsReply.newsReply;
        QUser user = QUser.user;

        NewsReply parentReply = tuple.get(reply);
        User replyUser = tuple.get(user);
        NewsReplyKick myNewsReplyKick = null;
        if(userPk != null) {
            myNewsReplyKick = newsReplyKickService.findByNewsReplyAndUser(parentReply.getPk(), userPk);
        }
        Long kickCount = newsReplyKickService.countKicks(parentReply.getPk());
        return ReplyDTO.builder()
                .pk(parentReply.getPk())
                .contents(parentReply.getContents())
                .createdAt(parentReply.getCreatedAt())
                .user(BaseUserDTO.builder()
                        .id(replyUser.getId())
                        .nickname(replyUser.getNickname())
                        .profileImageUrl(replyUser.getProfileImageUrl())
                        .build())
                .replies(parentReply.getParentNewsReply() != null ? null : getChildReplies(parentReply.getPk(), userPk))
                .isKicked(myNewsReplyKick!=null)
                .kickCount(kickCount)
                .build();
    }

    private List<ReplyDTO> getChildReplies(Long parentPk, Long userPk) {
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
                .map(tuple -> mapToReplyDTO(tuple, userPk))
                .collect(Collectors.toList());
    }

    public void save(NewsReply newsReply) {
        newsReplyRepository.save(newsReply);
    }

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
}

