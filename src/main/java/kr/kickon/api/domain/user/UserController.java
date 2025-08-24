package kr.kickon.api.domain.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.kickon.api.domain.actualSeasonTeam.ActualSeasonTeamService;
import kr.kickon.api.domain.league.dto.LeagueDTO;
import kr.kickon.api.domain.partners.PartnersService;
import kr.kickon.api.domain.team.TeamService;
import kr.kickon.api.domain.team.dto.FavoriteTeamDTO;
import kr.kickon.api.domain.team.dto.TeamDTO;
import kr.kickon.api.domain.teamReporter.TeamReporterService;
import kr.kickon.api.domain.user.dto.UserMeDto;
import kr.kickon.api.domain.user.request.DeleteUserRequest;
import kr.kickon.api.domain.user.request.PatchUserRequest;
import kr.kickon.api.domain.user.request.PrivacyUpdateRequest;
import kr.kickon.api.domain.user.response.GetUserMeResponse;
import kr.kickon.api.domain.userFavoriteTeam.UserFavoriteTeamService;
import kr.kickon.api.global.auth.jwt.user.JwtTokenProvider;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.entities.*;
import kr.kickon.api.global.common.enums.DataStatus;
import kr.kickon.api.global.common.enums.ResponseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

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
    private final PartnersService partnersService;
    private final TeamReporterService teamReporterService;

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
    public ResponseEntity<ResponseDTO<UserMeDto>> getUserMe() {
        User user = jwtTokenProvider.getUserFromSecurityContext();
        List<UserFavoriteTeam> userFavoriteTeams;
        UserMeDto userDto = new UserMeDto(user);
        boolean isInfluencer = partnersService.findByUserPk(user.getPk());
        userDto.setIsInfluencer(isInfluencer);
        userFavoriteTeams = userFavoriteTeamService.findTop3ByUserPkOrderByPriorityNumAsc(user.getPk());

        List<FavoriteTeamDTO> teamDTOList = new ArrayList<>();
        League league = null;

        for (UserFavoriteTeam fav : userFavoriteTeams) {
            // 혹시나 다른거 예외처리
            if (fav.getStatus() != DataStatus.ACTIVATED ||
                    fav.getTeam() == null ||
                    fav.getTeam().getStatus() != DataStatus.ACTIVATED) {
                continue;
            }
            FavoriteTeamDTO favoriteTeamDTO = FavoriteTeamDTO.builder()
                    .pk(fav.getTeam().getPk())
                    .nameKr(fav.getTeam().getNameKr())
                    .nameEn(fav.getTeam().getNameEn())
                    .logoUrl(fav.getTeam().getLogoUrl())
                    .priorityNum(fav.getPriorityNum())
                    .build();

            ActualSeasonTeam actualSeasonTeam = actualSeasonTeamService.findLatestByTeam(fav.getTeam().getPk());
            if (actualSeasonTeam != null && actualSeasonTeam.getActualSeason() != null) {
                league = actualSeasonTeam.getActualSeason().getLeague();
                favoriteTeamDTO.setLeaguePk(league.getPk());
                favoriteTeamDTO.setLeagueNameEn(league.getNameEn());
                favoriteTeamDTO.setLeagueNameKr(league.getNameKr());
                favoriteTeamDTO.setLeagueLogoUrl(league.getLogoUrl());
            }
            teamDTOList.add(favoriteTeamDTO);
        }

        userDto.setFavoriteTeams(teamDTOList);
        if (league != null) {
            userDto.setLeague(new LeagueDTO(league));
        }

        TeamReporter teamReporter = teamReporterService.findByUser(user);
        if(teamReporter != null) {
            userDto.setIsReporter(true);
        }

        // 응원팀 변경 가능 여부 및 다음 변경 가능일 계산
        List<UserFavoriteTeam> allFavoriteTeams = userFavoriteTeamService.findAllByUserPk(user.getPk());

        // 기존 활성화된 팀 PK Set (순서 무시)
        Set<Long> currentTeamPkSet = allFavoriteTeams.stream()
                .filter(uf -> uf.getStatus() == DataStatus.ACTIVATED)
                .map(uf -> uf.getTeam().getPk())
                .collect(Collectors.toSet());

        Set<Long> requestedTeamPkSet = currentTeamPkSet; // 프론트에 팀 구성 요청은 없음 → 현재 구성 기준

        boolean canChangeTeam = true;
        LocalDateTime nextAvailableChangeDate = null;

        //  가장 최근 updatedAt 기준으로 검사
        Optional<LocalDateTime> latestUpdatedAt = allFavoriteTeams.stream()
                .filter(uf -> uf.getStatus() == DataStatus.ACTIVATED)
                .map(UserFavoriteTeam::getUpdatedAt)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo);

        if (latestUpdatedAt.isPresent() &&
                latestUpdatedAt.get().plusMonths(6).isAfter(LocalDateTime.now())) {
            canChangeTeam = false;
            nextAvailableChangeDate = latestUpdatedAt.get().plusMonths(6);
        }

        userDto.setCanChangeTeam(canChangeTeam);
        userDto.setNextAvailableChangeDate(nextAvailableChangeDate);


        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS,userDto));
    }

    @Operation(summary = "내 정보 수정", description = "jwt 기반으로 내 정보 수정, jwt 없으면 접근 제한")
    @PatchMapping()
    public ResponseEntity<ResponseDTO<Void>> patchUser(@Valid @RequestBody PatchUserRequest request) {
        User user = jwtTokenProvider.getUserFromSecurityContext();
        userService.updateUser(user, request);
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.CREATED));
    }

    @Operation(summary = "회원 탈퇴", description = "jwt 기반으로 회원 탈퇴")
    @DeleteMapping("/me")
    public ResponseEntity<ResponseDTO<Void>> deleteUser(@Valid @RequestBody DeleteUserRequest request) {
        User user = jwtTokenProvider.getUserFromSecurityContext();
        user.setReason(request.getReason());
        userService.saveUser(user);
        userService.deleteUser(user);
        return ResponseEntity.ok(ResponseDTO.success(ResponseCode.SUCCESS));
    }

}
