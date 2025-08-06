package com.lighthouse.lawdCode.dto;

import com.lighthouse.estate.dto.BuildingInfoDto;
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
    
    @ApiModelProperty(value = "건물 정보 목록", example = "[{\"buildingName\": \"목동아파트\", \"latitude\": 37.5665, \"longitude\": 126.9780}]", notes = "해당 지역의 건물 정보 리스트")
    private List<BuildingInfoDto> buildingInfos; // 건물 정보 목록
} 