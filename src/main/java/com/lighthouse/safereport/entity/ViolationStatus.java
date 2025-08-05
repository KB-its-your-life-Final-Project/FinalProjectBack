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
@ApiModel(description = "위반 여부 정보")
public class ViolationStatus {
    @ApiModelProperty(value = "위반 여부", example = "위반건축물", notes = "위반건축물일 경우 '위반건축물'로 표시")
    private String violationStatus;  // 위반건축물일 경우 "위반건축물"
} 