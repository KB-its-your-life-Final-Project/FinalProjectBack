package com.lighthouse.member.dto;

import org.springframework.security.crypto.password.PasswordEncoder;
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

    public MemberVO toVO() {
        return MemberVO.builder()
                .name(name)
                .email(email)
                .pwd(password)
                .kakaoUserId("")
                .phone("")
                .age(0)
                .role(10)
                .createdType(1) // 1: 기본 2: 카카오 3: 구글
                .build();
    }
}