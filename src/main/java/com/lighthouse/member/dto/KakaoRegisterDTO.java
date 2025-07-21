package com.lighthouse.member.dto;

import com.lighthouse.member.vo.MemberVO;
import com.lighthouse.member.service.external.KakaoUserClient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KakaoRegisterDTO {
    private String code;

    public MemberVO toVO() {
        return MemberVO.builder()
                .email("")
                .pwd("")
                .phone("")
                .age(0)
                .role(10)
                .createdType(2) // 1: 이메일, 2: 카카오, 3: 구글
                .build();
    }
}