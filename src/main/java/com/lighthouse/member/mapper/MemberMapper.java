package com.lighthouse.member.mapper;

import com.lighthouse.member.dto.ChangePasswordDTO;
import com.lighthouse.member.vo.MemberVO;
import com.lighthouse.security.vo.AuthVO;

import java.util.List;

public interface MemberMapper {
    MemberVO selectMemberByUsername(String username);

    List<MemberVO> selectMembers();

    MemberVO get(String username);

    MemberVO findByUsername(String username); // id 중복 체크시 사용

    int insert(MemberVO member); // 회원 정보 추가

    int insertAuth(AuthVO auth); // 회원 권한 정보 추가

    int update(MemberVO member);

    int updatePassword(ChangePasswordDTO changePasswordDTO);

}
