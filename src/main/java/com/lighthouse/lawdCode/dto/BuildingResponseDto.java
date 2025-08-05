package com.lighthouse.lawdCode.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel(description = "건물 검색 결과")
public class BuildingResponseDto {
    @ApiModelProperty(value = "5자리 지역코드", example = "11110", notes = "검색한 지역의 5자리 코드")
    private String regionCode; // 5자리 지역코드
    
    @ApiModelProperty(value = "읍면동 이름", example = "목동", notes = "검색한 읍면동 이름")
    private String dongName; // 읍면동 이름
    
    @ApiModelProperty(value = "건물명 목록", example = "[\"목동아파트\", \"목동빌라\"]", notes = "해당 지역의 건물명 리스트")
    private List<String> buildingNames; // 건물명 목록
} 