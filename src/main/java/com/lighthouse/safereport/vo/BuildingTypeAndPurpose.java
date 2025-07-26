package com.lighthouse.safereport.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BuildingTypeAndPurpose {
    private String violationStatus; // 위반 여부
    private String buildingPurpose; // 건물 용도
}
