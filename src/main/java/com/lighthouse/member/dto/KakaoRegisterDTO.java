package com.lighthouse.member.dto;

import com.lighthouse.member.vo.MemberVO;
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
    private String name;

    public MemberVO toVO() {
        return MemberVO.builder()
                .name(name != null ? name : "KakaoUser")
                .pwd("")
                .createdType(2) // 1: 이메일, 2: 카카오, 3: 구글
                .build();
    }
}