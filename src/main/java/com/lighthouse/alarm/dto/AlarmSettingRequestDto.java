package com.lighthouse.alarm.dto;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel(description = "알림 설정 변경 요청 데이터")
public class AlarmSettingRequestDto {
    private Integer type; //알림 타입
    private Integer isChecked; //활성화 여부
}
