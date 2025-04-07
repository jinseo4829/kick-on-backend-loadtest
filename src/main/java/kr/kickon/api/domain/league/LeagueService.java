package kr.kickon.api.domain.league;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.kickon.api.global.common.BaseService;
import kr.kickon.api.global.common.entities.League;
import kr.kickon.api.global.common.entities.QActualSeason;
import kr.kickon.api.global.common.entities.QEventBoard;
import kr.kickon.api.global.common.entities.QLeague;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.common.enums.LeagueType;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import kr.kickon.api.global.util.UUIDGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class LeagueService implements BaseService<League> {
    private final LeagueRepository leagueRepository;
    private final JPAQueryFactory queryFactory;
    private final UUIDGenerator uuidGenerator;

    @Override
    public League findById(String uuid) {
        BooleanExpression predicate = QLeague.league.id.eq(uuid).and(QLeague.league.status.eq(DataStatus.ACTIVATED));
        Optional<League> league = leagueRepository.findOne(predicate);
        return league.orElse(null);
    }

    @Override
    public League findByPk(Long pk) {
        BooleanExpression predicate = QLeague.league.pk.eq(pk).and(QLeague.league.status.eq(DataStatus.ACTIVATED));
        Optional<League> league = leagueRepository.findOne(predicate);
        return league.orElse(null);
    }

    public League findByApiId(Long apiId) {
        BooleanExpression predicate = QLeague.league.apiId.eq(apiId).and(QLeague.league.status.eq(DataStatus.ACTIVATED));
        Optional<League> league = leagueRepository.findOne(predicate);
        if(league.isPresent()) return league.get();
        throw new NotFoundException(ResponseCode.NOT_FOUND_LEAGUE);
    }

    public void save(League league) {
        leagueRepository.save(league);
    }

    public List<League> findAllLeagues(){
        return queryFactory.selectFrom(QLeague.league)
                .where(QLeague.league.status.eq(DataStatus.ACTIVATED), QLeague.league.type.eq(LeagueType.League))
                .fetch();
    }
    public List<League> findAll(){
        return queryFactory.selectFrom(QLeague.league)
                .where(QLeague.league.status.eq(DataStatus.ACTIVATED))
                .fetch();
    }

    public List<League> findAllBySeason(Integer season){
        QLeague league = QLeague.league;
        QActualSeason actualSeason = QActualSeason.actualSeason;
        return queryFactory.selectFrom(league)
                .join(actualSeason).on(actualSeason.league.pk.eq(league.pk))
                .where(league.status.eq(DataStatus.ACTIVATED).and(actualSeason.status.eq(DataStatus.ACTIVATED)).and(actualSeason.year.eq(season)))
                .fetch();
    }
}
