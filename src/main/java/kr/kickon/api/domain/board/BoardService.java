package kr.kickon.api.domain.board;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import kr.kickon.api.domain.aws.AwsService;
import kr.kickon.api.domain.awsFileReference.AwsFileReferenceService;
import kr.kickon.api.domain.board.dto.BoardDetailDTO;
import kr.kickon.api.domain.board.dto.BoardListDTO;
import kr.kickon.api.domain.board.dto.PaginatedBoardListDTO;
import kr.kickon.api.domain.board.dto.BoardListDTO;
import kr.kickon.api.domain.board.dto.PaginatedBoardListDTO;
import kr.kickon.api.domain.user.dto.BaseUserDTO;
import kr.kickon.api.domain.boardKick.BoardKickService;
import kr.kickon.api.domain.team.dto.TeamDTO;
import kr.kickon.api.global.common.BaseService;
import kr.kickon.api.global.common.entities.*;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.common.enums.UsedInType;
import kr.kickon.api.global.util.UUIDGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import software.amazon.awssdk.services.s3.S3Client;

@Service
@Slf4j
@RequiredArgsConstructor
public class BoardService implements BaseService<Board> {
    private final BoardRepository boardRepository;
    private final JPAQueryFactory queryFactory;
    private final BoardKickService boardKickService;
    private final UUIDGenerator uuidGenerator;
    private final AwsFileReferenceService awsFileReferenceService;
    private final AwsService awsService;
    @Value("${spring.config.activate.on-profile}")
    private String env;

    @Override
    public Board findById(String uuid) {
        BooleanExpression predicate = QBoard.board.id.eq(uuid).and(QBoard.board.status.eq(DataStatus.ACTIVATED));
        Optional<Board> board = boardRepository.findOne(predicate);
        return board.orElse(null);
    }

    @Override
    public Board findByPk(Long pk) {
        BooleanExpression predicate = QBoard.board.pk.eq(pk).and(QBoard.board.status.eq(DataStatus.ACTIVATED));
        Optional<Board> board = boardRepository.findOne(predicate);
        return board.orElse(null);
    }

    @Transactional
    public Board createBoardWithImages(Board board, String[] usedImageKeys) {
        Board saved = boardRepository.save(board);

        if (usedImageKeys != null) {
            List<String> fullKeys = Arrays.stream(usedImageKeys)
                    .map(key -> env + "/board-files/" + key)
                    .collect(Collectors.toList());

            awsFileReferenceService.updateFilesAsUsed(
                    fullKeys,
                    UsedInType.BOARD,
                    saved.getPk()
            );
        }

        return saved;
    }

    public JPAQuery<Tuple> createBoardListDTOQuery() {
        QBoard board = QBoard.board;
        QBoardKick boardKick = QBoardKick.boardKick;
        QBoardViewHistory boardViewHistory = QBoardViewHistory.boardViewHistory;
        QBoardReply boardReply = QBoardReply.boardReply;
        QUser user = QUser.user;
        QTeam team = QTeam.team;
        return queryFactory.select(board, user, team,
                        boardKick.pk.countDistinct().coalesce(0L).as("kickCount"),
                        boardViewHistory.pk.countDistinct().coalesce(0L).as("viewCount"),
                        boardReply.pk.countDistinct().coalesce(0L).as("replyCount"))
                .from(board)
                .join(user).on(board.user.pk.eq(user.pk))
                .leftJoin(team).on(board.team.pk.eq(team.pk))
                .leftJoin(boardKick).on(board.pk.eq(boardKick.board.pk).and(boardKick.status.eq(DataStatus.ACTIVATED)))
                .leftJoin(boardViewHistory).on(board.pk.eq(boardViewHistory.board.pk).and(boardViewHistory.status.eq(DataStatus.ACTIVATED)))
                .leftJoin(boardReply).on(board.pk.eq(boardReply.board.pk).and(boardReply.status.eq(DataStatus.ACTIVATED)))
                .where(board.status.eq(DataStatus.ACTIVATED)
                        .and(user.status.eq(DataStatus.ACTIVATED)));
    }
    public BoardListDTO tupleToBoardListDTO(Tuple tuple) {
        QBoard board = QBoard.board;
        QUser user = QUser.user;
        Board boardEntity = tuple.get(board);
        User userEntity = tuple.get(user);
        QTeam team = QTeam.team;
        Team teamEntity = tuple.get(team);
        BoardListDTO boardListDTO = BoardListDTO.builder()
                .pk(boardEntity.getPk())
                .title(boardEntity.getTitle())
                .user(BaseUserDTO.builder()
                        .id(userEntity.getId())
                        .nickname(userEntity.getNickname())
                        .profileImageUrl(userEntity.getProfileImageUrl())
                        .build())
                .hasImage(boardEntity.getHasImage())
                .createdAt(tuple.get(board.createdAt))
                .createdAt(boardEntity.getCreatedAt())
                .likes(tuple.get(3, Long.class).intValue())
                .views(tuple.get(4, Long.class).intValue())
                .replies(tuple.get(5, Long.class).intValue())
                .build();


        if(teamEntity!=null){
            boardListDTO.setTeam(TeamDTO.builder()
                    .pk(teamEntity.getPk())
                    .logoUrl(teamEntity.getLogoUrl())
                    .nameKr(teamEntity.getNameKr())
                    .nameEn(teamEntity.getNameEn())
                    .build());
        }
        return boardListDTO;
    }

