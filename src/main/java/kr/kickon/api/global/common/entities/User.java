package kr.kickon.api.global.common.entities;

import jakarta.persistence.*;
import kr.kickon.api.global.common.enums.ProviderType;
import kr.kickon.api.global.common.enums.UserAccountStatus;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "User")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity{
    @Column(nullable = false, length = 255)
    private String providerId;

    @Column(columnDefinition = "text")
    private String profileImageUrl;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ProviderType provider;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @ColumnDefault(UserAccountStatus.VALUE.DEFAULT)
    private UserAccountStatus userStatus = UserAccountStatus.DEFAULT;

    @Column(nullable = false, length = 256)
    private String email;

    @Column(length = 10)
    private String nickname;

    @Column()
    private LocalDateTime privacyAgreedAt;

    @Column()
    private LocalDateTime marketingAgreedAt;

    @Override
    public String toString() {

        return getId() + " / " + getProviderId() + " / " + provider + getProfileImageUrl() + " / " + getNickname() + " / " + getEmail() + " / " + getPrivacyAgreedAt();
    }
}


