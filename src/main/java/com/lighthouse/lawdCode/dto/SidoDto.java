package com.lighthouse.lawdCode.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "시/도 정보")
public class SidoDto {
    @ApiModelProperty(value = "시/도 코드", example = "11", notes = "서울=11, 부산=26, 대구=27, 인천=28, 광주=29, 대전=30, 울산=31, 세종=36, 경기=41, 강원=42, 충북=43, 충남=44, 전북=45, 전남=46, 경북=47, 경남=48, 제주=50")
    private String sidoCd; // 시도 코드
    
    @ApiModelProperty(value = "시/도 이름", example = "서울", notes = "시/도의 한글 이름")
    private String sidoNm; // 시도 이름
}
