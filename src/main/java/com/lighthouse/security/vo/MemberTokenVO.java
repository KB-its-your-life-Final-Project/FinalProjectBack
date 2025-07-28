package com.lighthouse.security.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberTokenVO {
    private int id;
    private int memberId;
    private String refreshToken;
    private Date createdAt;
    private Date expiresAt;
}
