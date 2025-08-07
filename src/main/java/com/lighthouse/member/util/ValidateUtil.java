package com.lighthouse.member.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ValidateUtil {
    /**
     * null 또는 공백 여부 검사
     * @param str 검사할 문자열
     * @return null 또는 공백이면 true, 아니면 false
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 이름 형식 유효성 검사
     * - 한글 또는 영문으로 2~20자 확인
     * @param name 검사할 이름
     * @return 유효한 형식이면 true, 아니면 false
     */
    public static boolean isValidNameFormat(String name) {
        return name.matches("^[가-힣a-zA-Z]{2,20}$");
    }

    /**
     * 이메일 형식 유효성 검사
     * - 이메일이 id@domain.tld (id@domain.topLevelDomain) 형식 확인
     * @param email 검사할 이메일 주소
     * @return 유효한 이메일 형식이면 true, 아니면 false
     */
    public static boolean isValidEmailFormat(String email) {
        return email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    }

    /**
     * 비밀번호 형식 유효성 검사
     * - 대문자, 소문자, 숫자, 특수문자 각각 1개 이상 포함 확인
     * - 최소 8자 이상 확인
     * @param password 검사할 비밀번호
     * @return 유효한 비밀번호 형식이면 true, 아니면 false
     */
    public static boolean isValidPasswordFormat(String password) {
        return password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,}$");
    }

    /**
     * IP 유효성 검사
     * - null, 빈 문자열, "unknown"이 아닌지 확인
     * @param ip 검사할 IP 주소
     * @return 유효한 IP 형식이면 true, 아니면 false
     */
    public static boolean isValidIp(String ip) {
        return ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip);
    }

    /**
     * 이미지 파일 MIME 타입 유효성 검사
     * - 허용: image/jpeg, image/jpg, image/svg, image/png, image/webp
     * @param imgType 이미지 MIME 타입
     * @return 유효한 이미지 타입이면 true, 아니면 false
     */
    public static boolean isValidImgType(String imgType) {
        if (isEmpty(imgType)) {
            return false;
        }
        return imgType.equals("image/jpeg") ||
               imgType.equals("image/jpg") ||
                imgType.equals("image/svg") ||
               imgType.equals("image/png") ||
               imgType.equals("image/webp");
    }
}
