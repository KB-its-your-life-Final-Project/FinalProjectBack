package com.lighthouse.security.mapper;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public interface MemberTokenMapper {

    int insertRefreshToken(MemberTokenVO memberTokenVO);

    int updateRefreshToken(MemberTokenVO memberTokenVO);

    MemberTokenVO findRefreshTokenByMemberId(int memberId);
}