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
public class RegisterKakaoDTO {
    private String code;

    public Member toVO() {
        return Member.builder()
                .email("")
                .pwd("")
                .googleId("")
                .phone("")
                .age(0)
                .role(10)
                .createdType(2) // 1: 이메일, 2: 카카오, 3: 구글
                .build();
    }
}