    public List<BoardListDTO> findTop10Boards() {
        QBoard board = QBoard.board;
        QUser user = QUser.user;
        List<Tuple> results = createBoardListDTOQuery()
                .groupBy(board.pk, user.pk)
                .orderBy(board.createdAt.desc())
                .limit(10).fetch();
        return results.stream().map(this::tupleToBoardListDTO).toList();
    }

    public BoardDetailDTO findOneBoardListDTOByPk(Long boardPk,User userData) {
        QBoard board = QBoard.board;
        QUser user = QUser.user;
        QTeam team = QTeam.team;
        Tuple result = createBoardListDTOQuery()
                .where(board.pk.eq(boardPk))
                .groupBy(board.pk)
                .fetchOne();
        if(result == null) return null;
        Board boardEntity = result.get(board);
        User userEntity = result.get(user);
        BoardDetailDTO boardDetailDTO = BoardDetailDTO.builder()
                .pk(boardEntity.getPk())
                .title(boardEntity.getTitle())
                .user(BaseUserDTO.builder()
                        .id(userEntity.getId())
                        .nickname(userEntity.getNickname())
                        .profileImageUrl(userEntity.getProfileImageUrl())
                        .build())
                .hasImage(boardEntity.getHasImage())
                .createdAt(result.get(board.createdAt))
                .createdAt(boardEntity.getCreatedAt())
                .likes(result.get(3, Long.class).intValue())
                .views(result.get(4, Long.class).intValue())
                .replies(result.get(5, Long.class).intValue())
                .content(boardEntity.getContents())
                .build();
        if(userData!=null){
            BoardKick boardKick = boardKickService.findByBoardAndUser(boardDetailDTO.getPk(), userData.getPk());
            boardDetailDTO
                .setIsKicked(boardKick!=null);
        }
        if(result.get(team)!=null){
            Team teamEntity = result.get(team);
                boardDetailDTO.setTeam(TeamDTO.builder()
                    .pk(teamEntity.getPk())
                    .logoUrl(teamEntity.getLogoUrl())
                    .nameKr(teamEntity.getNameKr())
                    .nameEn(teamEntity.getNameEn())
                    .build());
        }

        String prefix = env + "/board-files/";

        List<AwsFileReference> usedImageReferences = awsFileReferenceService.findbyBoardPk(
            boardEntity.getPk()
        );
        String[] usedImageKeys = usedImageReferences.stream()
            .map(AwsFileReference::getS3Key) // 각 객체에서 S3 키만 추출
            .map(key -> key.substring(prefix.length())) // prefix 제거
            .toArray(String[]::new);

        boardDetailDTO.setUsedImageKeys(usedImageKeys);
        return boardDetailDTO;
    }

