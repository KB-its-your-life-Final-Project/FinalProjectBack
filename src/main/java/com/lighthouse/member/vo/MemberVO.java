package com.lighthouse.member.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberVO {
    private int id;             // 아이디
    private String name;        // 이름
    private String email;       // 이메일
    private String pwd;         // 비밀번호
    private String kakaoUserId; // 카카오 식별자
    private String phone;       // 전화번호
    private int age;            // 나이
    private int role;           // 역할 (1:최고관리자,4:관리자,10:일반회원)
    private String profileImg;  // 프로필 경로
    private int createdType;    // 계정 종류 (1: 기본, 2: 카카오, 3: 구글)
    private Date regDate;       // 회원가입일
    private String regIp;       // 회원가입 시 IP
    private String recentIp;    // 최근 접속 IP
    private int isDisable;      // 활성화 여부
    private int isDelete;       // 삭제 여부
}