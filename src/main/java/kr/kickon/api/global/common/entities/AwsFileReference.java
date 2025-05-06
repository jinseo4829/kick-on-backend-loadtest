package kr.kickon.api.global.common.entities;
import jakarta.persistence.*;
import kr.kickon.api.global.common.enums.UsedInType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "AwsFileReference")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class AwsFileReference extends BaseEntity {

    @Column(name = "s3_key", length = 512, nullable = false, unique = true)
    private String s3Key;

    @Enumerated(EnumType.STRING)
    @Column(name = "used_in", nullable = false)
    private UsedInType usedIn;

    @Column(name = "reference_pk")
    private Long referencePk;
}