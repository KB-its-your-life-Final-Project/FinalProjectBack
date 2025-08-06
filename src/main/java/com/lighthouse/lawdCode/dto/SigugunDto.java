package com.lighthouse.lawdCode.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "시/군/구 정보")
public class SigugunDto {
    @ApiModelProperty(value = "시/군/구 코드", example = "110", notes = "시/군/구의 고유 코드 (예: 종로구=110, 중구=140)")
    private String sggCd; // 시군구 코드
    
    @ApiModelProperty(value = "시/군/구 이름", example = "종로구", notes = "시/군/구의 한글 이름")
    private String sggNm; // 시군구 이름
    
    @ApiModelProperty(value = "시/도 코드", example = "11", notes = "상위 시/도 코드 (예: 서울=11)")
    private String sidoCd; // 시도 코드
}
