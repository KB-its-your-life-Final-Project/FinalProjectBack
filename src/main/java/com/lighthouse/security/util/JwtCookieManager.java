package com.lighthouse.security.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtCookieManager {
    private final JwtProcessor jwtProcessor;

    public void setTokensToCookies(HttpServletResponse resp, String subject) {
        log.info("JwtCookieManager.setTokensToCookies 실행  ======");

        // Access Token, Refresh Token 생성
        String accessToken = jwtProcessor.generateAccessToken(subject);
        String refreshToken = jwtProcessor.generateRefreshToken(subject);
        log.info("JwtCookieManager: accessToken 발급: " + accessToken);
        log.info("JwtCookieManager: refreshToken 발급: " + refreshToken);

        // Access Token 쿠키 설정 (HttpOnly, 경로, 만료시간)
        Cookie accessTokenCookie = new Cookie("accessToken", accessToken);
        accessTokenCookie.setHttpOnly(true);
        // accessTokenCookie.setSecure(true);  // HTTPS 적용 시 true
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(60 * 10); // 10분
        log.info("[accessToken 쿠키]");
        log.info(" - Name: {}", accessTokenCookie.getName());
        log.info(" - Value: {}", accessTokenCookie.getValue());
        log.info(" - HttpOnly: {}", accessTokenCookie.isHttpOnly());
//        log.info(" - Secure: {}", accessTokenCookie.getSecure());
        log.info(" - Path: {}", accessTokenCookie.getPath());
        log.info(" - MaxAge: {}", accessTokenCookie.getMaxAge());

        // Refresh Token 쿠키 설정 (HttpOnly, 경로, 만료시간)
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        // refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(60 * 60 * 24 * 14); // 2주
        log.info("[refreshToken 쿠키]");
        log.info(" - Name: {}", refreshTokenCookie.getName());
        log.info(" - Value: {}", refreshTokenCookie.getValue());
        log.info(" - HttpOnly: {}", refreshTokenCookie.isHttpOnly());
//        log.info(" - Secure: {}", refreshTokenCookie.getSecure());
        log.info(" - Path: {}", refreshTokenCookie.getPath());
        log.info(" - MaxAge: {}", refreshTokenCookie.getMaxAge());

        // 쿠키를 응답에 추가
        resp.addCookie(accessTokenCookie);
        resp.addCookie(refreshTokenCookie);

        log.info("JwtCookieManager: 응답 객체 resp: " + resp);
    }

    public void clearTokensFromCookies (HttpServletResponse resp) {
        log.info("JwtCookieManager.clearTokensFromCookies 실행  ======");

        // Access Token 쿠키 설정 (HttpOnly, 경로, 만료시간)
        Cookie accessTokenCookie = new Cookie("accessToken", "");
        accessTokenCookie.setHttpOnly(true);
        // accessTokenCookie.setSecure(true);  // HTTPS 적용 시 true
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(0); // 즉시 삭제
        log.info("[accessToken 쿠키]");
        log.info(" - Name: {}", accessTokenCookie.getName());
        log.info(" - Value: {}", accessTokenCookie.getValue());
        log.info(" - HttpOnly: {}", accessTokenCookie.isHttpOnly());
//        log.info(" - Secure: {}", accessTokenCookie.getSecure());
        log.info(" - Path: {}", accessTokenCookie.getPath());
        log.info(" - MaxAge: {}", accessTokenCookie.getMaxAge());

        // Refresh Token 쿠키 설정 (HttpOnly, 경로, 만료시간)
        Cookie refreshTokenCookie = new Cookie("refreshToken", "");
        refreshTokenCookie.setHttpOnly(true);
        // refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0); // 즉시 삭제
        log.info("[refreshToken 쿠키]");
        log.info(" - Name: {}", refreshTokenCookie.getName());
        log.info(" - Value: {}", refreshTokenCookie.getValue());
        log.info(" - HttpOnly: {}", refreshTokenCookie.isHttpOnly());
//        log.info(" - Secure: {}", refreshTokenCookie.getSecure());
        log.info(" - Path: {}", refreshTokenCookie.getPath());
        log.info(" - MaxAge: {}", refreshTokenCookie.getMaxAge());

        // 쿠키를 응답에 추가
        resp.addCookie(accessTokenCookie);
        resp.addCookie(refreshTokenCookie);

        log.info("JwtCookieManager: 쿠키 초    기화 완료");
    }
}