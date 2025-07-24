package com.lighthouse.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ErrorCode implements ResponseCode{
    // Member
    MEMBER_NOT_FOUND(1001, "회원 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    DUPLICATE_EMAIL(1002, "이미 사용 중인 이메일입니다.", HttpStatus.CONFLICT),
    EMAIL_SEND_FAIL(1003, "인증번호 전송 실패", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_EMAIL_FORMAT(1004, "올바르지 않은 이메일 형식입니다.", HttpStatus.BAD_REQUEST),
    INVALID_PHONE_FORMAT(1005, "올바르지 않은 비밀번호 형식입니다.", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD_FORMAT(1006, "올바르지 않은 전화번호 형식입니다.", HttpStatus.BAD_REQUEST),
    INVALID_VERRIFICATION_CODE(1007, "인증번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
    MEMBER_REGISTER_FAIL(1006, "회원가입에 실패하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    KAKAO_REGISTER_LOGIN_FAIL(1007, "카카오 회원가입 또는 로그인에 실패하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    GOOGLE_REGISTER_LOGIN_FAIL(1007, "구글 회원가입 또는 로그인에 실패하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    EMAIL_LOGIN_FAIL(1007, "이메일 로그인에 실패하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    MEMBER_LOGOUT_FAIL(1008, "로그아웃에 실패하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final int code;
    private final String message;
    private final HttpStatus status;
}