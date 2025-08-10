package kr.kickon.api.domain.news;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.transaction.Transactional;

import java.util.*;

import kr.kickon.api.domain.aws.AwsService;
import kr.kickon.api.domain.awsFileReference.AwsFileReferenceService;
import kr.kickon.api.domain.embeddedLink.EmbeddedLinkService;
import kr.kickon.api.domain.news.dto.*;
import kr.kickon.api.domain.newsKick.NewsKickService;
import kr.kickon.api.domain.team.dto.TeamDTO;
import kr.kickon.api.domain.teamReporter.TeamReporterService;
import kr.kickon.api.domain.user.dto.BaseUserDTO;
import kr.kickon.api.global.common.BaseService;
import kr.kickon.api.global.common.entities.*;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.common.enums.UsedInType;
import kr.kickon.api.global.util.HtmlParserUtil;
import kr.kickon.api.global.util.UUIDGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.stream.Collectors;
import software.amazon.awssdk.services.s3.S3Client;

@Service
@Slf4j
@RequiredArgsConstructor
public class NewsService implements BaseService<News> {
    private final NewsRepository newsRepository;
    private final JPAQueryFactory queryFactory;
    private final UUIDGenerator uuidGenerator;
    private final NewsKickService newsKickService;
    private final AwsFileReferenceService awsFileReferenceService;
    private final AwsService awsService;
    private final EmbeddedLinkService embeddedLinkService;
    private final TeamReporterService teamReporterService;

    @Value("${spring.config.activate.on-profile}")
    private String env;

    // region {findById} UUID 기반 단건 조회
    @Override
    public News findById(String uuid) {
        BooleanExpression predicate = QNews.news.id.eq(uuid).and(QNews.news.status.eq(DataStatus.ACTIVATED));
        Optional<News> newsEntity = newsRepository.findOne(predicate);
        return newsEntity.orElse(null);
    }
    // endregion

    // region {findByPk} PK 기반 단건 조회
    @Override
    public News findByPk(Long pk) {
        BooleanExpression predicate = QNews.news.pk.eq(pk).and(QNews.news.status.eq(DataStatus.ACTIVATED));
        Optional<News> newsEntity = newsRepository.findOne(predicate);
        return newsEntity.orElse(null);
    }
    // endregion

    // region {createNewsWithImages} 뉴스 생성 + 이미지/영상 사용 처리
    @Transactional
    public News createNewsWithMedia(News news, String[] usedImageKeys, String[] usedVideoKeys) {
        News savedNewsEntity = newsRepository.save(news);
        // YouTube 링크 추출
        String[] embeddedLinks = HtmlParserUtil.extractYoutubeWatchLinks(savedNewsEntity.getContents());
//        System.out.println(env);
        if (usedImageKeys != null) {
            List<String> fullKeys = Arrays.stream(usedImageKeys)
                    .map(key -> env + "/news-files/" + key)
                    .collect(Collectors.toList());
            awsFileReferenceService.updateFilesAsUsed(
                    fullKeys,
                    UsedInType.NEWS,
                    savedNewsEntity.getPk()
            );
        }
        if (usedVideoKeys != null && usedVideoKeys.length > 0) {
            List<String> fullKeys = Arrays.stream(usedVideoKeys)
                .map(key -> env + "/news-files/" + key)
                .toList();

            awsFileReferenceService.updateFilesAsUsed(
                fullKeys,
                UsedInType.NEWS,
                savedNewsEntity.getPk()
            );
        }

        if (embeddedLinks.length > 0) {
            List<EmbeddedLink> links = Arrays.stream(embeddedLinks)
                .distinct()
                .map(link -> EmbeddedLink.builder()
                    .id(UUID.randomUUID().toString())
                    .url(link)
                    .usedIn(UsedInType.NEWS)
                    .referencePk(savedNewsEntity.getPk())
                    .build()
                ).collect(Collectors.toList());

            embeddedLinkService.saveAll(links);
        }

        return savedNewsEntity;
    }
    // endregion

