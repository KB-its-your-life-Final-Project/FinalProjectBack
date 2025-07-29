package com.lighthouse.member.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ValidateUtils {
    // 공백/빈값 여부 검사
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    // 이메일 형식 유효성 검사
    // : 영어로 구성, id@domain.topLevelDomain 형식
    public static boolean isValidEmailFormat(String email) {
        return email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    }

    // 비밀번호 형식 유효성 검사
    // : 대문자, 소문자, 숫자, 특수문자 각 1개 이상 포함, 최소 8자리
    public static boolean isValidPasswordFormat(String password) {
        return password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}$");
    }
}
