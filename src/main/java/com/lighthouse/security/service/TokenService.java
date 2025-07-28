package com.lighthouse.security.service;

import com.lighthouse.security.dto.TokenDTO;
import com.lighthouse.security.mapper.MemberTokenMapper;
import com.lighthouse.security.vo.MemberTokenVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.reflect.Member;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {
    private final MemberTokenMapper memberTokenMapper;

    // Refresh Token DB 저장
    public void saveRefreshToken (int memberId, TokenDTO tokenDto) {
        MemberTokenVO memberTokenVo = new MemberTokenVO();
        memberTokenVo.setMemberId(memberId);
        memberTokenVo.setRefreshToken(tokenDto.getRefreshToken());
        memberTokenVo.setCreatedAt(tokenDto.getRefreshTokenCreatedAt());
        memberTokenVo.setExpiresAt(tokenDto.getRefreshTokenExpiresAt());

        int updated = memberTokenMapper.updateRefreshToken(memberTokenVo);
        if (updated == 0) {
            memberTokenMapper.insertRefreshToken(memberTokenVo);
            log.info("RefreshToken 새로 insert 완료");
        } else {
            log.info("RefreshToken update 완료");
        }
    }

    // Refresh Token 검증 (DB 값과 비교)
    public boolean isRefreshTokenValid (int memberId, String refreshToken) {
        MemberTokenVO memberTokenVo = memberTokenMapper.findRefreshTokenByMemberId(memberId);
        String savedRefreshToken = memberTokenVo.getRefreshToken();
        return refreshToken.equals(savedRefreshToken);
    }
}