    // region {createNewsListDTOQuery} 뉴스 목록 쿼리 생성
    public JPAQuery<Tuple> createNewsListDTOQuery(){
        QNews news = QNews.news;
        QNewsKick newsKick = QNewsKick.newsKick;
        QNewsViewHistory newsViewHistory = QNewsViewHistory.newsViewHistory;
        QNewsReply newsReply = QNewsReply.newsReply;
        QUser user = QUser.user;
        QTeam team = QTeam.team;
        return queryFactory.select(news, user, team,
                        newsKick.pk.countDistinct().coalesce(0L).as("kickCount"),
                        newsViewHistory.pk.countDistinct().coalesce(0L).as("viewCount"),
                        newsReply.pk.countDistinct().coalesce(0L).as("replyCount"))
                .from(news)
                .join(user).on(news.user.pk.eq(user.pk))
                .leftJoin(team).on(news.team.pk.eq(team.pk))
                .leftJoin(newsKick).on(news.pk.eq(newsKick.news.pk).and(newsKick.status.eq(DataStatus.ACTIVATED)))
                .leftJoin(newsViewHistory).on(news.pk.eq(newsViewHistory.news.pk).and(newsViewHistory.status.eq(DataStatus.ACTIVATED)))
                .leftJoin(newsReply).on(news.pk.eq(newsReply.news.pk).and(newsReply.status.eq(DataStatus.ACTIVATED)))
                .where(news.status.eq(DataStatus.ACTIVATED)
                        .and(user.status.eq(DataStatus.ACTIVATED)));
    }
    // endregion

    // region {fromTupleToNewsListDTO} 튜플 → DTO 변환
    public NewsListDTO fromTupleToNewsListDTO(Tuple tuple){
        QNews news = QNews.news;
        QUser user = QUser.user;
        QTeam team = QTeam.team;
        News newsEntity = tuple.get(news);
        User userEntity = tuple.get(user);
        Team teamEntity = tuple.get(team);

        NewsListDTO newsListDTO = NewsListDTO.builder()
                .pk(newsEntity.getPk())
                .title(newsEntity.getTitle())
                .content(newsEntity.getContents())
                .thumbnailUrl(newsEntity.getThumbnailUrl())
                .category(newsEntity.getCategory().getKoreanName())
                .user(BaseUserDTO.builder()
                        .id(userEntity.getId())
                        .nickname(userEntity.getNickname())
                        .profileImageUrl(userEntity.getProfileImageUrl())
                        .build())
                .createdAt(newsEntity.getCreatedAt())
                .likes(tuple.get(3, Long.class).intValue())
                .views(tuple.get(4, Long.class).intValue())
                .replies(tuple.get(5, Long.class).intValue())
                .build();


        if(teamEntity!=null){
            newsListDTO.setTeam(TeamDTO.builder()
                    .pk(teamEntity.getPk())
                    .logoUrl(teamEntity.getLogoUrl())
                    .nameKr(teamEntity.getNameKr())
                    .nameEn(teamEntity.getNameEn())
                    .build());
        }

        TeamReporter teamReporter = teamReporterService.findByUserId(newsListDTO.getUser().getId());
        if(teamReporter != null) {
            newsListDTO.getUser().setIsReporter(true);
        }

        return newsListDTO;
    }
    // endregion

    // region {getNewsDetailDTOByPk} 뉴스 상세 조회
    /**
     * 뉴스 PK와 사용자 정보로 뉴스 상세 정보를 조회합니다.
     * - 좋아요 여부(Kick 여부), 이미지 키 배열, 팀 정보 등을 포함한 DTO 반환
     */
    public NewsDetailDTO getNewsDetailDTOByPk(Long newsPk, User userData) {
        QNews news = QNews.news;
        QUser user = QUser.user;
        QTeam team = QTeam.team;

        Tuple result = createNewsListDTOQuery()
                .where(news.pk.eq(newsPk))
                .groupBy(news.pk)
                .fetchOne();
        if(result == null) return null;

        News newsEntity = result.get(news);
        User userEntity = result.get(user);
        Team teamEntity = result.get(team);

        NewsDetailDTO newsDetailDTO = new NewsDetailDTO(newsEntity, new BaseUserDTO(userEntity),result.get(3, Long.class).intValue(),result.get(4, Long.class).intValue(), result.get(5, Long.class).intValue());

        if(userData!=null){
            NewsKick newsKick = newsKickService.findByNewsAndUser(Objects.requireNonNull(result.get(news)).getPk(), userData.getPk());
            newsDetailDTO.setIsKicked(newsKick!=null);
        }

        if(teamEntity!=null){
            newsDetailDTO.setTeam(new TeamDTO(teamEntity));
        }

        String prefix = env + "/news-files/";

        List<AwsFileReference> usedImageReferences = awsFileReferenceService.findbyNewsPk(
            newsEntity.getPk()
        );
        String[] usedImageKeys = usedImageReferences.stream()
            .map(AwsFileReference::getS3Key) // 각 객체에서 S3 키만 추출
            .map(key -> key.substring(prefix.length())) // prefix 제거
            .toArray(String[]::new);

        newsDetailDTO.setUsedImageKeys(usedImageKeys);
        TeamReporter teamReporter = teamReporterService.findByUserId(newsDetailDTO.getUser().getId());
        if(teamReporter != null) {
            newsDetailDTO.getUser().setIsReporter(true);
        }
        return newsDetailDTO;
    }
    // endregion

