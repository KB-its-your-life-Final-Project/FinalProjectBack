package com.lighthouse.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.lighthouse.member.vo.MemberVO;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailRegisterDTO {
    private String email;
    private String name;
    private String password;
    private int createdType;

    public MemberVO toVO() {
        return MemberVO.builder()
                .email(email)
                .name(name)
                .pwd(password)
                .createdType(1) // 1: 기본 2: 카카오 3: 구글
                .build();
    }
}