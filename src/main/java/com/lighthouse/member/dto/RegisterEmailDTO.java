package com.lighthouse.member.dto;

import com.lighthouse.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterEmailDTO {
    private String email;
    private String verificationCode;
    private String name;
    private String password;

    public Member toVO() {
        return Member.builder()
                .name(name)
                .email(email)
                .pwd(password)
                .kakaoId("")
                .googleId("")
                .phone("")
                .age(0)
                .role(10)
                .createdType(1) // 1: 기본 2: 카카오 3: 구글
                .build();
    }
}