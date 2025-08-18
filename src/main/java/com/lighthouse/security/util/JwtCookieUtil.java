package com.lighthouse.security.util;

import com.lighthouse.security.dto.TokenDTO;
import io.swagger.models.auth.In;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtCookieUtil {
    private final JwtUtil jwtUtil;
    @Value("${JWT_EXPIRATION:600000}") int cookieMaxAge;
    public TokenDTO setTokensToCookies(HttpServletResponse resp, int subject) {
        log.info("JwtCookieUtil.setTokensToCookies 실행  ======");
        
        // HttpServletResponse가 null인 경우 체크
        if (resp == null) {
            log.warn("HttpServletResponse가 null입니다. 토큰 발급은 완료되었지만 쿠키 설정을 건너뜁니다.");
            // 토큰은 발급하되 쿠키 설정은 건너뛰기
            String accessToken = jwtUtil.generateAccessToken(subject);
            String refreshToken = jwtUtil.generateRefreshToken(subject);
            
            // 발급 시간과 만료 시간 추출
            Date createdAt = jwtUtil.getIssuedAt(refreshToken);
            Date expiresAt = jwtUtil.getExpiration(refreshToken);
            
            return new TokenDTO(accessToken, refreshToken, createdAt, expiresAt);
        }
        
        // Access Token 생성
        String accessToken = jwtUtil.generateAccessToken(subject);
        // Refresh Token 생성
        String refreshToken = jwtUtil.generateRefreshToken(subject);

        // Access Token 쿠키 설정 (HttpOnly, 경로, 만료시간)
        Cookie accessTokenCookie = new Cookie("accessToken", accessToken);
        accessTokenCookie.setHttpOnly(true);
        // accessTokenCookie.setSecure(true);  // HTTPS 적용 시 true
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(cookieMaxAge/1000);
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
        log.info("JwtCookieUtil: 응답 객체 resp: " + resp);

        // 발급 시간과 만료 시간 추출
        Date createdAt = jwtUtil.getIssuedAt(refreshToken);
        Date expiresAt = jwtUtil.getExpiration(refreshToken);

        log.info("RefreshToken createdAt: {}", createdAt);
        log.info("RefreshToken expiresAt: {}", expiresAt);
        return new TokenDTO(accessToken, refreshToken, createdAt, expiresAt);
    }

    public String getAccessTokenFromRequest(HttpServletRequest req) {
        log.info("JwtCookieUtil.getAccessTokenFromRequest 실행  ======");
        if (req.getCookies() == null) return null;
        for (Cookie cookie : req.getCookies()) {
            if ("accessToken".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public String getRefreshTokenFromRequest(HttpServletRequest req) {
        log.info("JwtCookieUtil.getRefreshTokenFromRequest 실행 ======");
        if (req.getCookies() == null) return null;
        for (Cookie cookie : req.getCookies()) {
            if ("refreshToken".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public void clearTokensFromCookies(HttpServletResponse resp) {
        log.info("JwtCookieUtil.clearTokensFromCookies 실행  ======");
        
        // HttpServletResponse가 null인 경우 체크
        if (resp == null) {
            log.warn("HttpServletResponse가 null입니다. 쿠키 초기화를 건너뜁니다.");
            return;
        }

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

        log.info("JwtCookieUtil: 쿠키 초기화 완료");
    }
}