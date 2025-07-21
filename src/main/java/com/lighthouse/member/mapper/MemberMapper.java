package com.lighthouse.member.mapper;

import com.lighthouse.member.dto.ChangePasswordDTO;
import com.lighthouse.member.vo.MemberVO;

import java.util.List;

public interface MemberMapper {
    List<MemberVO> selectMembers(); // 모든 사용자 조회

    MemberVO selectMemberById(int id); // 아이디로 사용자 조회

    MemberVO selectMemberByEmail(String email); //  이메일로 사용자 조회

    MemberVO selectMemberByKakaoUserId(String kakaoUserId); // 카카오계정으로 사용자 조회

    int insertUser(MemberVO member); // 사용자 정보 추가

    int updateUser(MemberVO member); // 사용자 정보 수정

    int updatePassword(ChangePasswordDTO changePasswordDTO); // 비밀번호 수정
}
