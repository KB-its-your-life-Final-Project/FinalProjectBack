package com.lighthouse.lawdCode.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel(description = "건물 정보")
public class BuildingInfoDto {
    @ApiModelProperty(value = "건물명", example = "목동아파트", notes = "건물의 이름")
    private String buildingName; // 건물명
    
    @ApiModelProperty(value = "위도", example = "37.5665", notes = "건물의 위도 좌표")
    private Double latitude; // 위도
    
    @ApiModelProperty(value = "경도", example = "126.9780", notes = "건물의 경도 좌표")
    private Double longitude; // 경도
    
    @ApiModelProperty(value = "지번 주소", example = "역삼동 123-45", notes = "건물의 지번 주소")
    private String jibunAddr; // 지번 주소
} 