    // region {getRecent3NewsList} 최근 뉴스 3건 조회
    /**
     * 전체 뉴스 중 최근 작성된 3개 뉴스를 조회합니다.
     */
    public List<NewsListDTO> getRecent3NewsList() {
        QNews news = QNews.news;
        QUser user = QUser.user;
        List<Tuple> results = createNewsListDTOQuery()
                .groupBy(news.pk, user.pk)
                .orderBy(news.createdAt.desc())
                .limit(3)
                .fetch();
        return results.stream().map(this::fromTupleToNewsListDTO).toList();
    }
    // endregion

    // region {getRecent3NewsListWithUserTeam} 사용자의 응원 팀 기준 뉴스 조회
    /**
     * 사용자의 응원 팀 목록(teamPks)에 해당하는 뉴스 중 최근 작성된 뉴스 최대 limit 개 조회
     * - 중복 제거 포함
     */
    public List<NewsListDTO> getRecent3NewsListWithUserTeam(Set<Long> teamPks, int limit) {
        QNews news = QNews.news;
        QUser user = QUser.user;

        List<Tuple> results = createNewsListDTOQuery()
                .where(news.team.pk.in(teamPks))
                .groupBy(news.pk, user.pk)
                .orderBy(news.createdAt.desc())
                .limit(limit)
                .fetch();

        // 중복 제거를 위한 LinkedHashSet (정렬 유지 + 중복 제거)
        LinkedHashSet<NewsListDTO> uniqueNews = results.stream()
                .map(this::fromTupleToNewsListDTO)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return new ArrayList<>(uniqueNews).stream().limit(limit).toList();
    }
    // endregion

    // region {getTop5HotNewsList} 최근 24시간 이내 작성된 뉴스 중 조회수 기준으로 상위 5개를 반환합니다.
    /**
     * 최근 24시간 이내 작성된 뉴스 중 조회수 기준으로 상위 5개를 반환합니다.
     * - 조회수는 NewsViewHistory 기준
     * - 팀 정보와 리그명 포함
     */
    public List<HotNewsListDTO> getTop5HotNewsList() {
        QNews news = QNews.news;
        QTeam team = QTeam.team;
        QNewsViewHistory newsViewHistory = QNewsViewHistory.newsViewHistory;
        QActualSeasonTeam actualSeasonTeam = QActualSeasonTeam.actualSeasonTeam;
        QActualSeason actualSeason = QActualSeason.actualSeason;
        QLeague league = QLeague.league;

        List<Tuple> results = queryFactory.select(news, team,
                        newsViewHistory.pk.count().coalesce(0L).as("viewCount"),
                        league.nameKr.as("leagueName"))
                .from(news)
                .leftJoin(newsViewHistory).on(news.pk.eq(newsViewHistory.news.pk)
                        .and(newsViewHistory.status.eq(DataStatus.ACTIVATED)))
                .leftJoin(team).on(news.team.pk.eq(team.pk))
                // 가장 최근 진행 중인 ActualSeasonTeam을 찾고, ActualSeason을 통해 League 가져오기
                .leftJoin(actualSeasonTeam).on(team.pk.eq(actualSeasonTeam.team.pk)
                        .and(actualSeasonTeam.status.eq(DataStatus.ACTIVATED)))
                .leftJoin(actualSeason).on(actualSeasonTeam.actualSeason.pk.eq(actualSeason.pk))
                .leftJoin(league).on(actualSeason.league.pk.eq(league.pk))
                .where(news.status.eq(DataStatus.ACTIVATED)
                        .and(news.createdAt.goe(LocalDateTime.now().minusDays(1)))) // 최근 24시간 이내 뉴스만 조회
                .groupBy(news.pk, league.pk) // 그룹핑 추가
                .orderBy(newsViewHistory.pk.count().coalesce(0L).desc()) // 조회수 기준 내림차순 정렬
                .limit(5)
                .fetch();

        return results.stream().map(tuple -> {
            News newsEntity = tuple.get(news);
            Team teamEntity = tuple.get(team);
            HotNewsListDTO.HotNewsListDTOBuilder builder = HotNewsListDTO.builder()
                    .pk(newsEntity.getPk())
                    .title(newsEntity.getTitle())
                    .thumbnailUrl(newsEntity.getThumbnailUrl())
                    .category(newsEntity.getCategory().getKoreanName())
                    .views(tuple.get(2, Long.class).intValue()); // 조회수 가져오기

            if (teamEntity != null) {
                builder.teamNameEn(teamEntity.getNameEn())
                        .teamNameKr(teamEntity.getNameKr())
                        .teamPk(teamEntity.getPk())
                        .teamLogoUrl(teamEntity.getLogoUrl());
            }

            // League 정보 추가
            if (tuple.get(3, String.class) != null) { // leagueName이 존재하면 추가
                builder.leagueNameKr(tuple.get(3, String.class));
            }

            return builder.build();
        }).toList();
    }
    // endregion

