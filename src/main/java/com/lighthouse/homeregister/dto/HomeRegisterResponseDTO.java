package com.lighthouse.homeregister.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@ApiModel(description = "나의 집 저장 응답 데이터")
public class HomeRegisterResponseDTO {
    @ApiModelProperty(value = "부동산 ID", example = "12345")
    private Integer estateId;
    
    @ApiModelProperty(value = "건물명", example = "강남아파트")
    private String buildingName;

    @ApiModelProperty(value = "건물동명", example = "101동")
    private String buildingNumber;
    
    @ApiModelProperty(value = "처리 타입", example = "UPDATE", notes = "UPDATE: 수정, INSERT: 신규 등록")
    private String actionType;
    
    @ApiModelProperty(value = "계약 시작일", example = "2023.04.23")
    private String contractStart;
    
    @ApiModelProperty(value = "계약 종료일", example = "2025.04.23")
    private String contractEnd;
    
    @ApiModelProperty(value = "거래 유형", example = "1", notes = "1: 전세, 2: 월세")
    private Integer rentType;
    
    @ApiModelProperty(value = "전세 금액", example = "200")
    private Integer jeonseAmount;
    
    @ApiModelProperty(value = "월세 보증금", example = "300")
    private Integer monthlyDeposit;
    
    @ApiModelProperty(value = "월세 금액", example = "50")
    private Integer monthlyRent;
    
    @ApiModelProperty(value = "등록일", example = "2023-04-23T10:30:00")
    private String regDate;
    
    @ApiModelProperty(value = "법정동명", example = "역삼동")
    private String umdNm;
    
    @ApiModelProperty(value = "지번주소", example = "서울특별시 강남구 역삼동 123-45")
    private String jibunAddr;
    
    @ApiModelProperty(value = "위도", example = "37.5665")
    private Double latitude;
    
    @ApiModelProperty(value = "경도", example = "126.9780")
    private Double longitude;
    
    @ApiModelProperty(value = "도로명주소", example = "서울특별시 강남구 테헤란로 123")
    private String roadAddress;
}
