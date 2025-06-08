package kr.kickon.api.domain.news;

import com.querydsl.core.BooleanBuilder;
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
import kr.kickon.api.domain.news.dto.*;
import kr.kickon.api.domain.newsKick.NewsKickService;
import kr.kickon.api.domain.team.dto.TeamDTO;
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

    @Value("${spring.config.activate.on-profile}")
    private String env;

    @Override
    public News findById(String uuid) {
        BooleanExpression predicate = QNews.news.id.eq(uuid).and(QNews.news.status.eq(DataStatus.ACTIVATED));
        Optional<News> news = newsRepository.findOne(predicate);
        return news.orElse(null);
    }

    @Override
    public News findByPk(Long pk) {
        BooleanExpression predicate = QNews.news.pk.eq(pk).and(QNews.news.status.eq(DataStatus.ACTIVATED));
        Optional<News> news = newsRepository.findOne(predicate);
        return news.orElse(null);
    }

    @Transactional
    public News createNewsWithImages(News news, String[] usedImageKeys) {
        News saved = newsRepository.save(news);
//        System.out.println(env);
        if (usedImageKeys != null) {
            List<String> fullKeys = Arrays.stream(usedImageKeys)
                    .map(key -> env + "/news-files/" + key)
                    .collect(Collectors.toList());
            awsFileReferenceService.updateFilesAsUsed(
                    fullKeys,
                    UsedInType.NEWS,
                    saved.getPk()
            );
        }

        return saved;
    }

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

    public NewsListDTO tupleToNewsListDTO(Tuple tuple){
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

        return newsListDTO;
    }

    public NewsDetailDTO findNewsDetailDTOByPk(Long newsPk, User userData) {
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
        return newsDetailDTO;
    }

    public List<NewsListDTO> findRecent3News() {
        QNews news = QNews.news;
        QUser user = QUser.user;
        List<Tuple> results = createNewsListDTOQuery()
                .groupBy(news.pk, user.pk)
                .orderBy(news.createdAt.desc())
                .limit(3)
                .fetch();
        return results.stream().map(this::tupleToNewsListDTO).toList();
    }

    public List<NewsListDTO> findRecent3NewsWithUserTeam(Set<Long> teamPks, int limit) {
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
                .map(this::tupleToNewsListDTO)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return new ArrayList<>(uniqueNews).stream().limit(limit).toList();
    }

    public List<HotNewsListDTO> findTop5HotNews() {
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

    public News save(News news){
        return newsRepository.save(news);
    }

    public PaginatedNewsListDTO findNewsWithPagination(Long teamPk, int page, int size, String sortBy, Long leaguePk, Boolean infiniteFlag, Long lastNewsPk, Long lastViewCount) {
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
            List<NewsListDTO> newsList = results.stream().map(this::tupleToNewsListDTO).toList();

            // ✅ 메타데이터 포함한 결과 반환
            return new PaginatedNewsListDTO(newsList, hasNext);
        }else{
            // 일반 페이지 네이션
            dataQuery.offset(offset)
                    .limit(size);
            results = dataQuery.fetch();
            // ✅ DTO 변환
            List<NewsListDTO> newsList = results.stream().map(this::tupleToNewsListDTO).toList();

            // ✅ 메타데이터 포함한 결과 반환
            return new PaginatedNewsListDTO(page, size, totalCount, newsList);
        }
    }

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

    @Transactional
    public News patchNews(News news, String[] usedImageKeys) {
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
}
