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
public class LoginRequestDTO {
    private String email;
    private String pwd;
    private String code;
    private int createdType;

    public Member toMember() {
        return Member.builder()
                .createdType(createdType) // 1: 기본 2: 카카오 3: 구글
                .build();
    }
}