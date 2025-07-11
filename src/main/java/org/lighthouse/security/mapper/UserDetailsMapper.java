package org.lighthouse.security.mapper;

import org.lighthouse.member.vo.MemberVO;

public interface UserDetailsMapper {
    MemberVO get(String username);
}
