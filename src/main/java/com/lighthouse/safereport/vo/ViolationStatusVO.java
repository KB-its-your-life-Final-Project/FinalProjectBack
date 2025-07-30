package com.lighthouse.safereport.vo;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViolationStatusVO {
    private String violationStatus;  // 위반건축물일 경우 "위반건축물"
} 