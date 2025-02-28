package kr.kickon.api.domain.league;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.kickon.api.domain.eventBoard.dto.GetEventBoardDTO;
import kr.kickon.api.global.common.BaseService;
import kr.kickon.api.global.common.entities.ActualSeasonRanking;
import kr.kickon.api.global.common.entities.League;
import kr.kickon.api.global.common.entities.QActualSeasonRanking;
import kr.kickon.api.global.common.entities.QLeague;
import kr.kickon.api.global.common.enums.DataStatus;
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
        if(league.isEmpty()) throw new NotFoundException(ResponseCode.NOT_FOUND_LEAGUE);
        return league.get();
    }

    @Override
    public League findByPk(Long pk) {
        BooleanExpression predicate = QLeague.league.pk.eq(pk).and(QLeague.league.status.eq(DataStatus.ACTIVATED));
        Optional<League> league = leagueRepository.findOne(predicate);
        if(league.isEmpty()) throw new NotFoundException(ResponseCode.NOT_FOUND_LEAGUE);
        return league.get();
    }
}
