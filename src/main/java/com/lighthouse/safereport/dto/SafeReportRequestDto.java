package com.lighthouse.safereport.dto;

import lombok.Data;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@Data
@ApiModel(description = "안전 리포트 요청 데이터")
public class SafeReportRequestDto {
    @ApiModelProperty(value = "건물명", example = "아파트명")
    private String buildingName;
    
    @ApiModelProperty(value = "도로명 주소", example = "서울특별시 강남구 테헤란로 123")
    private String roadAddress;
    
    @ApiModelProperty(value = "지번 주소", example = "서울특별시 강남구 역삼동 123-45")
    private String jibunAddress;
    
    @ApiModelProperty(value = "동명", example = "101동")
    private String dongName;
    
    @ApiModelProperty(value = "위도", example = "37.5665")
    private double lat;
    
    @ApiModelProperty(value = "경도", example = "126.9780")
    private double lng;
    
    @ApiModelProperty(value = "예산 (만원)", example = "5000")
    private Integer budget;
}
