package com.lighthouse.member.mapper;

import com.lighthouse.member.dto.ChangePasswordDTO;
import com.lighthouse.member.vo.MemberVO;
import com.lighthouse.security.vo.AuthVO;

import java.util.List;

public interface MemberMapper {
    List<MemberVO> selectMembers(); // 모든 사용자 조회

    MemberVO selectMemberByUsername(String username); // 아이디로 사용자 조회

    MemberVO findByUsername(String username); // 아이디 중복 확인시 사용

    int insertUser(MemberVO member); // 사용자 정보 추가

    int insertAuth(AuthVO auth); // 사용자 권한 정보 추가

    int updateMember(MemberVO member); // 사용자 정보 수정

    int updatePassword(ChangePasswordDTO changePasswordDTO); // 비밀번호 수정
}
