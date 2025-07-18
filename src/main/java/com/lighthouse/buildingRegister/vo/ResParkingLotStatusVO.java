package com.lighthouse.buildingRegister.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResParkingLotStatusVO {
    private Long registerId;
    private String resArea;       // 면적
    private String resNumber;     // 호수(매수)
    private String resType;       // 구분
}
