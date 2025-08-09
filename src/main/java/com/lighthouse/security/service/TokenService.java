package com.lighthouse.security.service;

import com.lighthouse.security.dto.TokenDTO;
import com.lighthouse.security.mapper.MemberTokenMapper;
import com.lighthouse.security.entity.MemberToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.lighthouse.security.util.JwtUtil;

import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {
    private final MemberTokenMapper memberTokenMapper;
    private final JwtUtil jwtUtil;

    // Access Token 검증 (만료 여부 확인)
    public boolean isAccessTokenValid (String accessToken) {
        Date expiresAt = jwtUtil.getExpiration(accessToken);
        boolean isExpired = expiresAt.before(new Date());
        return !isExpired;
    }
    // Refresh Token 검증 (만료 여부 확인, DB 값과 일치 여부 확인)
    public boolean isRefreshTokenValid (int memberId, String refreshToken) {
        // 만료 여부 확인
        Date expiresAt = jwtUtil.getExpiration(refreshToken);
        boolean isExpired = expiresAt.before(new Date());
        if (isExpired) {
            return false;
        }
        // DB 값과 일치 여부 확인
        MemberToken memberToken = memberTokenMapper.findRefreshTokenByMemberId(memberId);
        String savedRefreshToken = memberToken.getRefreshToken();
        return refreshToken.equals(savedRefreshToken);
    }

    // Refresh Token DB 저장
    public void saveRefreshToken (int memberId, TokenDTO tokenDto) {
        MemberToken memberToken = new MemberToken();
        memberToken.setMemberId(memberId);
        memberToken.setRefreshToken(tokenDto.getRefreshToken());
        memberToken.setCreatedAt(tokenDto.getRefreshTokenCreatedAt());
        memberToken.setExpiresAt(tokenDto.getRefreshTokenExpiresAt());

        int updated = memberTokenMapper.updateRefreshToken(memberToken);
        if (updated == 0) {
            memberTokenMapper.insertRefreshToken(memberToken);
            log.info("RefreshToken 새로 insert 완료");
        } else {
            log.info("RefreshToken update 완료");
        }
    }
}
