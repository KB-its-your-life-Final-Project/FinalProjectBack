package com.lighthouse.security.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.lighthouse.member.vo.MemberVO;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDTO {
    String username;
    public static UserInfoDTO of(MemberVO member) {
        return new UserInfoDTO(
                member.getEmail()
        );
    }
}