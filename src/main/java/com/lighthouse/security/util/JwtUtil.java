package com.lighthouse.security.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
    static private final long ACCESS_TOKEN_VALID_MILLISECOND = 1000L * 60 * 10;             // 10분
    static private final long REFRESH_TOKEN_VALID_MILLISECOND = 1000L * 60 * 60 * 24 * 14;  // 2주

    @Value("${JWT_SECRET}")
    private String secretKey;
    private Key key;

    @PostConstruct
    public void initKey() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(int subject, long validTime) {
        return Jwts.builder()
                .setSubject(String.valueOf(subject))
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + validTime))
                .signWith(key)
                .compact();
    }

    public String generateAccessToken(int subject) {
        return generateToken(subject, ACCESS_TOKEN_VALID_MILLISECOND);
    }

    public String generateRefreshToken(int subject) {
        return generateToken(subject, REFRESH_TOKEN_VALID_MILLISECOND);
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
