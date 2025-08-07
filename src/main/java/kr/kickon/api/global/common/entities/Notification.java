package kr.kickon.api.global.common.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_pk", nullable = false)
    private User receiver;

    private String type;

    private String content;

    private String redirectUrl;

    @Column(name = "is_read", nullable = false)
    private boolean read;

    public void markAsRead() {
        this.read = true;
    }
}


