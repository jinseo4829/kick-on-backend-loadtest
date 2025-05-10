package kr.kickon.api.global.common.entities;

import jakarta.persistence.*;
import kr.kickon.api.global.common.converters.BooleanConverter;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "ImageUpload")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class ImageUpload extends BaseEntity {

    @Column(nullable = false, length = 1000)
    private String imagePath;

    @Column(nullable = false)
    @Convert(converter = BooleanConverter.class)
    private Boolean isUsed;

    @Column(nullable = true)
    private LocalDateTime usedAt;
}