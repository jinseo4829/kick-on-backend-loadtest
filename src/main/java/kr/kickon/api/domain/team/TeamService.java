package kr.kickon.api.domain.team;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.transaction.Transactional;
import kr.kickon.api.domain.migration.dto.ApiTeamDTO;
import kr.kickon.api.global.common.BaseService;
import kr.kickon.api.global.common.entities.QTeam;
import kr.kickon.api.global.common.entities.Team;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import kr.kickon.api.global.util.UUIDGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class TeamService implements BaseService<Team> {
    private final TeamRepository teamRepository;
    private final JPAQueryFactory queryFactory;
    private final UUIDGenerator uuidGenerator;
//    public List<User> findUserByEmail(String email){
//        // JPAQueryFactory
//        return queryFactory.selectFrom(QUser.user)
//                .where(QUser.user.email.eq(email))
//                .fetch();
//    }
    @Override
    public Team findById(String uuid) {
        BooleanExpression predicate = QTeam.team.id.eq(uuid).and(QTeam.team.status.eq(DataStatus.ACTIVATED));
        Optional<Team> team = teamRepository.findOne(predicate);
        return team.orElse(null);
    }

    @Override
    public Team findByPk(Long pk) {
        BooleanExpression predicate = QTeam.team.pk.eq(pk).and(QTeam.team.status.eq(DataStatus.ACTIVATED));
        Optional<Team> team = teamRepository.findOne(predicate);
        return team.orElse(null);
    }

    public Optional<Team> findByApiId(Long apiId) {
        BooleanExpression predicate = QTeam.team.apiId.eq(apiId).and(QTeam.team.status.eq(DataStatus.ACTIVATED));
        return teamRepository.findOne(predicate);
    }

    @Transactional
    public void saveTeamsByApi(List<ApiTeamDTO> apiTeams) {
        List<String> ids = new ArrayList<>();
        apiTeams.forEach(apiTeam -> {
            String id="";
            Optional<Team> team = findByApiId(apiTeam.getId());
            if(team.isPresent()) {
                Team teamObj = team.get();
                teamObj.setCode(apiTeam.getCode());
                teamObj.setNameEn(apiTeam.getName());
                teamObj.setLogoUrl(apiTeam.getLogo());
                teamRepository.save(teamObj);
            }else{
                // 중복되지 않는 ID를 생성할 때까지 반복
                do {
                    try{
                        id = uuidGenerator.generateUniqueUUID(this::findById);
                    }catch (NotFoundException ignore){
                    }
                    // 이미 생성된 ID가 배열에 있는지 확인
                    if(!ids.contains(id)) {
                        break;
                    }
                } while (true); // 중복이 있을 경우 다시 생성
                ids.add(id);
                Team teamObj = Team.builder()
                        .id(id)
                        .nameEn(apiTeam.getName())
                        .code(apiTeam.getCode())
                        .logoUrl(apiTeam.getLogo())
                        .apiId(Long.valueOf(apiTeam.getId()))
                        .build();
                teamRepository.save(teamObj);
            }
        });
    }
}
