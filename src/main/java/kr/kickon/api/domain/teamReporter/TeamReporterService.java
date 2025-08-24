package kr.kickon.api.domain.teamReporter;

import com.querydsl.core.types.dsl.BooleanExpression;
import kr.kickon.api.global.common.entities.*;
import kr.kickon.api.global.common.enums.DataStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class TeamReporterService {
    private final TeamReporterRepository teamReporterRepository;

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
