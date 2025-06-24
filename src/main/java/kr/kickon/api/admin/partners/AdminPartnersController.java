package kr.kickon.api.admin.partners;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import kr.kickon.api.admin.partners.dto.PartnersListDTO;
import kr.kickon.api.admin.partners.request.PartnersFilterRequest;
import kr.kickon.api.admin.partners.response.GetPartnersResponse;
import kr.kickon.api.global.common.PagedMetaDTO;
import kr.kickon.api.global.common.ResponseDTO;
import kr.kickon.api.global.common.enums.ResponseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/partners")
@Tag(name = "파트너스")
@Slf4j
public class AdminPartnersController {

  private final AdminPartnersService adminpartnersService;
  @GetMapping
  @Operation(summary = "파트너스 리스트 조회", description = "파트너스 리스트를 조회합니다. 각 filter 조건은 옵셔널 입니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "성공",
          content = @Content(schema = @Schema(implementation = GetPartnersResponse.class))),
  })
  public ResponseEntity<ResponseDTO<List<PartnersListDTO>>> getFilteredPartners(@Valid @ModelAttribute PartnersFilterRequest request) {
    Pageable pageable = request.toPageable();
    Page<PartnersListDTO> partnersPage = adminpartnersService.findPartnersByFilter(request, pageable);

    return ResponseEntity.ok(
        ResponseDTO.success(
            ResponseCode.SUCCESS,
            partnersPage.getContent(),
            new PagedMetaDTO(
                partnersPage.getNumber() + 1,
                partnersPage.getSize(),
                partnersPage.getTotalElements()
            )
        )
    );
  }

  /*
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
  }*/
}
