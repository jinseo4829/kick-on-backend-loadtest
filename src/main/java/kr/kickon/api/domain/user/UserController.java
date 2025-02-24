package kr.kickon.api.domain.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import kr.kickon.api.domain.user.dto.PrivacyUpdateRequest;
import kr.kickon.api.global.auth.oauth.dto.PrincipalUserDetail;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.entities.User;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import kr.kickon.api.global.error.exceptions.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeParseException;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
@Slf4j
public class UserController {
    private final UserService userService;

    @PatchMapping("/privacy")
//    @Operation(summary = "개인정보 동의", description = "개인 정보 동의")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "Success",
//                    content = {@Content(schema = @Schema(implementation = ResponseDTO.class))}),
//            @ApiResponse(responseCode = "404", description = "Not Found"),
//    })
    public ResponseEntity<ResponseDTO<Void>> updatePrivacy(@Valid @RequestBody PrivacyUpdateRequest request) throws DateTimeParseException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PrincipalUserDetail principalUserDetail = null;
        if (authentication != null) {
            principalUserDetail = (PrincipalUserDetail) authentication.getPrincipal();
        }
        if(principalUserDetail==null) throw new UnauthorizedException(ResponseCode.INVALID_TOKEN);
        Optional<User> user = userService.findByPk(Long.parseLong(principalUserDetail.getName()));
        if(user.isEmpty()) throw new NotFoundException(ResponseCode.NOT_FOUND_USER);
        userService.updatePrivacy(user.get(), request);
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS));
    }

}
