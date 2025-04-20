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
    GAMBLE_CLOSED(HttpStatus.BAD_REQUEST, "GAMBLE_CLOSED", "게임 시작 30분 전까지만 승부 예측이 가능합니다."),
    DUPLICATED_USER_GAME_GAMBLE(HttpStatus.BAD_REQUEST, "DUPLICATED_USER_GAME_GAMBLE", "이미 게임에 대한 승부예측이 진행중입니다."),
    ALREADY_FINISHED_GAMBLE(HttpStatus.BAD_REQUEST, "ALREADY_FINISHED_GAMBLE", "이미 해당 승부예측이 종료 되었습니다."),
    DUPLICATED_NICKNAME(HttpStatus.BAD_REQUEST, "DUPLICATED_NICKNAME", "이미 사용중인 닉네임 입니다."),

    // ✅ 401 Unauthorized
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "인증이 필요합니다. 로그인 후 이용해주세요."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_TOKEN", "유효하지 않은 토큰입니다. 다시 로그인해주세요."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "INVALID_REFRESH_TOKEN", "유효하지 않은 Refresh 토큰입니다."),

    // ✅ 403 Forbidden
    FORBIDDEN(HttpStatus.FORBIDDEN, "FORBIDDEN", "접근 권한이 없습니다."),
    FORBIDDEN_RESISTER(HttpStatus.FORBIDDEN, "FORBIDDEN_RESISTER","탈퇴 후 7일이 지나지 않아 재가입할 수 없습니다."),

    // ✅ 404 Not Found
    NOT_FOUND_USER(HttpStatus.NOT_FOUND, "NOT_FOUND_USER", "해당 유저를 찾을 수 없습니다."),
    NOT_FOUND_EVENT_BOARD(HttpStatus.NOT_FOUND, "NOT_FOUND_EVENT_BOARD", "해당 이벤트 게시글을 찾을 수 없습니다."),
    NOT_FOUND_ACTUAL_SEASON_RANKING(HttpStatus.NOT_FOUND,"NOT_FOUND_ACTUAL_SEASON_RANKING","해당 실제 시즌 순위를 찾을 수 없습니다."),
    NOT_FOUND_GAMBLE_SEASON_RANKING(HttpStatus.NOT_FOUND, "NOT_FOUND_GAMBLE_SEASON_RANKING", "해당 승부예측 순위를 찾을 수 없습니다."),
    NOT_FOUND_GAMBLE_SEASON(HttpStatus.NOT_FOUND, "NOT_FOUND_GAMBLE_SEASON", "해당 승부예측 시즌을 찾을 수 없습니다."),
    NOT_FOUND_TEAM(HttpStatus.NOT_FOUND, "NOT_FOUND_TEAM", "해당 팀을 찾을 수 없습니다."),
    NOT_FOUND_USER_FAVORITE_TEAM(HttpStatus.NOT_FOUND, "NOT_FOUND_USER_FAVORITE_TEAM", "유저가 좋아하는 팀을 찾을 수 없습니다."),
    NOT_FOUND_LEAGUE(HttpStatus.NOT_FOUND,"NOT_FOUND_LEAGUE", "해당 리그를 찾을 수 없습니다."),
    NOT_FOUND_BOARD(HttpStatus.NOT_FOUND,"NOT_FOUND_BOARD", "해당 게시글을 찾을 수 없습니다."),
    NOT_FOUND_NEWS(HttpStatus.NOT_FOUND,"NOT_FOUND_NEWS", "해당 뉴스를 찾을 수 없습니다."),
    NOT_FOUND_PARENT_NEWS(HttpStatus.NOT_FOUND,"NOT_FOUND_PARENT_NEWS", "해당 부모 뉴스를 찾을 수 없습니다."),
    NOT_FOUND_ACTUAL_SEASON(HttpStatus.NOT_FOUND,"NOT_FOUND_ACTUAL_SEASON", "해당 실제 시즌을 찾을 수 없습니다."),
    NOT_FOUND_ACTUAL_LEAGUE_BY_LEAGUE(HttpStatus.NOT_FOUND,"NOT_FOUND_ACTUAL_LEAGUE_BY_LEAGUE", "해당 리그 PK 값에 맞는 시즌을 찾을 수 없습니다."),
    NOT_FOUND_COUNTRY(HttpStatus.NOT_FOUND,"NOT_FOUND_COUNTRY", "해당 국가를 찾을 수 없습니다."),
    NOT_FOUND_GAME(HttpStatus.NOT_FOUND,"NOT_FOUND_GAME", "해당 경기를 찾을 수 없습니다."),
    NOT_FOUND_USER_GAME_GAMBLE(HttpStatus.NOT_FOUND,"NOT_FOUND_USER_GAME_GAMBLE", "해당 유저 참여 승부예측 경기를 찾을 수 없습니다."),
    NOT_FOUND_ACTUAL_SEASON_TEAM(HttpStatus.NOT_FOUND,"NOT_FOUND_ACTUAL_SEASON_TEAM", "해당 시즌의 팀을 찾을 수 없습니다."),
    NOT_FOUND_USER_POINT_RANKING(HttpStatus.NOT_FOUND,"NOT_FOUND_USER_POINT_RANKING", "해당 시즌의 유저 포인트 랭킹을 찾을 수 없습니다."),
    NOT_FOUND_BOARD_REPLY(HttpStatus.NOT_FOUND,"NOT_FOUND_BOARD_REPLY", "해당 게시글 댓글을 찾을 수 없습니다."),
    NOT_FOUND_NEWS_REPLY(HttpStatus.NOT_FOUND,"NOT_FOUND_NEWS_REPLY", "해당 뉴스 댓글을 찾을 수 없습니다."),

    // ✅ 500 Internal Server Error
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다."),
    AWS_PRESIGNED_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "AWS_PRESIGNED_ERROR", "Presigned Url 생성 중 내부 오류가 발생했습니다."),
    SLACK_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SLACK_SERVER_ERROR", "슬랙 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
