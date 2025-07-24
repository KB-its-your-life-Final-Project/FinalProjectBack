package com.lighthouse.member.mapper;

import com.lighthouse.member.vo.MemberVO;

import java.util.List;

public interface MemberMapper {
    List<MemberVO> findAllMembers(); // 모든 사용자 조회

    MemberVO findMemberById(int id); // 아이디로 사용자 조회

    MemberVO findMemberByEmail(String email); //  이메일로 사용자 조회

    MemberVO findMemberByKakaoId(String kakaoId); // 카카오계정으로 사용자 조회

    MemberVO findMemberByGoogleId(String googleId); // 구글계정으로 사용자 조회

    int insertMember(MemberVO member); // 사용자 정보 추가

    int updateMember(MemberVO member); // 사용자 정보 수정
}
