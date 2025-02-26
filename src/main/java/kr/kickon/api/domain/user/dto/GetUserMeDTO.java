package kr.kickon.api.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.global.common.entities.Team;
import kr.kickon.api.global.common.entities.User;
import kr.kickon.api.global.common.entities.UserFavoriteTeam;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Field;

@Getter
@Setter
@Schema(name = "GetUserMeDTO", description = "사용자 고유 ID")
public class GetUserMeDTO {
    @Schema(description = "사용자 고유 ID", example = "sdfjkl2-sldkjf2-wkjl")
    private String id;

    @Schema(description = "닉네임", example = "민서짱9")
    private String nickname;


    private String profileImageUrl;


    private String logoUrl;

    public GetUserMeDTO(User user,Team team) {
        this.id = user.getId();
        this.nickname = user.getNickname();
        this.profileImageUrl = user.getProfileImageUrl();
        this.logoUrl = team.getLogoUrl();
        applySchemaAnnotations(GetUserMeDTO.class,User.class);
        applySchemaAnnotations(GetUserMeDTO.class,Team.class);
    }

    public GetUserMeDTO(User user) {
        this.id = user.getId();
        this.nickname = user.getNickname();
        this.profileImageUrl = user.getProfileImageUrl();
        this.logoUrl = null;
        applySchemaAnnotations(GetUserMeDTO.class,User.class);
    }

    private void applySchemaAnnotations(Class<?> dtoClass, Class<?> entityClass) {
        for (Field dtoField : dtoClass.getDeclaredFields()) {
            try {
                Field entityField = entityClass.getDeclaredField(dtoField.getName());
                Schema schema = entityField.getAnnotation(Schema.class);
                if (schema != null) {
                    dtoField.setAccessible(true);
                    dtoField.getAnnotation(Schema.class).description();
                    dtoField.getAnnotation(Schema.class).example();
                }
            } catch (NoSuchFieldException ignored) {
            }
        }
    }
}
