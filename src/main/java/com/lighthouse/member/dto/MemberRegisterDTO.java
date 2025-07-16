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
public class MemberRegisterDTO {
    private String username;
    private String password;

    public MemberVO toVO() {
        return MemberVO.builder()
                .username(username)
                .password(password)
                .build();
    }
}