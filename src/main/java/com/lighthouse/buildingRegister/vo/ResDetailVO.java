package com.lighthouse.buildingRegister.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResDetailVO {
    private Long registerId;
    private String resType;       // 구분
    private String resContents;   // 내용
}