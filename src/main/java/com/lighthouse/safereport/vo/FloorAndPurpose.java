package com.lighthouse.safereport.vo;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FloorAndPurpose {
    private String resFloor;      // 층수
    private String resUserType;    // 용도
    private String resStructure;  // 구조
    private String resArea;       // 면적
}
