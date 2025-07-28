package com.lighthouse.security.mapper;

import org.apache.ibatis.annotations.Mapper;
import com.lighthouse.member.entity.Member;

@Mapper
public interface UserDetailsMapper {
    Member get(String email);
}
