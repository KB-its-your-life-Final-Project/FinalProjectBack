package com.lighthouse.member.mapper;

import com.lighthouse.member.dto.MemberDTO;
import com.lighthouse.member.vo.MemberVO;

import java.util.List;

public interface MemberMapper {
    MemberVO findById(Number id);

    List<MemberVO> findAll();
}
