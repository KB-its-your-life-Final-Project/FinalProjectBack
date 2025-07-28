package com.lighthouse.member.mapper;

import java.util.List;
import com.lighthouse.member.entity.Member;

public interface MemberMapper {
    List<Member> findAllMembers(); // 모든 사용자 조회

    Member findMemberById(int id); // 아이디로 사용자 조회

    Member findMemberByEmail(String email); //  이메일로 사용자 조회

    Member findMemberByKakaoId(String kakaoId); // 카카오계정으로 사용자 조회

    Member findMemberByGoogleId(String googleId); // 구글계정으로 사용자 조회

    int insertMember(Member member); // 사용자 정보 추가

    int updateMember(Member member); // 사용자 정보 수정
}
