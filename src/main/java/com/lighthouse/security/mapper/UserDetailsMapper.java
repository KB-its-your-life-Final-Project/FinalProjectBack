package com.lighthouse.security.mapper;

import com.lighthouse.member.vo.MemberVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserDetailsMapper {
    MemberVO get(String username);
}
