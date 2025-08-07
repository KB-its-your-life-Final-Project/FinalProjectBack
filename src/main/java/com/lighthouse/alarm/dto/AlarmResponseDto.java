package com.lighthouse.alarm.dto;

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
@ApiModel(description = "알림 응답 데이터")
public class AlarmResponseDto {
    @ApiModelProperty(value = "알림 ID", example = "1")
    private Integer id;
    
    @ApiModelProperty(value = "회원 ID", example = "123")
    private Integer memberId;
    
    @ApiModelProperty(value = "알림 타입 (2: 시세변화, 3: 계약만료)", example = "3")
    private Integer type;
    
    @ApiModelProperty(value = "알림 내용", example = "등록하신 매물 'OOO아파트'의 계약이 30일 후 만료됩니다.")
    private String text;
    
    @ApiModelProperty(value = "등록 날짜", example = "2024-01-01 10:00:00")
    private String regDate;
    
    @ApiModelProperty(value = "확인 여부 (0: 확인 안함, 1: 확인함)", example = "0")
    private Integer isChecked;
    
    @ApiModelProperty(value = "알림 수신 여부 (0: 수신안함, 1: 수신함)", example = "1")
    private Integer getAlarm;
}
