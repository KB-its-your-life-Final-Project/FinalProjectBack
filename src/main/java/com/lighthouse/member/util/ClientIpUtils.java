package com.lighthouse.member.util;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;

@Slf4j
public class ClientIpUtils {
    // 사용자 IP 주소 추출 함수
    public static String getClientIp(HttpServletRequest req) {
        log.info("ClientIpUtils.getClientIp 실행  ======");
        log.info("받은 request 객체: " + req);

        // Proxy / Load Balancer를 통해 전달된 경우
        String ip = req.getHeader("X-Forwarded-For");
        log.info("X-Forwarded-For: {}", ip);
        if (isValidIp(ip)) {
            return ip.split(",")[0].trim();
        }

        // 일부 Proxy 서버의 헤더
        ip = req.getHeader("Proxy-Client-IP");
        log.info("Proxy-Client-IP: {}", ip);
        if (isValidIp(ip)) { return ip; }

        // WebLogic 같은 WAS 환경의 헤더
        ip = req.getHeader("WL-Proxy-Client-IP");
        log.info("WL-Proxy-Client-IP: {}", ip);
        if (isValidIp(ip)) { return ip; }

        // 위 헤더들이 없을 경우 기본 remote 주소에서 추출
        ip = req.getRemoteAddr();
        log.info("RemoteAddr: {}", ip);
        return ip;
    }

    // IP 유효성 검사 함수
    private static boolean isValidIp (String ip) {
        return ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip);
    }
}
