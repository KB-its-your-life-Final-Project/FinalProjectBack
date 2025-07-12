package com.lighthouse.member.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.lighthouse.security.vo.AuthVO;
import com.lighthouse.member.vo.MemberVO;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberDTO {
    String username;
    String email;
    List<String> roles;

    public static MemberDTO of(MemberVO member){
        return new MemberDTO(
                member.getUsername(),
                member.getEmail(),
                member.getAuthList().stream()
                        .map(AuthVO::getAuth)
                        .toList()
        );
    }
}
