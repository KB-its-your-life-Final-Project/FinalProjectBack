package com.lighthouse.security.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtProcessor {
    static private final long ACCESS_TOKEN_VALID_MILLISECOND = 1000L * 60 * 10;             // 10분
    static private final long REFRESH_TOKEN_VALID_MILLISECOND = 1000L * 60 * 60 * 24 * 14;  // 2주

    @Value("${jwt.secret}")
    private String secretKey;
    private Key key;

    @PostConstruct
    public void initKey() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String subject, int createdType, long validTime) {
        return Jwts.builder()
                .setSubject(subject)
                .claim("createdType", createdType)  // 추가 데이터: 회원 가입 유형
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + validTime))
                .signWith(key)
                .compact();
    }

    public String generateAccessToken(String subject, int createdType) {
        return generateToken(subject, createdType, ACCESS_TOKEN_VALID_MILLISECOND);
    }

    public String generateRefreshToken(String subject, int createdType) {
        return generateToken(subject, createdType, REFRESH_TOKEN_VALID_MILLISECOND);
    }

    // JWT Subject(Member) 추출- 해석 불가인 경우 예외 발생
    // 예외 ExpiredJwtException, UnsupportedJwtException, MalformedJwtException, SignatureException, IllegalArgumentException
    public String getSubjectFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public int getCreatedTypeFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("createdType", Integer.class);
    }

    // JWT 검증(유효 기간 검증) - 해석 불가인 경우 예외 발생
    // refresh token 검증 시에만 사용
    public boolean validateToken(String token) {
        Jws<Claims> claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
        return true;
    }

    public boolean isExpired(String token) {
        try {
            Date expiration = Jwts.parser()
                    .setSigningKey(key)
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public Date getIssuedAt(String token) {
        return Jwts.parser()
                .setSigningKey(key)
                .parseClaimsJws(token)
                .getBody()
                .getIssuedAt();
    }

    public Date getExpiration(String token) {
        return Jwts.parser()
                .setSigningKey(key)
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
    }
}