    // region {save} 뉴스 저장
    public News save(News news){
        return newsRepository.save(news);
    }
    // endregion

    // region {getNewsListWithPagination} 뉴스 리스트 조회 (페이징 또는 무한 스크롤 방식)
    /**
     * 뉴스 리스트 조회 (페이징 또는 무한 스크롤 방식)
     *
     * @param teamPk     팀 PK (선택)
     * @param page       페이지 번호 (1부터 시작)
     * @param size       페이지당 게시글 수
     * @param sortBy     정렬 기준 ("hot" 또는 "recent")
     * @param leaguePk   리그 PK (선택)
     * @param infiniteFlag 무한스크롤 여부
     * @param lastNewsPk 마지막으로 받은 뉴스 PK (커서 기반)
     * @param lastViewCount 마지막으로 받은 뉴스 조회수 (커서 기반)
     * @return 페이지네이션 또는 무한스크롤 응답 DTO
     */
    public PaginatedNewsListDTO getNewsListWithPagination(Long teamPk, int page, int size, String sortBy, Long leaguePk, Boolean infiniteFlag, Long lastNewsPk, Long lastViewCount) {
        QNews news = QNews.news;
        QNewsViewHistory newsViewHistory = QNewsViewHistory.newsViewHistory;
        QUser user = QUser.user;

        Integer offset = (page - 1) * size;
        LocalDateTime hotThreshold = LocalDateTime.now().minusHours(48); // 최근 48시간 기준
        List<Long> teamPks = List.of();
        // ✅ 전체 게시글 수 계산
        JPAQuery<Long> totalQuery = queryFactory.select(news.pk.count())
                .from(news)
                .join(user).on(news.user.pk.eq(user.pk))
                .where(news.status.eq(DataStatus.ACTIVATED)
                        .and(user.status.eq(DataStatus.ACTIVATED)));

        if (teamPk != null) totalQuery.where(news.team.pk.eq(teamPk));
        if ("hot".equalsIgnoreCase(sortBy)) totalQuery.where(news.createdAt.goe(hotThreshold)); // hot일 때 48시간 내 필터링
        if (leaguePk != null) {
            QActualSeason actualSeason = QActualSeason.actualSeason;
            QActualSeasonTeam actualSeasonTeam = QActualSeasonTeam.actualSeasonTeam;

            teamPks = queryFactory.select(actualSeasonTeam.team.pk)
                    .from(actualSeasonTeam)
                    .join(actualSeason).on(actualSeasonTeam.actualSeason.pk.eq(actualSeason.pk))
                    .where(actualSeason.league.pk.eq(leaguePk).and(actualSeasonTeam.status.eq(DataStatus.ACTIVATED)).and(actualSeason.status.eq(DataStatus.ACTIVATED)).and(actualSeasonTeam.team.status.eq(DataStatus.ACTIVATED)))
                    .fetch();

            totalQuery.where(news.team.pk.in(teamPks));
        }
        Long totalCount = totalQuery.fetchOne();

        // 실제 데이터 쿼리
        // ✅ 메인 쿼리
        JPAQuery<Tuple> dataQuery = createNewsListDTOQuery()
                .groupBy(news.pk, user.pk);
        List<Tuple> results;
        if (leaguePk != null) dataQuery.where(news.team.pk.in(teamPks));
        if (teamPk != null) dataQuery.where(news.team.pk.eq(teamPk));
        // ✅ 정렬 기준에 따른 동적 처리
        if ("hot".equalsIgnoreCase(sortBy)) {
            dataQuery.where(news.createdAt.goe(hotThreshold)); // 최근 48시간 필터링

            // ✅ 복합 정렬 기준 (조회수, pk)
            dataQuery.orderBy(
                    newsViewHistory.pk.countDistinct().coalesce(0L).desc(),
                    news.pk.desc()
            );

            // ✅ 커서 조건
            if (lastViewCount != null && lastNewsPk != null && lastNewsPk > 0) {
                // group by 이후 having으로 거르기보다 subquery를 활용한 커서 기반 조회
                NumberTemplate<Long> viewCountAlias = Expressions.numberTemplate(Long.class, "count(distinct {0})", newsViewHistory.pk);

                dataQuery.having(
                        viewCountAlias.lt(lastViewCount)
                                .or(viewCountAlias.eq(lastViewCount).and(news.pk.lt(lastNewsPk)))
                );
            }
        } else {
            dataQuery.orderBy(news.createdAt.desc(), news.pk.desc()); // 최신순도 pk 정렬 추가
            if (lastNewsPk != null && lastNewsPk > 0) {
                dataQuery.where(news.pk.lt(lastNewsPk));
            }
        }

        // offset 설정
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
            List<NewsListDTO> newsList = results.stream().map(this::fromTupleToNewsListDTO).toList();

            // ✅ 메타데이터 포함한 결과 반환
            return new PaginatedNewsListDTO(newsList, hasNext);
        }else{
            // 일반 페이지 네이션
            dataQuery.offset(offset)
                    .limit(size);
            results = dataQuery.fetch();
            // ✅ DTO 변환
            List<NewsListDTO> newsList = results.stream().map(this::fromTupleToNewsListDTO).toList();

            // ✅ 메타데이터 포함한 결과 반환
            return new PaginatedNewsListDTO(page, size, totalCount, newsList);
        }
    }
    // endregion

    // region {deleteNews} 뉴스 삭제 처리 (소프트 삭제 및 관련 이미지 삭제)
    /**
     * 뉴스 삭제 처리 (소프트 삭제 및 관련 이미지 삭제)
     */
    @Transactional
    public void deleteNews(News news) {
        news.setStatus(DataStatus.DEACTIVATED);
        newsRepository.save(news);

        //이미지 삭제
        List<AwsFileReference> references = awsFileReferenceService.findbyNewsPk(news.getPk());
        try (S3Client s3 = S3Client.builder().build()) {
            for (AwsFileReference file : references) {
                awsService.deleteFileFromS3AndDb(s3, file);
            }
        }
    }
    // endregion

    // region {updateNews} 뉴스 수정 처리 + 이미지 키 정리
    /**
     * 뉴스 수정 처리 + 이미지 키 정리
     *
     * @param news 수정할 뉴스 엔티티
     * @param usedImageKeys 사용된 이미지 키 목록
     * @return 저장된 뉴스 엔티티
     */
    @Transactional
    public News updateNews(News news, String[] usedImageKeys) {
        News saved = newsRepository.save(news);
        // 1. 기존 이미지 키 전체 조회
        List<AwsFileReference> references = awsFileReferenceService.findbyNewsPk(saved.getPk());
        Set<String> existingKeys = references.stream()
            .map(AwsFileReference::getS3Key)
            .collect(Collectors.toSet());

        // 2. 요청으로 들어온 키를 Set으로 변환
        Set<String> requestedKeys = Optional.ofNullable(usedImageKeys)
            .map(keys -> Arrays.stream(keys)
                .map(key -> env + "/news-files/" + key)
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
                UsedInType.NEWS,
                saved.getPk()
            );
        }
        return saved;
    }
    // endregion
}
