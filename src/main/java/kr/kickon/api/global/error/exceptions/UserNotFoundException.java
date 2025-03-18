package kr.kickon.api.global.error.exceptions;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException() {
        super("해당 이메일을 가진 회원을 찾을 수 없습니다.");
    }

    public UserNotFoundException(String message) {
        super(message);
    }
}