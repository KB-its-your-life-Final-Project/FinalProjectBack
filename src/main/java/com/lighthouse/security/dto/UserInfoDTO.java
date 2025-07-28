package com.lighthouse.security.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import com.lighthouse.member.entity.Member;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDTO {
    String email;
    public static UserInfoDTO of(Member member) {
        return new UserInfoDTO(
                member.getEmail()
        );
    }
}