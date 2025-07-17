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

    // region {findById} BoardReply UUID로 조회
    /**
     * BoardReply의 UUID로 단건 조회
     */
    @Override
    public BoardReply findById(String uuid) {
        BooleanExpression predicate = QBoardReply.boardReply.id.eq(uuid).and(QBoardReply.boardReply.status.eq(DataStatus.ACTIVATED));
        Optional<BoardReply> boardReply = boardReplyRepository.findOne(predicate);
        return boardReply.orElse(null);
    }
    // endregion

    // region {findByPk} BoardReply PK로 조회
    /**
     * BoardReply의 PK로 단건 조회
     */
    @Override
    public BoardReply findByPk(Long pk) {
        BooleanExpression predicate = QBoardReply.boardReply.pk.eq(pk).and(QBoardReply.boardReply.status.eq(DataStatus.ACTIVATED));
        Optional<BoardReply> boardReply = boardReplyRepository.findOne(predicate);
        return boardReply.orElse(null);
    }
    // endregion

    // region {getReplyListByBoard} 게시글 댓글 목록 조회
    /**
     * 특정 게시글(boardPk)에 대한 댓글 목록을 조회합니다.
     *
     * @param boardPk     게시글 PK
     * @param userPk      현재 로그인한 사용자 PK (좋아요 여부 판단용)
     * @param page        페이지 번호 (1부터 시작)
     * @param size        페이지 크기
     * @param infiniteFlag true일 경우 무한스크롤 방식 적용
     * @param lastReplyPk 무한스크롤 기준 댓글 PK (이보다 작은 댓글만 조회)
     * @return PaginatedReplyListDTO 댓글 목록 + 메타정보
     */
    public PaginatedReplyListDTO getReplyListByBoard(Long boardPk, Long userPk, Integer page, Integer size, Boolean infiniteFlag, Long lastReplyPk) {
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
            List<ReplyDTO> boardList = results.stream().map(tuple -> fromTupleAndUser(tuple, userPk)).toList();

            // ✅ 메타데이터 포함한 결과 반환
            return new PaginatedReplyListDTO(boardList, hasNext);
        }else{
            // 일반 페이지 네이션
            dataQuery.offset(offset)
                    .limit(size);
            results = dataQuery.fetch();
            // ✅ DTO 변환
            List<ReplyDTO> boardList = results.stream().map(tuple -> fromTupleAndUser(tuple, userPk)).toList();

            // ✅ 메타데이터 포함한 결과 반환
            return new PaginatedReplyListDTO(page, size, total, boardList);
        }
    }
    // endregion

    // region {fromTupleAndUser} 댓글 Tuple을 ReplyDTO로 변환
    /**
     * 댓글 Tuple 데이터를 ReplyDTO로 변환합니다.
     *
     * @param tuple  댓글 + 사용자 정보를 담은 QueryDSL Tuple
     * @param userPk 현재 로그인한 사용자 PK (좋아요 여부 판단용)
     * @return ReplyDTO 댓글 정보 DTO
     */
    private ReplyDTO fromTupleAndUser(Tuple tuple, Long userPk) {
        QBoardReply reply = QBoardReply.boardReply;
        QUser user = QUser.user;

        BoardReply parentReply = tuple.get(reply);
        User replyUser = tuple.get(user);
        BoardReplyKick myBoardReplyKick = null;
        if(userPk != null) {
            myBoardReplyKick = boardReplyKickService.findByBoardReplyAndUser(parentReply.getPk(), userPk);
        }
        Long kickCount = boardReplyKickService.getBoardReplyKickCount(parentReply.getPk());
        return ReplyDTO.builder()
                .pk(parentReply.getPk())
                .contents(parentReply.getContents())
                .createdAt(parentReply.getCreatedAt())
                .user(BaseUserDTO.builder()
                        .id(replyUser.getId())
                        .nickname(replyUser.getNickname())
                        .profileImageUrl(replyUser.getProfileImageUrl())
                        .build())
                .replies(parentReply.getParentBoardReply() != null ? null : getChildReplyList(parentReply.getPk(), userPk))
                .isKicked(myBoardReplyKick!=null)
                .kickCount(kickCount)
                .build();
    }
    // endregion

    // region {getChildReplyList} 대댓글 목록 조회
    /**
     * 특정 부모 댓글의 자식 댓글(대댓글) 목록을 조회합니다.
     *
     * @param parentPk 부모 댓글의 PK
     * @param userPk   현재 로그인한 사용자 PK (좋아요 여부 판단용)
     * @return 대댓글 목록 리스트 (ReplyDTO)
     */
    private List<ReplyDTO> getChildReplyList(Long parentPk, Long userPk) {
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
                .map(tuple -> fromTupleAndUser(tuple, userPk))
                .collect(Collectors.toList());
    }
    // endregion

    // region {createBoardReplyWithImages} 댓글 등록 + 이미지 처리
    /**
     * 댓글을 저장하고, 사용된 이미지 파일들을 BOARD_REPLY 용도로 등록합니다.
     *
     * @param boardReply     저장할 댓글 엔티티
     * @param usedImageKeys  사용된 이미지 키 배열 (null 허용)
     * @return 저장된 BoardReply 엔티티
     */
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
    // endregion

    // region {save} 댓글 저장
    /**
     * 댓글 엔티티를 저장합니다.
     *
     * @param boardReply 저장할 댓글 엔티티
     */
    public void save(BoardReply boardReply) {
        boardReplyRepository.save(boardReply);
    }
    // endregion

    // region {deleteBoardReply} 댓글 삭제 (soft delete) + 이미지 삭제
    /**
     * 댓글을 비활성화(soft delete) 처리하고, 연결된 이미지 파일을 S3에서 삭제합니다.
     *
     * @param boardReply 삭제할 댓글 엔티티
     */
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
    // endregion

    // region {updateBoardReply} 댓글 수정
    /**
     * 댓글을 수정합니다.
     *
     * @param boardReply 수정된 댓글 엔티티
     * @return 저장된 BoardReply 엔티티
     */
    @Transactional
    public BoardReply updateBoardReply(BoardReply boardReply) {
        BoardReply saved = boardReplyRepository.save(boardReply);

        return saved;
    }
    // endregion
}
