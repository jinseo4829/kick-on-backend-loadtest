package kr.kickon.api.domain.embeddedLink;

import com.querydsl.core.types.dsl.BooleanExpression;
import java.util.List;
import java.util.Optional;
import kr.kickon.api.global.common.entities.AwsFileReference;
import kr.kickon.api.global.common.entities.EmbeddedLink;
import kr.kickon.api.global.common.entities.QAwsFileReference;
import kr.kickon.api.global.common.entities.QEmbeddedLink;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.common.enums.UsedInType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmbeddedLinkService {

  private final EmbeddedLinkRepository embeddedLinkRepository;

  public EmbeddedLink findByPk(Long pk) {
    BooleanExpression predicate = QEmbeddedLink.embeddedLink.pk.eq(pk).and(QEmbeddedLink.embeddedLink.status.eq(
        DataStatus.ACTIVATED));
    Optional<EmbeddedLink> embeddedLink = embeddedLinkRepository.findOne(predicate);
    return embeddedLink.orElse(null);
  }

  public void saveAll(List<EmbeddedLink> links) {
    embeddedLinkRepository.saveAll(links);
  }

  public List<EmbeddedLink> findByBoardPk(Long boardPk){
    return embeddedLinkRepository.findByUsedInEqualsAndReferencePkEquals(UsedInType.BOARD, boardPk);
  }

  public List<EmbeddedLink> findByNewsPk(Long newsPk){
    return embeddedLinkRepository.findByUsedInEqualsAndReferencePkEquals(UsedInType.NEWS, newsPk);
  }
  public void save(EmbeddedLink embeddedLink) {
    embeddedLinkRepository.save(embeddedLink);
  }
}
