package kr.kickon.api.global.common.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Table(name = "Notification")
@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_pk", nullable = false)
    private User receiver;

    private String type;

    private String content;

    private String redirectUrl;

    private boolean isRead;

    public void markAsRead() {
        this.isRead = true;
    }
}

