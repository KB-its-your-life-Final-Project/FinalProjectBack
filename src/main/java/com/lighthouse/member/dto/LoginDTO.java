package com.lighthouse.member.dto;

import com.lighthouse.member.vo.MemberVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginDTO {
    private String email;
    private String password;
    private String code;
    private int createdType;

    public MemberVO toVO() {
        return MemberVO.builder()
                .createdType(createdType) // 1: 기본 2: 카카오 3: 구글
                .build();
    }
}