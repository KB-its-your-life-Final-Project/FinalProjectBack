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
    INVALID_PHONE_FORMAT(1003, "올바르지 않는 전화번호 형식입니다.", HttpStatus.BAD_REQUEST),

    // SafeReport
    SAFEREPORT_NOT_FOUND(2001, "해당 위도/경도에 대한 건물 데이터가 없습니다.", HttpStatus.NOT_FOUND);

    private final int code;
    private final String message;
    private final HttpStatus status;
}