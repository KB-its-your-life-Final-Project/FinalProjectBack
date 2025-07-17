package com.lighthouse.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.lighthouse.member.vo.MemberVO;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberDTO {
    private String email;
    private Date regDate;

    public static MemberDTO of(MemberVO member){
        return MemberDTO.builder()
                .email(member.getEmail())
                .regDate(member.getRegDate())
                .build();
    }
}
