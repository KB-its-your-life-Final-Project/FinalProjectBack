package com.lighthouse.alarm.controller;

import com.lighthouse.alarm.dto.AlarmResponseDto;
import com.lighthouse.alarm.dto.AlarmSettingDto;
import com.lighthouse.alarm.dto.AlarmSettingRequestDto;
import com.lighthouse.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alarm")
public class AlarmController {
    // 사용자별 알림 설정된 것 조회
    // 알림 오는 것들이 뜨는 페이지에서 사용
    @GetMapping("/settings")
    public ResponseEntity<ApiResponse<List<AlarmSettingDto>>> getAlarmSettings(){
        // member_id로 해당 사용자의 알람 설정들 조회
        // 알림 유형(type)별로 get_alarm 상태 반환
    }

    // 알림 설정 업데이트
    // 알림 설정 페이지에서 사용
    @PutMapping("/settings")
    public ResponseEntity<ApiResponse<Void>> updateAlarmSetting(@RequestBody AlarmSettingRequestDto request){
        // 특정 타입의 알림 설정 변경( get_alarm 속성 업데이트)
    }

    // 알림 목록 조회
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<AlarmResponseDto>>> getAlarmList(
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="20") int size
    ){
        // 사용자의 알림 목록 조회
    }

    // 알림 읽음 처리
    @PutMapping("/{alarmId}/read")
    public ResponseEntity<ApiResponse<Void>> markAlarmRead(@PathVariable int alarmId){
        // is_checked를 읽음 처리
    }
}
