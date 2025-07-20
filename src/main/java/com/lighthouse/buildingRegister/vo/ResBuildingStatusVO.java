package com.lighthouse.buildingRegister.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResBuildingStatusVO {
    private Long registerId;
    private String resType;       // 구분
    private String resFloor;      // 층
    private String resStructure;  // 구조
    private String resUseType;    // 용도
    private String resArea;       // 면적
}
