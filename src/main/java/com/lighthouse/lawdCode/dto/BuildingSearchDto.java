package com.lighthouse.lawdCode.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "건물 검색 요청 정보")
public class BuildingSearchDto {
    @ApiModelProperty(value = "5자리 지역코드 (sido_cd + sgg_cd)", example = "11110", notes = "시도코드(2자리) + 시군구코드(3자리)")
    private String regionCode; // 5자리 지역코드
    
    @ApiModelProperty(value = "읍면동 한글 이름", example = "목동", notes = "검색할 읍면동의 한글 이름")
    private String dongName; // 읍면동 이름
} 