package org.lighthouse.member.mapper;

import org.lighthouse.member.vo.MemberVO;

import java.util.List;

public interface MemberMapper {
    MemberVO selectMemberByUsername(String username);

    List<MemberVO> selectMembers();
}
