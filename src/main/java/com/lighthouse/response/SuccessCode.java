package com.lighthouse.response;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SuccessCode implements ResponseCode{
	// Member
	MEMBER_FETCH_SUCCESS(1000, "회원 조회 성공"),
	MEMBER_USABLE_EMAIL_SUCCESS(1001, "사용 가능한 이메일 주소"),
	MEMBER_VERIFICATION_CODE_SENT(1002, "인증번호 전송 성공"),
	MEMBER_VERIFICATION_CODE_SUCCESS(1003, "인증번호 검증 성공"),
	MEMBER_REGISTER_SUCCESS(1002, "회원 가입 성공"),
	MEMBER_UPDATE_SUCCESS(1003, "회원 정보 수정 성공"),
	MEMBER_KAKAO_REGISTER_LOGIN_SUCCESS(1004, "카카오 회원가입 또는 로그인 성공"),
	MEMBER_GOOGLE_REGISTER_LOGIN_SUCCESS(1005, "구글 회원가입 또는 로그인 성공"),
	MEMBER_LOGIN_SUCCESS(1005, "로그인 성공"),
	MEMBER_LOGOUT_SUCCESS(1005, "로그아웃 성공");
	
	private final int code;
	private final String message;
}