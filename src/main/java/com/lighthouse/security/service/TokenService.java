package com.lighthouse.security.service;

import com.lighthouse.security.mapper.MemberTokenMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {
    private final MemberTokenMapper memberTokenMapper;

    // Refresh Token DB 저장
    public void saveRefreshToken (String memberId, String refreshToken) {
        int updated = memberTokenMapper.updateRefreshToken(memberId, refreshToken);
        if (updated == 0) {
            memberTokenMapper.insertRefreshToken(memberId, refreshToken);
            log.info("RefreshToken 새로 insert 완료");
        } else {
            log.info("RefreshToekn update 완료");
        }
    }

    // Refresh Token 검증 (DB 값과 비교)
    public boolean isRefreshTokenValid (int memberId, String refreshToken) {
        String savedRefreshToken = memberTokenMapper.findRefreshTokenByMemberId(memberId);
        return refreshToken.equals(savedRefreshToken);
    }
}
