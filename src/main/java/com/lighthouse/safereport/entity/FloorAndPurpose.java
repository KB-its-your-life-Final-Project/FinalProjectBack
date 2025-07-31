package com.lighthouse.safereport.entity;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "층수와 용도 정보")
public class FloorAndPurpose {
    @ApiModelProperty(value = "층수", example = "5층")
    private String resFloor;      // 층수
    
    @ApiModelProperty(value = "용도", example = "주거")
    private String resUseType;    // 용도
    
    @ApiModelProperty(value = "구조", example = "철근콘크리트")
    private String resStructure;  // 구조
    
    @ApiModelProperty(value = "면적", example = "84.5㎡")
    private String resArea;       // 면적
}
