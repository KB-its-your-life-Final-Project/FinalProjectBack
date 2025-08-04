package com.lighthouse.member.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeRequestDTO {
    private String name;
    private String pwd;
    private int changeType; // 1: name, 2: pwd
}
