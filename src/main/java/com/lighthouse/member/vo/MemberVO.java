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
    private int id;
    private String name;
    private String email;
    private String pwd;
    private String kakaoUserId;
    private int phoneNum;
    private int age;
    private String profileImg;
    private int createdType;
    private Date regDate;
    private String regIp;
    private String recentIp;
    private int isDisable;
    private int isDelete;
}