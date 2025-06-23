package kr.kickon.api.admin.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.kickon.api.admin.user.dto.UserDetailDTO;
import kr.kickon.api.admin.user.dto.UserListDTO;
import kr.kickon.api.admin.user.request.UserFilterRequest;
import kr.kickon.api.admin.user.response.GetUserDetailResponse;
import kr.kickon.api.admin.user.response.GetUsersResponse;
import kr.kickon.api.domain.user.UserService;
import kr.kickon.api.domain.userFavoriteTeam.UserFavoriteTeamService;
import kr.kickon.api.global.common.PagedMetaDTO;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.entities.User;
import kr.kickon.api.global.common.entities.UserFavoriteTeam;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/user")
@Tag(name = "유저")
@Slf4j
public class AdminUserController {
    private final UserService userService;
    private final UserFavoriteTeamService userFavoriteTeamService;

    @GetMapping
    @Operation(summary = "유저 리스트 조회", description = "유저 리스트를 조회합니다. 각 filter 조건은 옵셔널 입니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = GetUsersResponse.class))),
    })
    public ResponseEntity<ResponseDTO<List<UserListDTO>>> getFilteredUsers(@Valid @ModelAttribute UserFilterRequest request) {
        Pageable pageable = request.toPageable();
        Page<User> userPage = userService.findUsersByFilter(request, pageable);
        List<UserListDTO> dtos = userPage.getContent().stream()
                .map(UserListDTO::fromEntity)
                .toList();

        return ResponseEntity.ok(
                ResponseDTO.success(
                        ResponseCode.SUCCESS,
                        dtos,
                        new PagedMetaDTO(
                                userPage.getNumber() + 1,
                                userPage.getSize(),
                                userPage.getTotalElements()
                        )
                )
        );
    }

    @GetMapping("/{userPk}")
    @Operation(summary = "유저 상세 조회", description = "유저의 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = GetUserDetailResponse.class))),
    })
    public ResponseEntity<ResponseDTO<UserDetailDTO>> getUserDetail(@PathVariable Long userPk) {
        User user = userService.findByPk(userPk);
        if (user == null) throw new NotFoundException(ResponseCode.NOT_FOUND_USER);

        List<UserFavoriteTeam> favoriteTeams = userFavoriteTeamService.findAllByUserPk(userPk);

        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS, new UserDetailDTO(user, favoriteTeams)));
    }
}
