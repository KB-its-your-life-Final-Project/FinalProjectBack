package com.lighthouse.safereport.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel(description = "거래금액, 건축연도, 역전세율 정보")
public class RentalRatioAndBuildyear {
    @ApiModelProperty(value = "거래가 (만원)", example = "50000")
    private int dealAmount;//거래가 ( 전세가율 점수 계산 목적 )
    
    @ApiModelProperty(value = "건축연도", example = "2010")
    private int buildYear;//건축연도 ( 연식 점수 계산 목적 )
    
    @ApiModelProperty(value = "역전세율 (%)", example = "85.5")
    private double reverseRentalRatio; //역전세율
    
    @ApiModelProperty(value = "연식률 점수", example = "2")
    private int buildYearScore; //연식률 점수
    
    @ApiModelProperty(value = "최종 점수", example = "5")
    private int score; //최종 점수 = 전세가율점수 + 연식점수
}
