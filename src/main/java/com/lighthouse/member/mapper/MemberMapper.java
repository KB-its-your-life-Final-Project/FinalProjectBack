package com.lighthouse.member.mapper;

import com.lighthouse.member.entity.Member;

import java.util.List;

public interface MemberMapper {
    List<Member> findAllMembers(); // 모든 회원 조회

    Member findMemberById(int id); // 아이디로 회원 조회

    Member findDeletedMemberById(int id); // 아이디로 탈퇴회원 조회

    Member findMemberByEmail(String email); //  이메일로 회원 조회

    Member findMemberByKakaoId(String kakaoId); // 카카오 회원ID로 회원 조회

    Member findMemberByGoogleId(String googleId); // 구글 ID로 회원 조회

    Boolean existsByEmail(String email); // 이메일로 회원 존재 여부 확인

    Boolean existsByKakaoId(String kakaoId); // 카카오 회원ID로 회원 존재 여부 확인

    Boolean existsByGoogleId(String googleId); // 구글ID로 회원 존재 여부 확인

    int insertMember(Member member); // 회원 정보 추가

    int updateMember(Member member); // 회원 정보 수정
}