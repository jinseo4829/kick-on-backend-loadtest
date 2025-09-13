package kr.kickon.api.domain.shorts;

import com.querydsl.core.types.dsl.BooleanExpression;
import kr.kickon.api.domain.awsFileReference.AwsFileReferenceService;
import kr.kickon.api.domain.embeddedLink.EmbeddedLinkService;
import kr.kickon.api.global.common.entities.AwsFileReference;
import kr.kickon.api.global.common.entities.EmbeddedLink;
import kr.kickon.api.global.common.entities.Shorts;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.common.enums.ShortsType;
import kr.kickon.api.global.common.enums.UsedInType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShortsServiceTest {

    @InjectMocks
    private ShortsService shortsService;

    @Mock
    private ShortsRepository shortsRepository;

    @Mock
    private AwsFileReferenceService awsFileReferenceService;

    @Mock
    private EmbeddedLinkService embeddedLinkService;

    @Test
    @DisplayName("쇼츠 조회 - 존재하는 pk")
    void findByPk_success() {
        Shorts shorts = Shorts.builder().pk(1L).status(DataStatus.ACTIVATED).build();
        when(shortsRepository.findOne(any(BooleanExpression.class))).thenReturn(Optional.of(shorts));

        Shorts result = shortsService.findByPk(1L);

        assertThat(result).isNotNull();
        assertThat(result.getPk()).isEqualTo(1L);
    }

    @Test
    @DisplayName("쇼츠 조회 - 없는 pk")
    void findByPk_notFound() {
        when(shortsRepository.findOne(any(BooleanExpression.class))).thenReturn(Optional.empty());

        Shorts result = shortsService.findByPk(99L);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("UsedIn 조회 - AWS_FILE 타입")
    void getUsedIn_awsFile() {
        Shorts shorts = Shorts.builder().type(ShortsType.AWS_FILE).referencePk(10L).build();
        UsedInType usedIn = UsedInType.BOARD;

        when(awsFileReferenceService.findByPk(10L)).thenReturn(
                AwsFileReference.builder().usedIn(usedIn).build()
        );

        UsedInType result = shortsService.getUsedIn(shorts);

        assertThat(result).isEqualTo(usedIn);
        verify(awsFileReferenceService).findByPk(10L);
        verifyNoInteractions(embeddedLinkService);
    }

    @Test
    @DisplayName("UsedIn 조회 - EMBEDDED_LINK 타입")
    void getUsedIn_embeddedLink() {
        Shorts shorts = Shorts.builder().type(ShortsType.EMBEDDED_LINK).referencePk(20L).build();
        UsedInType usedIn = UsedInType.BOARD;

        when(embeddedLinkService.findByPk(20L)).thenReturn(
                EmbeddedLink.builder().usedIn(usedIn).build()
        );

        UsedInType result = shortsService.getUsedIn(shorts);

        assertThat(result).isEqualTo(usedIn);
        verify(embeddedLinkService).findByPk(20L);
        verifyNoInteractions(awsFileReferenceService);
    }

    @Test
    @DisplayName("쇼츠 저장")
    void save_success() {
        ShortsType type = ShortsType.AWS_FILE;
        Long referencePk = 5L;

        when(shortsRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        shortsService.save(type, referencePk);

        verify(shortsRepository).save(argThat(s ->
                s.getType() == type && s.getReferencePk().equals(referencePk)
        ));
    }

    @Test
    @DisplayName("쇼츠 삭제")
    void deleteByReferencePkAndType_success() {
        Shorts shorts = Shorts.builder()
                .type(ShortsType.AWS_FILE)
                .referencePk(5L)
                .status(DataStatus.ACTIVATED)
                .build();

        when(shortsRepository.findByReferencePkAndType(5L, ShortsType.AWS_FILE)).thenReturn(shorts);
        when(shortsRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        shortsService.deleteByReferencePkAndType(5L, ShortsType.AWS_FILE);

        assertThat(shorts.getStatus()).isEqualTo(DataStatus.DEACTIVATED);
        verify(shortsRepository).save(shorts);
    }
}
