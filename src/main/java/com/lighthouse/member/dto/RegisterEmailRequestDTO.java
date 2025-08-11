package com.lighthouse.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.lighthouse.member.entity.Member;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterEmailRequestDTO {
    private String email;
//    private String verificationCode;
    private String name;
    private String pwd;
    private String pwdChk;

    public Member toMember() {
        return Member.builder()
                .name(name)
                .email(email)
                .pwd(pwd)
                .kakaoId("")
                .googleId("")
                .phone("")
                .age(0)
                .role(10)
                .createdType(1) // 1: 기본 2: 카카오 3: 구글
                .build();
    }
}