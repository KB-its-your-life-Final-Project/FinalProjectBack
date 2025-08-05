package com.lighthouse.homeregister.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "나의 집 저장 요청 데이터")
public class HomeRegisterRequestDTO {
    @ApiModelProperty(value = "위도", example = "37.5665")
    private double lat;

    @ApiModelProperty(value = "경도", example = "126.9780")
    private double lng;

    @ApiModelProperty(value = "건물동 이름", example = "101동")
    private String buildingNumber;

    @ApiModelProperty(value ="계약 시작일", example="2023.04.23")
    private String contractStart;

    @ApiModelProperty(value ="계약 종료일", example="2025.04.23")
    private String contractEnd;

    @ApiModelProperty(value="거래 유형", example= "1")
    private Integer rentType;

    @ApiModelProperty(value="전세 금액", example= "200")
    private Integer jeonseAmount;

    @ApiModelProperty(value="월세 보증금", example= "300")
    private Integer monthlyDeposit;

    @ApiModelProperty(value="월세 금액", example= "50")
    private Integer monthlyRent;
}
