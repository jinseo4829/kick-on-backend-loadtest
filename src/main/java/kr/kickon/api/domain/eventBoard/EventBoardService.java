package kr.kickon.api.domain.eventBoard;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.kickon.api.domain.eventBoard.dto.GetEventBoardDTO;
import kr.kickon.api.global.common.BaseService;
import kr.kickon.api.global.common.entities.EventBoard;
import kr.kickon.api.global.common.entities.QEventBoard;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import kr.kickon.api.global.util.UUIDGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class EventBoardService implements BaseService<EventBoard> {
    private final EventBoardRepository eventBoardRepository;
    private final JPAQueryFactory queryFactory;
    private final UUIDGenerator uuidGenerator;

    @Override
    public EventBoard findById(String uuid) {
        BooleanExpression predicate = QEventBoard.eventBoard.id.eq(uuid).and(QEventBoard.eventBoard.status.eq(DataStatus.ACTIVATED));
        Optional<EventBoard> eventBoard = eventBoardRepository.findOne(predicate);
        if(eventBoard.isEmpty()) throw new NotFoundException(ResponseCode.NOT_FOUND_EVENT_BOARD);
        return eventBoard.get();
    }

    @Override
    public EventBoard findByPk(Long pk) {
        BooleanExpression predicate = QEventBoard.eventBoard.pk.eq(pk).and(QEventBoard.eventBoard.status.eq(DataStatus.ACTIVATED));
        Optional<EventBoard> eventBoard = eventBoardRepository.findOne(predicate);
        if(eventBoard.isEmpty()) throw new NotFoundException(ResponseCode.NOT_FOUND_EVENT_BOARD);
        return eventBoard.get();
    }

    public List<GetEventBoardDTO> findAll() {
        return queryFactory
                .select(Projections.fields(
                        GetEventBoardDTO.class, // DTO 클래스를 명확히 지정
                        QEventBoard.eventBoard.id,
                        QEventBoard.eventBoard.title,
                        QEventBoard.eventBoard.thumbnailUrl,
                        QEventBoard.eventBoard.embeddedUrl, // 필드명 매칭
                        QEventBoard.eventBoard.orderNum
                ))
                .from(QEventBoard.eventBoard)
                .where(QEventBoard.eventBoard.status.eq(DataStatus.ACTIVATED), QEventBoard.eventBoard.isDisplayed.isTrue())
                .orderBy(QEventBoard.eventBoard.orderNum.asc())
                .fetch();
    }

}
