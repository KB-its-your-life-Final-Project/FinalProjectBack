package org.lighthouse.security.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.lighthouse.member.dto.MemberDTO;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResultDTO {
    String token;
    MemberDTO user;
}
