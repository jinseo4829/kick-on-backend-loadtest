package kr.kickon.api.domain.boardReply;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import kr.kickon.api.domain.aws.AwsService;
import kr.kickon.api.domain.awsFileReference.AwsFileReferenceService;
import kr.kickon.api.domain.boardReply.dto.PaginatedReplyListDTO;
import kr.kickon.api.domain.boardReply.dto.ReplyDTO;
import kr.kickon.api.domain.boardReplyKick.BoardReplyKickService;
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
public class BoardReplyService implements BaseService<BoardReply> {
    private final BoardReplyRepository boardReplyRepository;
    private final BoardReplyKickService boardReplyKickService;
    private final JPAQueryFactory queryFactory;
    private final UUIDGenerator uuidGenerator;
    private final AwsFileReferenceService awsFileReferenceService;
    private final AwsService awsService;
    @Value("${spring.config.activate.on-profile}")
    private String env;

    @Override
    public BoardReply findById(String uuid) {
        BooleanExpression predicate = QBoardReply.boardReply.id.eq(uuid).and(QBoardReply.boardReply.status.eq(DataStatus.ACTIVATED));
        Optional<BoardReply> boardReply = boardReplyRepository.findOne(predicate);
        return boardReply.orElse(null);
    }

    @Override
    public BoardReply findByPk(Long pk) {
        BooleanExpression predicate = QBoardReply.boardReply.pk.eq(pk).and(QBoardReply.boardReply.status.eq(DataStatus.ACTIVATED));
        Optional<BoardReply> boardReply = boardReplyRepository.findOne(predicate);
        return boardReply.orElse(null);
    }

    public PaginatedReplyListDTO getRepliesByBoard(Long boardPk, Long userPk, Integer page, Integer size, Boolean infiniteFlag, Long lastReplyPk) {
        QBoardReply reply = QBoardReply.boardReply;
        QUser user = QUser.user;
        Integer offset = (page - 1) * size;
        Long total = queryFactory.select(reply.pk.countDistinct())
                .from(reply)
                .join(user).on(reply.user.pk.eq(user.pk))
                .where(reply.board.pk.eq(boardPk)
                        .and(reply.parentBoardReply.isNull())
                        .and(user.status.eq(DataStatus.ACTIVATED))
                        .and(reply.status.eq(DataStatus.ACTIVATED)))
                .fetchOne();


        JPAQuery<Tuple> dataQuery = queryFactory.select(reply, user)
                .from(reply)
                .join(user).on(reply.user.pk.eq(user.pk))
                .where(reply.board.pk.eq(boardPk)
                        .and(reply.parentBoardReply.isNull())
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
            return new PaginatedReplyListDTO(boardList, hasNext);
        }else{
            // 일반 페이지 네이션
            dataQuery.offset(offset)
                    .limit(size);
            results = dataQuery.fetch();
            // ✅ DTO 변환
            List<ReplyDTO> boardList = results.stream().map(tuple -> mapToReplyDTO(tuple, userPk)).toList();

            // ✅ 메타데이터 포함한 결과 반환
            return new PaginatedReplyListDTO(page, size, total, boardList);
        }
    }

    private ReplyDTO mapToReplyDTO(Tuple tuple, Long userPk) {
        QBoardReply reply = QBoardReply.boardReply;
        QUser user = QUser.user;

        BoardReply parentReply = tuple.get(reply);
        User replyUser = tuple.get(user);
        BoardReplyKick myBoardReplyKick = null;
        if(userPk != null) {
            myBoardReplyKick = boardReplyKickService.findByBoardReplyAndUser(parentReply.getPk(), userPk);
        }
        Long kickCount = boardReplyKickService.countKicks(parentReply.getPk());
        return ReplyDTO.builder()
                .pk(parentReply.getPk())
                .contents(parentReply.getContents())
                .createdAt(parentReply.getCreatedAt())
                .user(BaseUserDTO.builder()
                        .id(replyUser.getId())
                        .nickname(replyUser.getNickname())
                        .profileImageUrl(replyUser.getProfileImageUrl())
                        .build())
                .replies(parentReply.getParentBoardReply() != null ? null : getChildReplies(parentReply.getPk(), userPk))
                .isKicked(myBoardReplyKick!=null)
                .kickCount(kickCount)
                .build();
    }

    private List<ReplyDTO> getChildReplies(Long parentPk, Long userPk) {
        QBoardReply reply = QBoardReply.boardReply;
        QUser user = QUser.user;

        List<Tuple> results = queryFactory.select(reply, user)
                .from(reply)
                .join(user).on(reply.user.pk.eq(user.pk))
                .where(reply.parentBoardReply.pk.eq(parentPk)
                        .and(reply.status.eq(DataStatus.ACTIVATED)))
                .orderBy(reply.createdAt.desc())
                .fetch();

        return results.stream()
                .map(tuple -> mapToReplyDTO(tuple, userPk))
                .collect(Collectors.toList());
    }

    @Transactional
    public BoardReply createBoardReplyWithImages(BoardReply boardReply, String[] usedImageKeys) {
        BoardReply saved = boardReplyRepository.save(boardReply);

        if (usedImageKeys != null) {
            List<String> fullKeys = Arrays.stream(usedImageKeys)
                    .map(key -> env + "/board-reply-files/" + key)
                    .collect(Collectors.toList());
            awsFileReferenceService.updateFilesAsUsed(
                    fullKeys,
                    UsedInType.BOARD_REPLY,
                    saved.getPk()
            );
        }

        return saved;
    }

    public void save(BoardReply boardReply) {
        boardReplyRepository.save(boardReply);
    }

    @Transactional
    public void deleteBoardReply(BoardReply boardReply) {
        boardReply.setStatus(DataStatus.DEACTIVATED);
        boardReplyRepository.save(boardReply);

        //이미지 삭제
        List<AwsFileReference> references = awsFileReferenceService.findbyBoardReplyPk(boardReply.getPk());
        try (S3Client s3 = S3Client.builder().build()) {
            for (AwsFileReference file : references) {
                awsService.deleteFileFromS3AndDb(s3, file);
            }
        }
    }

    @Transactional
    public BoardReply patchBoardReply(BoardReply boardReply) {
        BoardReply saved = boardReplyRepository.save(boardReply);

        return saved;
    }
}
