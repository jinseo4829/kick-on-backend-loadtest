package kr.kickon.api.admin.user.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Data
@Schema(description = "유저 리스트 조회를 위한 필터링 요청 객체")
public class UserFilterRequest {

    @Schema(description = "이메일", example = "user@example.com")
    private String email;

    @Schema(description = "닉네임", example = "축잘알유저")
    private String nickname;

    @Schema(description = "페이지 번호 (1부터 시작)", example = "1")
    private Integer page = 1;

    @Schema(description = "페이지 크기", example = "20")
    private Integer size = 20;

    public Pageable toPageable() {
        int pageIndex = Math.max(this.page - 1, 0); // 0-based
        return PageRequest.of(pageIndex, size);
    }
}