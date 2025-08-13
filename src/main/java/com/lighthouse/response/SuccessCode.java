package com.lighthouse.response;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SuccessCode implements ResponseCode{

	// Member
	MEMBER_FETCH_SUCCESS(1001, "회원 조회 성공"),
	MEMBER_USABLE_EMAIL_SUCCESS(1002, "사용 가능한 이메일 주소"),
	MEMBER_VERIFICATION_CODE_SENT(1003, "인증번호 전송 성공"),
	MEMBER_VERIFICATION_CODE_SUCCESS(1004, "인증번호 검증 성공"),
	MEMBER_REGISTER_SUCCESS(1005, "회원 가입 성공"),
	MEMBER_UNREGISTER_SUCCESS(1006, "회원 탈퇴 성공"),
	MEMBER_KAKAO_REGISTER_LOGIN_SUCCESS(1007, "카카오 회원가입 또는 로그인 성공"),
	MEMBER_GOOGLE_REGISTER_LOGIN_SUCCESS(1008, "구글 회원가입 또는 로그인 성공"),
	MEMBER_LOGIN_EMAIL_SUCCESS(1009, "이메일 로그인 성공"),
	MEMBER_LOGOUT_SUCCESS(1010, "로그아웃 성공"),
	MEMBER_UPDATE_NAME_SUCCESS(1006, "회원 이름 변경 성공"),
	MEMBER_UPDATE_PWD_SUCCESS(1006, "회원 비밀번호 변경 성공"),
	MEMBER_UPDATE_PROFILEIMAGE_SUCCESS(1006, "회원 프로필 사진 변경 성공"),
	MEMBER_PASSWORD_VERIFICATION_SUCCESS(1006, "회원 비밀번호 검증 성공" ),

	// SafeReport
	SAFEREPORT_FETCH_SUCCESS(2000, "안심 레포트 데이터 조회 성공"),
	RECENT_SAFEREPORT_LIST_SUCCESS(2001, "최근 본 안심레포트 목록 조회 성공"),
	RECENT_SAFEREPORT_DETAIL_SUCCESS(2002, "최근 본 안심레포트 상세 조회 성공"),
	RECENT_SAFEREPORT_DELETE_SUCCESS(2003, "최근 본 안심레포트 삭제 성공"),

	// News
	NEWS_YOUTH_PROGRAM_FETCH_SUCCESS(3001, "온통청년 제공 청년 프로그램 조회 성공"),
	NEWS_YOUTH_PROGRAM_GET_UNREAD_SUCCESS(3002, "회원이 안 읽은 온통청년 콘텐츠 조회 성공"),
	
	// WishList
	WISHLIST_GETLIST_SUCCESS(3001,"찜 목록 조회 성공"),
	WISHLIST_SAVE_SUCCESS(3002, "찜 목록 추가 성공"),
	WISHLIST_DELETE_SUCCESS(3003, "찜 목록 삭제 성공"),
	WISHLIST_FIND_SUCCESS(3004, "찜 유저, 주소 조회 성공"),
	SEARCH_HISTORY_GETLIST_SUCCESS(3005,"검색 기록 조회 성공"),
	SEARCH_HISTORY_SAVE_SUCCESS(3006,"검색 기록 저장 성공"),

	// Estate
	ESTATE_FETCH_SUCCESS(4001, "건물 정보 확인 성공"),
	ESTATE_SALES_FETCH_SUCCESS(4002, "건물 판매 내역 확인 성공"),

	// localinfo
	LOCALINFO_FETCH_SUCCESS(5001, "지역 정보 조회 성공"),
	WEATHER_FETCH_SUCCESS(5002, "날씨 조회 성공"),

    // LawdCd
    LAWDCD_FETCH_SUCCESS(6001, "지역 코드 조회 성공"),
	// HomeRegister
	HOME_REGISTER_SUCCESS(7001, "집 정보 등록 성공"),

	// alarm
	ALARM_FETCH_SUCCESS(8001, "알림 목록 조회 성공"),
	ALARM_UPDATE_SUCCESS(8002, "알림 설정 변경 성공"),
	ALARM_READ_SUCCESS(8003,"알림 읽음 처리 성공"),

	// aiRecommend
	AI_RECOMMEND_SUCCESS(9001, "AI 추천 성공");


	private final int code;
	private final String message;
}