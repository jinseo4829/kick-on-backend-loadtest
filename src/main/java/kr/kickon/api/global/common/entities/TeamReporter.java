package kr.kickon.api.global.common.entities;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import kr.kickon.api.global.common.enums.DataStatus;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "TeamReporter")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "구단 기자 엔티티")
public class TeamReporter extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "user_pk", foreignKey = @ForeignKey(name = "fk_team_reporter_user"))
    private User user;

    @ManyToOne
    @JoinColumn(name = "team_pk", foreignKey = @ForeignKey(name = "fk_team_reporter_team"))
    private Team team;
}