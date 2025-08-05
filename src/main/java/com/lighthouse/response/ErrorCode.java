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
    INVALID_PASSWORD_FORMAT(1005, "올바르지 않은 비밀번호 형식입니다.", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD_CHECK(1006, "비밀번호1과 비밀번호2가 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
    INVALID_PHONE_FORMAT(1007, "올바르지 않은 전화번호 형식입니다.", HttpStatus.BAD_REQUEST),
    INVALID_VERIFICATION_CODE(1008, "인증번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
    MEMBER_REGISTER_FAIL(1009, "회원가입에 실패하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    MEMBER_UNREGISTER_FAIL(1010, "회원 탈퇴에 실패하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    KAKAO_REGISTER_LOGIN_FAIL(1010, "카카오 회원가입 또는 로그인에 실패하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    GOOGLE_REGISTER_LOGIN_FAIL(1011, "구글 회원가입 또는 로그인에 실패하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_LOGIN_TYPE(1012, "올바르지 않은 로그인 접근입니다.", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1013, "비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
    EMAIL_LOGIN_FAIL(1014, "이메일 로그인에 실패하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    MEMBER_LOGOUT_FAIL(1015, "로그아웃에 실패하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    UNAUTHORIZED(1016, "인증되지 않은 사용자입니다.", HttpStatus.UNAUTHORIZED),
    INVALID_UPDATE_TYPE(1012, "올바르지 않은 회원 정보 수정 접근입니다.", HttpStatus.BAD_REQUEST),
    MEMBER_UPDATE_FAIL(1017, "회원 정보 수정에 실패하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

    // SafeReport
    BUILDINGINFO_NOT_FOUND(2001,"건물 건축 년도 또는 거래 금액 정보가 없습니다.", HttpStatus.NOT_FOUND),
    SAFEBUILDING_NOT_FOUND(2002, "건물 위반 정보 또는 용도 정보가 없습니다.", HttpStatus.NOT_FOUND),
    RECENT_SAFEREPORT_NOT_FOUND(2003, "최근 본 안심레포트를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    RECENT_SAFEREPORT_SAVE_FAIL(2004, "최근 본 안심레포트 저장에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    RECENT_SAFEREPORT_DELETE_FAIL(2005, "최근 본 안심레포트 삭제에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    RECENT_SAFEREPORT_ACCESS_DENIED(2006, "해당 최근 본 안심레포트에 접근할 권한이 없습니다.", HttpStatus.FORBIDDEN),

    
    // wishlist
    WISHLIST_PROCESS_FAIL(3001, "찜 처리에 실패하였습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    WISHLIST_NOT_FOUND(3002,"해당 찜 기록이 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    WISHLIST_BAD_REQUEST(3003,"잘못된 인자가 포함된 요청입니다.", HttpStatus.BAD_REQUEST),
    SEARCH_HISTORY_PROCESS_FAIL(3004, "검색 기록 저장에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    // Estate
    ESTATE_NOT_FOUND(4001, "건물에 대한 정보가 없습니다.", HttpStatus.NOT_FOUND),
    // localinfo
    REGION_NOT_FOUND( 5001, "지역 정보를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    //Server Error
    SERVER_NOT_RESPONDING(99001, "서버가 응답하지 않습니다", HttpStatus.SERVICE_UNAVAILABLE),
    ELEMENT_NOT_FOUND(99002, "알맞은 데이터를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
    private final int code;
    private final String message;
    private final HttpStatus status;
}