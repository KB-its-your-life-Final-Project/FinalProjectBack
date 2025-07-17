package com.lighthouse.member.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordDTO {
    int id;       // 아이디
    String oldPassword;    // 이전 비밀번호
    String newPassword;    // 새 비밀번호
}