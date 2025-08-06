package com.lighthouse.lawdCode.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "읍/면/동 정보")
public class DongDto {
    @ApiModelProperty(value = "읍/면/동 코드", example = "149", notes = "읍/면/동의 고유 코드")
    private String umdCd; // 읍면동 코드
    
    @ApiModelProperty(value = "읍/면/동 이름", example = "원서동", notes = "읍/면/동의 한글 이름")
    private String dongNm; // 읍면동 이름
    
    @ApiModelProperty(value = "시/군/구 코드", example = "110", notes = "상위 시/군/구 코드 (예: 종로구=110)")
    private String sggCd; // 시군구 코드
    
    @ApiModelProperty(value = "시/도 코드", example = "11", notes = "상위 시/도 코드 (예: 서울=11)")
    private String sidoCd; // 시도 코드
}
