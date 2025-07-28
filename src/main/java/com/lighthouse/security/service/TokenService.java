package com.lighthouse.security.service;

import com.lighthouse.security.dto.TokenDTO;
import com.lighthouse.security.mapper.MemberTokenMapper;
import com.lighthouse.security.entity.MemberToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {
    private final MemberTokenMapper memberTokenMapper;

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

    // Refresh Token 검증 (DB 값과 비교)
    public boolean isRefreshTokenValid (int memberId, String refreshToken) {
        MemberToken memberToken = memberTokenMapper.findRefreshTokenByMemberId(memberId);
        String savedRefreshToken = memberToken.getRefreshToken();
        return refreshToken.equals(savedRefreshToken);
    }
}
