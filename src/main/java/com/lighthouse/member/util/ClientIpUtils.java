package com.lighthouse.member.util;

import javax.servlet.http.HttpServletRequest;

public class ClientIpUtils {
    // 사용자 IP 주소 추출 함수
    public static String getClientIp(HttpServletRequest req) {
        // Proxy / Load Balancer를 통해 전달된 경우
        String ip = req.getHeader("X-Forwarded-For");
        if (isValidIp(ip)) {
            return ip.split(",")[0].trim();
        }

        // 일부 Proxy 서버의 헤더
        ip = req.getHeader("Proxy-Client-IP");
        if (isValidIp(ip)) { return ip; }

        // WebLogic 같은 WAS 환경의 헤더
        ip = req.getHeader("WL-Proxy-Client-IP");
        if (isValidIp(ip)) { return ip; }

        // 위 헤더들이 없을 경우 기본 remote 주소에서 추출
        return req.getRemoteAddr();
    }

    // IP 유효성 검사 함수
    private static boolean isValidIp (String ip) {
        return ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip);
    }
}
