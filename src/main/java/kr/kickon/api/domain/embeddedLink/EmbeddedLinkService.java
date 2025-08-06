package kr.kickon.api.domain.embeddedLink;

import java.util.List;
import kr.kickon.api.global.common.entities.EmbeddedLink;
import kr.kickon.api.global.common.enums.UsedInType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmbeddedLinkService {

  private final EmbeddedLinkRepository embeddedLinkRepository;
  public void saveAll(List<EmbeddedLink> links) {
    embeddedLinkRepository.saveAll(links);
  }

  public List<EmbeddedLink> findByBoardPk(Long boardPk){
    return embeddedLinkRepository.findByUsedInEqualsAndReferencePkEquals(UsedInType.BOARD, boardPk);
  }
  public void save(EmbeddedLink embeddedLink) {
    embeddedLinkRepository.save(embeddedLink);
  }
}
