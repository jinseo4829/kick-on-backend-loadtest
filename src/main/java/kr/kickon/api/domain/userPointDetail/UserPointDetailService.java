package kr.kickon.api.domain.userPointDetail;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.kickon.api.domain.userPointEvent.UserPointEventRepository;
import kr.kickon.api.global.common.BaseService;
import kr.kickon.api.global.common.entities.*;
import kr.kickon.api.global.common.enums.DataStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class UserPointDetailService {
    private final UserPointDetailRepository userPointDetailRepository;

    public UserPointDetail findByPk(Long pk) {
        BooleanExpression predicate = QUserPointDetail.userPointDetail.pk.eq(pk).and(QUserPointDetail.userPointDetail.status.eq(DataStatus.ACTIVATED));
        Optional<UserPointDetail> userPointDetail = userPointDetailRepository.findOne(predicate);
        return userPointDetail.orElse(null);
    }

    public void save(UserPointDetail userPointDetail) {
        userPointDetailRepository.save(userPointDetail);
    }
}

