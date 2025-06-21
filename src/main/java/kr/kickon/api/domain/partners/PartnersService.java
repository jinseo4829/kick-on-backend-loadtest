package kr.kickon.api.domain.partners;

import kr.kickon.api.global.common.enums.DataStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PartnersService {
  private final PartnersRepository partnersRepository;

  public boolean findByUserPk(Long userPk){
    return partnersRepository.existsByUserPkAndStatus(userPk, DataStatus.ACTIVATED);
  }
}
