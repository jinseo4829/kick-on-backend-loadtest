package kr.kickon.api.global.common.entities;
import jakarta.persistence.*;
import kr.kickon.api.domain.shorts.dto.ShortsDTO.VideoResource;
import kr.kickon.api.global.common.enums.GambleStatus;
import kr.kickon.api.global.common.enums.UsedInType;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "AwsFileReference")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class AwsFileReference extends BaseEntity implements VideoResource {
    @Column(length = 512, nullable = false, unique = true, name = "s3_key")
    private String s3Key;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UsedInType usedIn;

    @Column()
    private Long referencePk;

    @Override
    public String getVideoUrl() {
        return "https://kickon-files-bucket.s3.ap-northeast-2.amazonaws.com/" + s3Key;
    }

}