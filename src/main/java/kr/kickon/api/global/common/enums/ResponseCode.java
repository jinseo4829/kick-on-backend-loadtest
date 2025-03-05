package kr.kickon.api.global.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ResponseCode {
    // ✅ 200, 201 success
    SUCCESS(HttpStatus.OK, "GET_SUCCESS", "성공"),
    CREATED(HttpStatus.CREATED, "POST_OR_PATCH_SUCCESS", "성공"),

    // ✅ 400 Bad Request
    INVALID_CREATE_USER_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_CREATE_USER_REQUEST", "유저 생성 시 잘못된 값이 존재합니다."),
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Validation failed"),
    PARAMETER_NOT_EXIST(HttpStatus.BAD_REQUEST, "PARAMETER_NOT_EXIST", "파라미터가 존재 하지 않습니다."),
    INVALID_PARSING_INPUT(HttpStatus.BAD_REQUEST, "INVALID_PARSING_INPUT", "입력값 형식이 잘못 되었습니다. (parsing 오류)"),

    // ✅ 401 Unauthorized
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다. 로그인 후 이용해주세요."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "유효하지 않은 토큰입니다. 다시 로그인해주세요."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", "유효하지 않은 Refresh 토큰입니다."),

    // ✅ 403 Forbidden
    FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "접근 권한이 없습니다."),

    // ✅ 404 Not Found
    NOT_FOUND_USER(HttpStatus.NOT_FOUND, "NOT_FOUND_USER", "해당 유저를 찾을 수 없습니다."),
    NOT_FOUND_EVENT_BOARD(HttpStatus.NOT_FOUND, "NOT_FOUND_EVENT_BOARD", "해당 이벤트 게시글을 찾을 수 없습니다."),
    NOT_FOUND_ACTUAL_SEASON_RANKING(HttpStatus.NOT_FOUND,"NOT_FOUND_ACTUAL_SEASON_RANKING","해당 실제 시즌 순위를 찾을 수 없습니다."),
    NOT_FOUND_TEAM(HttpStatus.NOT_FOUND, "NOT_FOUND_TEAM", "해당 팀을 찾을 수 없습니다."),
    NOT_FOUND_USER_FAVORITE_TEAM(HttpStatus.NOT_FOUND, "NOT_FOUND_USER_FAVORITE_TEAM", "유저가 좋아하는 팀을 찾을 수 없습니다."),
    NOT_FOUND_LEAGUE(HttpStatus.NOT_FOUND,"NOT_FOUND_LEAGUE", "해당 리그를 찾을 수 없습니다."),
    NOT_FOUND_ACTUAL_SEASON(HttpStatus.NOT_FOUND,"NOT_FOUND_ACTUAL_SEASON", "해당 실제 시즌을 찾을 수 없습니다."),
    NOT_FOUND_ACTUAL_LEAGUE_BY_LEAGUE(HttpStatus.NOT_FOUND,"NOT_FOUND_ACTUAL_LEAGUE_BY_LEAGUE", "해당 리그 PK값에 맞는 시즌을 찾을 수 없습니다."),
    NOT_FOUND_COUNTRY(HttpStatus.NOT_FOUND,"NOT_FOUND_COUNTRY", "해당 국가를 찾을 수 없습니다."),
    NOT_FOUND_GAME(HttpStatus.NOT_FOUND,"NOT_FOUND_GAME", "해당 경기를 찾을 수 없습니다."),
    NOT_FOUND_ACTUAL_SEASON_TEAM(HttpStatus.NOT_FOUND,"NOT_FOUND_ACTUAL_SEASON_TEAM", "해당 시즌의 팀을 찾을 수 없습니다."),

    // ✅ 500 Internal Server Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
