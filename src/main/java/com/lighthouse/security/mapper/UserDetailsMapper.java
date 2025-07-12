package com.lighthouse.security.mapper;

import com.lighthouse.member.vo.MemberVO;

public interface UserDetailsMapper {
    MemberVO get(String username);
}
