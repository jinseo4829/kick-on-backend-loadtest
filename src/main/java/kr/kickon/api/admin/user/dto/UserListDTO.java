package kr.kickon.api.admin.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.global.common.entities.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(name = "AdminUserListDTO", description = "유저 리스트 조회 응답 DTO")
@AllArgsConstructor
public class UserListDTO {

    @Schema(description = "유저 PK", example = "1")
    private Long pk;

    @Schema(description = "유저 닉네임", example = "kickon_user")
    private String nickname;

    @Schema(description = "유저 이메일", example = "user@example.com")
    private String email;

    @Schema(description = "기타 정보", example = "etc 정보 예시")
    private String etc;

    public static UserListDTO fromEntity(User user) {
        return UserListDTO.builder()
                .pk(user.getPk())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .etc(user.getEtc())
                .build();
    }
}