    public PaginatedBoardListDTO findBoardsWithPagination(Long teamPk, Integer page, Integer size, String sortBy,Boolean infiniteFlag, Long lastBoardPk, Long lastViewCount) {
        QBoard board = QBoard.board;
        QBoardViewHistory boardViewHistory = QBoardViewHistory.boardViewHistory;
        QUser user = QUser.user;

        Integer offset = (page - 1) * size;
        LocalDateTime hotThreshold = LocalDateTime.now().minusHours(200); // 최근 48시간 기준

        JPAQuery<Long> totalQuery = queryFactory.select(board.pk.count())
                .from(board)
                .join(user).on(board.user.pk.eq(user.pk))
                .where(board.status.eq(DataStatus.ACTIVATED)
                        .and(user.status.eq(DataStatus.ACTIVATED)));
        if (teamPk != null) totalQuery.where(board.team.pk.eq(teamPk));
        if ("hot".equalsIgnoreCase(sortBy)) totalQuery.where(board.createdAt.goe(hotThreshold));

        Long totalCount = totalQuery.fetchOne();

        JPAQuery<Tuple> dataQuery = createBoardListDTOQuery()
                .groupBy(board.pk, user.pk)
                .offset(offset)
                .limit(size);
        if (teamPk != null) dataQuery.where(board.team.pk.eq(teamPk));

        if ("hot".equalsIgnoreCase(sortBy)) {
            dataQuery.where(board.createdAt.goe(hotThreshold)); // 최근 48시간 필터링

            // ✅ 복합 정렬 기준 (조회수, pk)
            dataQuery.orderBy(
                    boardViewHistory.pk.countDistinct().coalesce(0L).desc(),
                    board.pk.desc()
            );

            // ✅ 커서 조건
            if (lastViewCount != null && lastBoardPk != null && lastBoardPk > 0) {
                // group by 이후 having으로 거르기보다 subquery를 활용한 커서 기반 조회
                NumberTemplate<Long> viewCountAlias = Expressions.numberTemplate(Long.class, "count(distinct {0})", boardViewHistory.pk);

                dataQuery.having(
                        viewCountAlias.lt(lastViewCount)
                                .or(viewCountAlias.eq(lastViewCount).and(board.pk.lt(lastBoardPk)))
                );
            }
        } else {
            dataQuery.orderBy(board.createdAt.desc(), board.pk.desc()); // 최신순도 pk 정렬 추가
            if (lastBoardPk != null && lastBoardPk > 0) {
                dataQuery.where(board.pk.lt(lastBoardPk));
            }
        }

        List<Tuple> results;
        if(infiniteFlag!=null && infiniteFlag){
            // 무한 스크롤일 때
            dataQuery.limit(size + 1); // → hasNext 판단용
            results = dataQuery.fetch();
            // ✅ hasNext 처리
//            System.out.println(results);
            boolean hasNext = results.size() > size;
            if (hasNext) {
                results = results.subList(0, size); // 초과분 잘라내기
            }
            // ✅ DTO 변환
            List<BoardListDTO> boardList = results.stream().map(this::tupleToBoardListDTO).toList();

            // ✅ 메타데이터 포함한 결과 반환
            return new PaginatedBoardListDTO(boardList, hasNext);
        }else{
            // 일반 페이지 네이션
            dataQuery.offset(offset)
                    .limit(size);
            results = dataQuery.fetch();
            // ✅ DTO 변환
            List<BoardListDTO> boardList = results.stream().map(this::tupleToBoardListDTO).toList();

            // ✅ 메타데이터 포함한 결과 반환
            return new PaginatedBoardListDTO(page, size, totalCount, boardList);
        }
    }

    public Board save(Board board) {
        return boardRepository.save(board);
    }

    @Transactional
    public void deleteBoard(Board board) {
        board.setStatus(DataStatus.DEACTIVATED);
        boardRepository.save(board);

        //이미지 삭제
            List<AwsFileReference> references = awsFileReferenceService.findbyBoardPk(board.getPk());
            try (S3Client s3 = S3Client.builder().build()) {
                for (AwsFileReference file : references) {
                    awsService.deleteFileFromS3AndDb(s3, file);
                }
            }
    }

    @Transactional
    public Board patchBoard(Board board, String[] usedImageKeys) {
        Board saved = boardRepository.save(board);
        // 1. 기존 이미지 키 전체 조회
        List<AwsFileReference> references = awsFileReferenceService.findbyBoardPk(saved.getPk());
        Set<String> existingKeys = references.stream()
            .map(AwsFileReference::getS3Key)
            .collect(Collectors.toSet());

        // 2. 요청으로 들어온 키를 Set으로 변환
        Set<String> requestedKeys = Optional.ofNullable(usedImageKeys)
            .map(keys -> Arrays.stream(keys)
                .map(key -> env + "/board-files/" + key)
                .collect(Collectors.toSet()))
            .orElse(Collections.emptySet());

        // 3. 삭제 대상 = 기존 - 요청
        Set<String> keysToDelete = new HashSet<>(existingKeys);
        keysToDelete.removeAll(requestedKeys);

        try (S3Client s3 = S3Client.builder().build()) {
            for (AwsFileReference ref : references) {
                if (keysToDelete.contains(ref.getS3Key())) {
                    awsService.deleteFileFromS3AndDb(s3, ref);
                }
            }
        }

        // 4. 이미지 키들 등록 또는 갱신
        if (!requestedKeys.isEmpty()) {
            awsFileReferenceService.updateFilesAsUsed(
                new ArrayList<>(requestedKeys),
                UsedInType.BOARD,
                saved.getPk()
            );
        }
        return saved;
    }
}
