package kr.kickon.api.domain.team;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.transaction.Transactional;
import java.util.stream.Collectors;
import kr.kickon.api.domain.team.dto.TeamDTO;
import kr.kickon.api.domain.team.request.TeamListFilterRequest;
import kr.kickon.api.global.common.entities.League;
import kr.kickon.api.global.common.entities.QActualSeason;
import kr.kickon.api.global.common.entities.QActualSeasonTeam;
import kr.kickon.api.global.common.entities.QLeague;
import kr.kickon.api.global.common.entities.QTeam;
import kr.kickon.api.global.common.entities.Team;
import kr.kickon.api.global.common.enums.DataStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class TeamService{
    private final TeamRepository teamRepository;
    private final JPAQueryFactory queryFactory;
//    public List<User> findUserByEmail(String email){
//        // JPAQueryFactory
//        return queryFactory.selectFrom(QUser.user)
//                .where(QUser.user.email.eq(email))
//                .fetch();
//    }

    // region {findByPk} 팀 PK를 기반으로 활성화된 팀 엔티티를 조회합니다.
    /**
     * 팀 PK를 기반으로 활성화된 팀 엔티티를 조회합니다.
     */
    public Team findByPk(Long pk) {
        BooleanExpression predicate = QTeam.team.pk.eq(pk).and(QTeam.team.status.eq(DataStatus.ACTIVATED));
        Optional<Team> team = teamRepository.findOne(predicate);
        return team.orElse(null);
    }
    // endregion

    // region {findAll} 전체 활성화된 팀 목록을 조회합니다.
    /**
     * 전체 활성화된 팀 목록을 조회합니다.
     */
    public List<Team> findAll(){
        return queryFactory.selectFrom(QTeam.team).where(QTeam.team.status.eq(DataStatus.ACTIVATED)).fetch();
    }
    // endregion

    // region {findByApiId} API ID를 기반으로 활성화된 팀 엔티티를 조회합니다.
    /**
     * API ID를 기반으로 활성화된 팀 엔티티를 조회합니다.
     */
    public Team findByApiId(Long apiId) {
        BooleanExpression predicate = QTeam.team.apiId.eq(apiId).and(QTeam.team.status.eq(DataStatus.ACTIVATED));
        Optional<Team> team = teamRepository.findOne(predicate);
        return team.orElse(null);
    }
    // endregion

    // region {findByKeyword} 한글 또는 영어 이름에 키워드를 포함하는 활성화된 팀 목록을 조회합니다.
    /**
     * 한글 또는 영어 이름에 키워드를 포함하는 활성화된 팀 목록을 조회합니다.
     */
    public List<Team> findByKeyword(String keyword) {
        QTeam team = QTeam.team;
        return queryFactory.selectFrom(team)
                .where(team.nameKr.containsIgnoreCase(keyword)
                        .or(team.nameEn.containsIgnoreCase(keyword)))
                .where(team.status.eq(DataStatus.ACTIVATED))
                .fetch();
    }
    // endregion

    // region {save} 팀 엔티티를 저장합니다.
    /**
     * 팀 엔티티를 저장합니다.
     */
    @Transactional
    public Team save(Team team) {
        return teamRepository.save(team);
    }
    // endregion

    // region {getTeamListByFilter} 필터 조건과 페이징 정보를 기반으로 팀 목록을 조회합니다.
    /**
     * 필터 조건과 페이징 정보를 기반으로 팀 목록을 조회합니다.
     * - 실제 시즌, 리그와 연결된 팀만 조회
     * - 키워드 검색 지원 (팀 이름 한글/영문)
     */
    @Transactional
    public Page<TeamDTO> getTeamListByFilter(TeamListFilterRequest request, Pageable pageable) {
        QTeam team = QTeam.team;
        QActualSeasonTeam ast = QActualSeasonTeam.actualSeasonTeam;
        QActualSeason actualSeason = QActualSeason.actualSeason;
        QLeague league = QLeague.league;

        BooleanBuilder builder = new BooleanBuilder()
            .and(team.status.eq(DataStatus.ACTIVATED))
            .and(ast.status.eq(DataStatus.ACTIVATED))
            .and(actualSeason.status.eq(DataStatus.ACTIVATED))
            .and(league.status.eq(DataStatus.ACTIVATED));

        if (request.getLeague() != null) {
            builder.and(league.pk.eq(request.getLeague()));
        }

        if (request.getKeyword() != null && !request.getKeyword().isBlank()) {
            builder.and(
                team.nameKr.containsIgnoreCase(request.getKeyword())
                    .or(team.nameEn.containsIgnoreCase(request.getKeyword()))
            );
        }

        // Count query
        Long total = Optional.ofNullable(
            queryFactory
                .select(team.countDistinct())
                .from(ast)
                .join(ast.team, team)
                .join(ast.actualSeason, actualSeason)
                .join(actualSeason.league, league)
                .where(builder)
                .fetchOne()
        ).orElse(0L);

        // Fetch content
        List<Tuple> results = queryFactory
            .select(team, league)
            .from(ast)
            .join(ast.team, team)
            .join(ast.actualSeason, actualSeason)
            .join(actualSeason.league, league)
            .where(builder)
            .orderBy(actualSeason.startedAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        List<TeamDTO> teamDTOs = results.stream().map(t -> {
            Team tm = t.get(team);
            League lg = t.get(league);
            return TeamDTO.builder()
                .pk(tm.getPk())
                .nameKr(tm.getNameKr())
                .nameEn(tm.getNameEn())
                .logoUrl(tm.getLogoUrl())
                .leaguePk(lg.getPk())
                .leagueNameKr(lg.getNameKr())
                .leagueNameEn(lg.getNameEn())
                .build();
        }).collect(Collectors.toList());

        return new PageImpl<>(teamDTOs, pageable, total);
    }
    // endregion

}
