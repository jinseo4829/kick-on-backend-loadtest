package kr.kickon.api.domain.gambleSeasonTeam;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import kr.kickon.api.domain.gambleSeasonTeam.GambleSeasonTeamRepository;
import kr.kickon.api.domain.team.dto.SeasonTeamDTO;
import kr.kickon.api.domain.team.dto.TeamDTO;
import kr.kickon.api.global.common.BaseService;
import kr.kickon.api.global.common.entities.GambleSeasonTeam;
import kr.kickon.api.global.common.entities.QGambleSeasonTeam;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.common.enums.OperatingStatus;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import kr.kickon.api.global.util.UUIDGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class GambleSeasonTeamService implements BaseService<GambleSeasonTeam> {
    private final GambleSeasonTeamRepository gambleSeasonTeamRepository;
    private final JPAQueryFactory queryFactory;
    private final UUIDGenerator uuidGenerator;

    @Override
    public GambleSeasonTeam findById(String uuid) {
        BooleanExpression predicate = QGambleSeasonTeam.gambleSeasonTeam.id.eq(uuid).and(QGambleSeasonTeam.gambleSeasonTeam.status.eq(DataStatus.ACTIVATED));
        Optional<GambleSeasonTeam> gambleSeasonTeam = gambleSeasonTeamRepository.findOne(predicate);
        return gambleSeasonTeam.orElse(null);
    }

    @Override
    public GambleSeasonTeam findByPk(Long pk) {
        BooleanExpression predicate = QGambleSeasonTeam.gambleSeasonTeam.pk.eq(pk).and(QGambleSeasonTeam.gambleSeasonTeam.status.eq(DataStatus.ACTIVATED));
        Optional<GambleSeasonTeam> gambleSeasonTeam = gambleSeasonTeamRepository.findOne(predicate);
        if(gambleSeasonTeam.isEmpty()) throw new NotFoundException(ResponseCode.NOT_FOUND_GAMBLE_SEASON);
        return gambleSeasonTeam.get();
    }

    public GambleSeasonTeam findRecentOperatingByTeamPk(Long pk) {
        QGambleSeasonTeam gambleSeasonTeam = QGambleSeasonTeam.gambleSeasonTeam;
        BooleanExpression predicate = gambleSeasonTeam.team.pk.eq(pk).and(gambleSeasonTeam.status.eq(DataStatus.ACTIVATED).and(gambleSeasonTeam.gambleSeason.operatingStatus.eq(OperatingStatus.PROCEEDING)).and(gambleSeasonTeam.gambleSeason.status.eq(DataStatus.ACTIVATED)));
        Optional<GambleSeasonTeam> gambleSeasonTeamData = gambleSeasonTeamRepository.findOne(predicate);
        return gambleSeasonTeamData.orElse(null);
    }

    public List<SeasonTeamDTO> findAllByGambleSeasonPk(Long seasonPk) {
        return gambleSeasonTeamRepository
            .findAllByGambleSeason_PkAndStatus(seasonPk, DataStatus.ACTIVATED)
            .stream()
            .map(gst -> new SeasonTeamDTO(gst.getTeam()))
            .toList();
    }

}

