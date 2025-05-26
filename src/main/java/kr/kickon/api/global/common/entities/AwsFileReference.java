package kr.kickon.api.global.common.entities;
import jakarta.persistence.*;
import kr.kickon.api.global.common.enums.GambleStatus;
import kr.kickon.api.global.common.enums.UsedInType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "AwsFileReference")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class AwsFileReference extends BaseEntity {

    @Column(length = 512, nullable = false, unique = true)
    private String s3Key;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UsedInType usedIn;

    @Column()
    private Long referencePk;
}