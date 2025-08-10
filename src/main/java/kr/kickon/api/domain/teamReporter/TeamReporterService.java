package kr.kickon.api.domain.teamReporter;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.kickon.api.global.common.BaseService;
import kr.kickon.api.global.common.entities.*;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.util.UUIDGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class TeamReporterService implements BaseService<TeamReporter> {
    private final TeamReporterRepository teamReporterRepository;
    private final JPAQueryFactory queryFactory;
    private final UUIDGenerator uuidGenerator;

    @Override
    public TeamReporter findById(String uuid) {
        BooleanExpression predicate = QTeamReporter.teamReporter.id.eq(uuid).and(QTeamReporter.teamReporter.status.eq(DataStatus.ACTIVATED));
        Optional<TeamReporter> teamReporter = teamReporterRepository.findOne(predicate);
        return teamReporter.orElse(null);
    }

    @Override
    public TeamReporter findByPk(Long pk) {
        BooleanExpression predicate = QTeamReporter.teamReporter.pk.eq(pk).and(QTeamReporter.teamReporter.status.eq(DataStatus.ACTIVATED));
        Optional<TeamReporter> teamReporter = teamReporterRepository.findOne(predicate);
        return teamReporter.orElse(null);
    }

    public TeamReporter findByUser(User user) {
        return teamReporterRepository.findByUserAndStatus(user, DataStatus.ACTIVATED);
    }

    public TeamReporter findByUserId(String userId) {
        return teamReporterRepository.findByUserIdAndStatus(userId, DataStatus.ACTIVATED);
    }

    public void save(TeamReporter teamReporter) {
        teamReporterRepository.save(teamReporter);
    }
}
