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
import java.util.UUID;
import java.util.stream.Stream;
import kr.kickon.api.domain.aws.AwsService;
import kr.kickon.api.domain.awsFileReference.AwsFileReferenceService;
import kr.kickon.api.domain.board.dto.BoardDetailDTO;
import kr.kickon.api.domain.board.dto.BoardListDTO;
import kr.kickon.api.domain.board.dto.PaginatedBoardListDTO;
import kr.kickon.api.domain.board.dto.BoardListDTO;
import kr.kickon.api.domain.board.dto.PaginatedBoardListDTO;
import kr.kickon.api.domain.embeddedLink.EmbeddedLinkService;
import kr.kickon.api.domain.partners.PartnersService;
import kr.kickon.api.domain.shorts.ShortsService;
import kr.kickon.api.domain.teamReporter.TeamReporterService;
import kr.kickon.api.domain.user.dto.BaseUserDTO;
import kr.kickon.api.domain.boardKick.BoardKickService;
import kr.kickon.api.domain.team.dto.TeamDTO;
import kr.kickon.api.global.common.BaseService;
import kr.kickon.api.global.common.entities.*;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.common.enums.ShortsType;
import kr.kickon.api.global.common.enums.UsedInType;
import kr.kickon.api.global.error.exceptions.InternalServerException;
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
    private final PartnersService partnersService;
    private final EmbeddedLinkService embeddedLinkService;
    private final TeamReporterService teamReporterService;
    private final ShortsService shortsService;
    @Value("${spring.config.activate.on-profile}")
    private String env;

    // region {findById} uuid 기준으로 게시글 조회
    /**
     * UUID를 기반으로 활성화된 게시글을 조회합니다.
     */
    @Override
    public Board findById(String uuid) {
        BooleanExpression predicate = QBoard.board.id.eq(uuid).and(QBoard.board.status.eq(DataStatus.ACTIVATED));
        Optional<Board> board = boardRepository.findOne(predicate);
        return board.orElse(null);
    }
    // endregion

    // region {findByPk} PK 기준으로 게시글 조회
    /**
     * PK를 기반으로 활성화된 게시글을 조회합니다.
     */
    @Override
    public Board findByPk(Long pk) {
        BooleanExpression predicate = QBoard.board.pk.eq(pk).and(QBoard.board.status.eq(DataStatus.ACTIVATED));
        Optional<Board> board = boardRepository.findOne(predicate);
        return board.orElse(null);
    }
    //#endregion

    // region {createBoardWithImages} 게시글 생성 및 이미지/영상 연동 처리
    /**
     * 게시글을 생성하고, 해당 게시글에 사용된 이미지/영상 파일들을 연동 처리합니다.
     */
    @Transactional
    public Board createBoardWithMedia(Board board, String[] usedImageKeys, String[] usedVideoKeys, String[] embeddedLinks) {
        Board boardEntity = boardRepository.save(board);

        if (usedImageKeys != null) {
            List<String> fullKeys = Arrays.stream(usedImageKeys)
                    .map(key -> env + "/board-files/" + key)
                    .collect(Collectors.toList());

            awsFileReferenceService.updateFilesAsUsed(
                    fullKeys,
                    UsedInType.BOARD,
                    boardEntity.getPk()
            );
        }

        if (usedVideoKeys != null && usedVideoKeys.length > 0) {
            List<String> fullKeys = Arrays.stream(usedVideoKeys)
                .map(key -> env + "/board-files/" + key)
                .toList();

            awsFileReferenceService.updateFilesAsUsed(
                fullKeys,
                UsedInType.BOARD,
                boardEntity.getPk()
            );

            List<AwsFileReference> videoFiles = awsFileReferenceService.findbyBoardPk(boardEntity.getPk());
            videoFiles.forEach(awsFile -> {
                shortsService.save(ShortsType.AWS_FILE, awsFile.getPk());
            });
        }

        if (embeddedLinks != null && embeddedLinks.length > 0) {
            List<EmbeddedLink> links = Arrays.stream(embeddedLinks)
                .distinct()
                .map(link -> EmbeddedLink.builder()
                    .id(UUID.randomUUID().toString())
                    .url(link)
                    .usedIn(UsedInType.BOARD)
                    .referencePk(boardEntity.getPk())
                    .build()
                ).collect(Collectors.toList());

            embeddedLinkService.saveAll(links);

            links.forEach(link -> {
                shortsService.save(ShortsType.EMBEDDED_LINK, link.getPk());
            });
        }

        return boardEntity;
    }
    //#endregion

    // region {createBoardListDTOQuery} 게시글 리스트 조회용 Query 생성
    /**
     * 게시글 리스트 조회에 사용되는 JPAQuery 객체를 생성합니다.
     * 유저, 팀 정보와 함께 킥 수, 조회 수, 댓글 수를 집계합니다.
     */
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
    //#endregion

    // region {tupleToBoardListDTO} 게시글 Tuple → BoardListDTO 변환
    /**
     * 게시글 Tuple 데이터를 BoardListDTO 객체로 변환합니다.
     * Board, User, Team 엔티티를 기반으로 기본 정보와 통계 수치를 포함합니다.
     */
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
                .isPinned(boardEntity.getIsPinned())
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

        TeamReporter teamReporter = teamReporterService.findByUserId(boardEntity.getUser().getId());
        boardListDTO.getUser().setIsReporter(teamReporter != null);
        return boardListDTO;
    }
    //#endregion

    // region {getTop10BoardList} 최근 게시글 10개 조회
    /**
     * 최신 게시글 10개를 조회합니다.
     */
    public List<BoardListDTO> getTop10BoardList() {
        QBoard board = QBoard.board;
        QUser user = QUser.user;
        List<Tuple> results = createBoardListDTOQuery()
                .groupBy(board.pk, user.pk)
                .orderBy(board.createdAt.desc())
                .limit(10).fetch();
        return results.stream().map(this::tupleToBoardListDTO).toList();
    }
    //#endregion

    // region {getBoardDetailDTOByPk} 게시글 상세 조회
    /**
     * 특정 게시글의 상세 정보를 조회합니다.
     * @return BoardDetailDTO
     */
    public BoardDetailDTO getBoardDetailDTOByPk(Long boardPk,User userData) {
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
                .isPinned(boardEntity.getIsPinned())
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
        boolean isInfluencer = partnersService.findByUserPk(userEntity.getPk());
        boardDetailDTO.setIsInfluencer(isInfluencer);

        TeamReporter teamReporter = teamReporterService.findByUserId(boardEntity.getUser().getId());
        boardDetailDTO.getUser().setIsReporter(teamReporter != null);

        List<EmbeddedLink> embeddedLinks = embeddedLinkService.findByBoardPk(boardEntity.getPk());
        String[] embeddedUrls = embeddedLinks.stream()
            .map(EmbeddedLink::getUrl)
            .toArray(String[]::new);
        boardDetailDTO.setEmbeddedLinks(embeddedUrls);
        return boardDetailDTO;
    }
    //#endregion

    // region {getBoardListWithPagination} 게시글 리스트 조회 (무한스크롤/페이지네이션 모두 지원)
    /**
     * 게시글 목록을 정렬 조건 및 팀 필터링에 따라 조회합니다.
     * 무한스크롤 방식과 페이지네이션 방식 모두 지원합니다.
     *
     * @param teamPk        필터링할 팀 PK (null 가능)
     * @param page          페이지 번호 (1부터 시작)
     * @param size          페이지 당 항목 수
     * @param sortBy        정렬 기준 ("hot", "latest" 등)
     * @param infiniteFlag  무한스크롤 여부
     * @param lastBoardPk   커서 기반 페이징을 위한 마지막 게시글 PK
     * @param lastViewCount 커서 기반 페이징을 위한 마지막 게시글 조회수
     * @return PaginatedBoardListDTO
     */
    public PaginatedBoardListDTO getBoardListWithPagination(Long teamPk, Integer page, Integer size, String sortBy,Boolean infiniteFlag, Long lastBoardPk, Long lastViewCount) {
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
    // endregion

    // region {save} 게시물 저장
    public Board save(Board board) {
        return boardRepository.save(board);
    }
    // endregion

    // region {deleteBoard} 게시물 삭제
    @Transactional
    public void deleteBoard(Board boardEntity) {
        boardEntity.setStatus(DataStatus.DEACTIVATED);
        boardRepository.save(boardEntity);

        //이미지 및 영상 삭제
            List<AwsFileReference> references = awsFileReferenceService.findbyBoardPk(boardEntity.getPk());
            try (S3Client s3 = S3Client.builder().build()) {
                for (AwsFileReference file : references) {
                    shortsService.deleteByReferencePkAndType(file.getPk(), ShortsType.AWS_FILE);
                    awsService.deleteFileFromS3AndDb(s3, file);
                }
            }

        //연결된 링크 삭제
        List<EmbeddedLink> embeddedLinks = embeddedLinkService.findByBoardPk(boardEntity.getPk());
        try {for (EmbeddedLink link : embeddedLinks){
            shortsService.deleteByReferencePkAndType(link.getPk(), ShortsType.EMBEDDED_LINK);
            embeddedLinkService.deleteFileFromDb(link);}
        }catch(Exception e){
            throw new InternalServerException(ResponseCode.INTERNAL_SERVER_ERROR, e.getCause());
        }


    }
    // endregion

    // region {updateBoard} 게시글 수정 시 이미지 키 등록/삭제 처리
    /**
     * 게시글을 수정하고, 수정된 이미지 키에 따라 기존 S3 이미지 참조를 갱신합니다.
     */
    @Transactional
    public Board updateBoard(Board board, String[] usedImageKeys, String[] usedVideoKeys, String[] embeddedLinks) {
        Board saved = boardRepository.save(board);
        // 1. 기존 이미지 및 영상 키 전체 조회
        List<AwsFileReference> references = awsFileReferenceService.findbyBoardPk(saved.getPk());
        Set<String> existingKeys = references.stream()
            .map(AwsFileReference::getS3Key)
            .collect(Collectors.toSet());

        // 기존 링크 전체 조회
        List<EmbeddedLink> links = embeddedLinkService.findByBoardPk(board.getPk());
        Set<String> existingLinks = links.stream()
            .map(EmbeddedLink::getUrl)
            .collect(Collectors.toSet());

        // 2. 요청으로 들어온 키를 Set으로 변환
        Set<String> requestedKeys = Stream.of(
                Optional.ofNullable(usedImageKeys).orElse(new String[0]),
                Optional.ofNullable(usedVideoKeys).orElse(new String[0])
            )
            .flatMap(Arrays::stream)
            .map(key -> env + "/board-files/" + key)
            .collect(Collectors.toSet());

        Set<String> requestedLinks = Optional.ofNullable(embeddedLinks)
            .map(arr -> Arrays.stream(arr).collect(Collectors.toSet()))
            .orElse(Collections.emptySet());

        // 3. 삭제 대상 = 기존 - 요청
        Set<String> keysToDelete = new HashSet<>(existingKeys);
        keysToDelete.removeAll(requestedKeys);

        Set<String> linksToDelete = new HashSet<>(existingLinks);
        linksToDelete.removeAll(requestedLinks);

        try (S3Client s3 = S3Client.builder().build()) {
            for (AwsFileReference ref : references) {
                if (keysToDelete.contains(ref.getS3Key())) {
                    shortsService.deleteByReferencePkAndType(ref.getPk(), ShortsType.AWS_FILE);
                    awsService.deleteFileFromS3AndDb(s3, ref);
                }
            }
        }

        if (!linksToDelete.isEmpty()) {
            links.stream()
                .filter(e -> linksToDelete.contains(e.getUrl()))
                .forEach(link -> {
                    shortsService.deleteByReferencePkAndType(link.getPk(), ShortsType.EMBEDDED_LINK);
                    embeddedLinkService.deleteFileFromDb(link);
                });
        }

        // 4. 이미지 및 영상 키들 등록 또는 갱신
        Set<String> keysToAdd = new HashSet<>(requestedKeys);
        keysToAdd.removeAll(existingKeys);

        Set<String> linksToAdd = new HashSet<>(requestedLinks);
        linksToAdd.removeAll(existingLinks);

        if (!keysToAdd.isEmpty()) {
            awsFileReferenceService.updateFilesAsUsed(new ArrayList<>(keysToAdd), UsedInType.BOARD, saved.getPk());

            List<AwsFileReference> videoFiles = awsFileReferenceService.findbyBoardPk(saved.getPk())
                .stream()
                .filter(ref -> keysToAdd.contains(ref.getS3Key()))
                .filter(ref -> {
                    String key = ref.getS3Key().toLowerCase();
                    return key.endsWith(".mp4") || key.endsWith(".mov") || key.endsWith(".avi")
                        || key.endsWith(".mkv");
                })
                .toList();
            videoFiles.forEach(awsFile -> shortsService.save(ShortsType.AWS_FILE, awsFile.getPk()));
        }

            if (!linksToAdd.isEmpty()) {
            List<EmbeddedLink> newLinks = linksToAdd.stream()
                .map(link -> EmbeddedLink.builder()
                    .id(UUID.randomUUID().toString())
                    .url(link)
                    .usedIn(UsedInType.BOARD)
                    .referencePk(board.getPk())
                    .build()
                ).collect(Collectors.toList());

            embeddedLinkService.saveAll(newLinks);

            newLinks.forEach(link -> {
                shortsService.save(ShortsType.EMBEDDED_LINK, link.getPk());
            });
        }
        return saved;
    }
    // endregion
}
