package com.lighthouse.response;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SuccessCode implements ResponseCode{
	// Member
	MEMBER_FETCH_SUCCESS(100, "회원 조회 성공"),
	MEMBER_CREATE_SUCCESS(101, "회원 가입 성공"),
	MEMBER_UPDATE_SUCCESS(102, "회원 정보 수정 성공");
	
	private final int code;
	private final String message;
}