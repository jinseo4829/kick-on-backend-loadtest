package kr.kickon.api.domain.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import kr.kickon.api.domain.actualSeasonTeam.ActualSeasonTeamService;
import kr.kickon.api.domain.team.TeamService;
import kr.kickon.api.domain.user.dto.GetUserMeDTO;
import kr.kickon.api.domain.user.request.PatchUserRequest;
import kr.kickon.api.domain.user.request.PrivacyUpdateRequest;
import kr.kickon.api.domain.user.response.GetUserMeResponse;
import kr.kickon.api.domain.userFavoriteTeam.UserFavoriteTeamService;
import kr.kickon.api.global.auth.jwt.JwtTokenProvider;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.entities.*;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.common.enums.ResponseCode;
import kr.kickon.api.global.error.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeParseException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
@Tag(name = "유저 관련")
@Slf4j
public class UserController {
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserFavoriteTeamService userFavoriteTeamService;
    private final ActualSeasonTeamService actualSeasonTeamService;
    private final TeamService teamService;

    @PatchMapping("/privacy")
    @Operation(summary = "개인정보 동의", description = "개인 정보 동의")
    public ResponseEntity<ResponseDTO<Void>> updatePrivacy(@Valid @RequestBody PrivacyUpdateRequest request) throws DateTimeParseException {
        User user = jwtTokenProvider.getUserFromSecurityContext();
        userService.updatePrivacy(user, request);
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.CREATED));
    }

    @Operation(summary = "내 정보 조회", description = "jwt 기반으로 내 정보 조회, jwt 없으면 접근 제한")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공",
                    content = @Content(schema = @Schema(implementation = GetUserMeResponse.class)))
    })
    @GetMapping("/me")
    public ResponseEntity<ResponseDTO<GetUserMeDTO>> getUserMe() {
        User user = jwtTokenProvider.getUserFromSecurityContext();
        UserFavoriteTeam userFavoriteTeam = null;
        GetUserMeDTO userDto = new GetUserMeDTO(user);;
        userFavoriteTeam = userFavoriteTeamService.findByUserPk(user.getPk());
        League league = null;
        if(userFavoriteTeam != null && userFavoriteTeam.getTeam().getStatus() == DataStatus.ACTIVATED) {
            userDto.setTeamLogoUrl(userFavoriteTeam.getTeam().getLogoUrl());
            userDto.setTeamName(userFavoriteTeam.getTeam().getNameKr()!=null?userFavoriteTeam.getTeam().getNameKr():userFavoriteTeam.getTeam().getNameEn());
            ActualSeasonTeam actualSeasonTeam = actualSeasonTeamService.findLatestByTeam(userFavoriteTeam.getTeam().getPk());
            if(actualSeasonTeam != null) league = actualSeasonTeam.getActualSeason().getLeague();

        }

        if(league!=null) {
            userDto.setLeagueLogoUrl(league.getLogoUrl());
            userDto.setLeagueName(league.getNameKr()!=null?league.getNameKr(): league.getNameEn());
        }

        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS,userDto));
    }

    @Operation(summary = "내 정보 수정", description = "jwt 기반으로 내 정보 수정, jwt 없으면 접근 제한")
    @PatchMapping()
    @Transactional
    public ResponseEntity<ResponseDTO<Void>> patchUser(@Valid @RequestBody PatchUserRequest request) {
        User user = jwtTokenProvider.getUserFromSecurityContext();
        Team team = null;
        team = teamService.findByPk(request.getTeam());
        if(team != null) throw new NotFoundException(ResponseCode.NOT_FOUND_TEAM);
        user.setNickname(request.getNickname());
        userService.saveUser(user);
        if(request.getTeam()!=null){
            UserFavoriteTeam userFavoriteTeam = userFavoriteTeamService.findByUserPk(user.getPk());
            userFavoriteTeam.setTeam(team);
            userFavoriteTeamService.save(userFavoriteTeam);
        }
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.CREATED));
    }



}
