package com.lighthouse.security.mapper;

import com.lighthouse.security.entity.MemberToken;

public interface MemberTokenMapper {

    int insertRefreshToken(MemberToken memberToken);

    int updateRefreshToken(MemberToken memberToken);

    MemberToken findRefreshTokenByMemberId(int memberId);
}