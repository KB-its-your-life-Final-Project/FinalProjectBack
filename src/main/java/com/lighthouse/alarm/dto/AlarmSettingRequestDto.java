package com.lighthouse.alarm.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "알림 설정 변경 요청 데이터")
public class AlarmSettingRequestDto {
    @ApiModelProperty(value = "알림 타입 (2: 시세변화, 3: 계약만료)", example = "3", required = true)
    private Integer type;
    
    @ApiModelProperty(value = "알림 활성화 여부 (0: 비활성화, 1: 활성화)", example = "1", required = true)
    private Integer getAlarm;
}
