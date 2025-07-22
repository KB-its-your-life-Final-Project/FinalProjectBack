package com.lighthouse.buildingRegister.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResLicenseClassVO {
    private Long registerId;
    private String resLicenseNo;  // 면허번호
    private String resType;       // 구분
    private String resUserNm;     // 성명
}