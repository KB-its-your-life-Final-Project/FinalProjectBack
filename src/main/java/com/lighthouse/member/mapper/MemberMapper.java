package com.lighthouse.member.mapper;

import com.lighthouse.member.vo.MemberVO;

import java.util.List;

public interface MemberMapper {
    MemberVO selectMemberByUsername(String username);

    List<MemberVO> findAll();
}
