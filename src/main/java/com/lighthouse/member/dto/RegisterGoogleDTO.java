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
public class RegisterGoogleDTO {
      private String code;

      public MemberVO toVO() {
          return MemberVO.builder()
                  .pwd("")
                  .kakaoId("")
                  .phone("")
                  .age(0)
                  .role(10)
                  .createdType(3) // 1: 이메일, 2: 카카오, 3: 구글
                  .build();
      }
}