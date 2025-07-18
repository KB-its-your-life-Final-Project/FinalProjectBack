package com.lighthouse.security.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Component
@RequiredArgsConstructor
public class JwtCookieManager {
    private final JwtProcessor jwtProcessor;

    public void setTokensToCookies(HttpServletResponse resp, String subject) {
        // Access Token, Refresh Token 생성
        String accessToken = jwtProcessor.generateAccessToken(subject);
        String refreshToken = jwtProcessor.generateRefreshToken(subject);

        // Access Token 쿠키 설정 (HttpOnly, 경로, 만료시간)
        Cookie accessTokenCookie = new Cookie("accessToken", accessToken);
        accessTokenCookie.setHttpOnly(true);
        // accessTokenCookie.setSecure(true);  // HTTPS 적용 시 true
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(60 * 10); // 10분

        // Refresh Token 쿠키 설정 (HttpOnly, 경로, 만료시간)
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        // refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(60 * 60 * 24 * 14); // 2주

        // 쿠키를 응답에 추가
        resp.addCookie(accessTokenCookie);
        resp.addCookie(refreshTokenCookie);
    }
}
