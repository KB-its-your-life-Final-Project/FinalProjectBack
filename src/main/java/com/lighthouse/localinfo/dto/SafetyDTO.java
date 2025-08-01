package com.lighthouse.localinfo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SafetyDTO {
    private String regionCd;
    private String regionName;
    private String locataddNm;
    private Long totalSafetyBellCount;
}