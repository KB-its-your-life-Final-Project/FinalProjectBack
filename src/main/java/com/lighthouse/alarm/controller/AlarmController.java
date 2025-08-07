package com.lighthouse.alarm.controller;

import com.lighthouse.alarm.dto.AlarmResponseDto;
import com.lighthouse.alarm.dto.AlarmSettingRequestDto;
import com.lighthouse.alarm.mapper.AlarmMapper;
import com.lighthouse.alarm.service.AlarmService;
import com.lighthouse.response.ApiResponse;
import com.lighthouse.response.ErrorCode;
import com.lighthouse.response.SuccessCode;
import com.lighthouse.security.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alarm")
@RequiredArgsConstructor
@Slf4j
public class AlarmController {
   private final JwtUtil jwtUtil;
   private final AlarmMapper alarmMapper;
   private final AlarmService alarmService;
   
   // 특정 알림 설정 업데이트
   // 알림 설정 페이지에서 사용
   @PutMapping("/settings")
   public ResponseEntity<ApiResponse<Void>> updateAlarmSetting(@RequestBody AlarmSettingRequestDto requestdto, @CookieValue("accessToken") String token){
       // 특정 타입의 알림 설정 변경( get_alarm 속성 업데이트)
      try{
         Integer memberId = Integer.valueOf(jwtUtil.getSubjectFromToken(token));
         
         //알림 설정 업데이트
         alarmService.updateAlarmSetting(memberId,requestdto.getType(),requestdto.getIsChecked());
         return ResponseEntity.ok(ApiResponse.success(SuccessCode.ALARM_UPDATE_SUCCESS));
      }catch(Exception e){
         log.error("알림 설정 업데이트 실패",e);
         return ResponseEntity.internalServerError()
                 .body(ApiResponse.error(ErrorCode.ALARM_SETTING_FAIL));
      }
   }

   // 알림 목록 조회
   // 알림 나타나는 페이지에서 사용
   @GetMapping("/list")
   public ResponseEntity<ApiResponse<List<AlarmResponseDto>>> getAlarmList(@CookieValue("accessToken") String token){
       // 사용자의 알림 목록 조회
      try{
         Integer memberId = Integer.valueOf(jwtUtil.getSubjectFromToken(token));
         
         // 보내야 할 알림 목록들 조회(알림 받기로 한 것만)
         List<AlarmResponseDto> alarmList = alarmService.getAlarmList(memberId);
         return ResponseEntity.ok(ApiResponse.success(SuccessCode.ALARM_FETCH_SUCCESS, alarmList));
      }catch(Exception e){
         log.error("알림 목록 조회 실패",e);
         return ResponseEntity.internalServerError()
                 .body(ApiResponse.error(ErrorCode.ALARM_FETCH_FAIL));
      }
   }

   // 알림 읽음 처리
   @PutMapping("/{alarmId}/read")
   public ResponseEntity<ApiResponse<Void>> markAlarmRead(@PathVariable int alarmId, @CookieValue("accessToken") String token){
       // is_checked를 읽음 처리
      try{
         Integer memberId = Integer.valueOf(jwtUtil.getSubjectFromToken(token));
         
         alarmService.setAlarmRead(memberId, alarmId);
         return ResponseEntity.ok(ApiResponse.success(SuccessCode.ALARM_READ_SUCCESS));
      }catch(Exception e){
         log.error("알림 읽음 처리 실패",e);
         return ResponseEntity.internalServerError()
                 .body(ApiResponse.error(ErrorCode.ALARM_FETCH_FAIL));
      }
   }
   
   // 새로운 알림 개수 조회 (프론트엔드에서 주기적으로 호출)
   @GetMapping("/count")
   public ResponseEntity<ApiResponse<Integer>> getUnreadAlarmCount(@CookieValue("accessToken") String token){
       try{
         Integer memberId = Integer.valueOf(jwtUtil.getSubjectFromToken(token));
         
         List<AlarmResponseDto> alarmList = alarmService.getAlarmList(memberId);
         return ResponseEntity.ok(ApiResponse.success(SuccessCode.ALARM_FETCH_SUCCESS, alarmList.size()));
      }catch(Exception e){
         log.error("알림 개수 조회 실패",e);
         return ResponseEntity.internalServerError()
                 .body(ApiResponse.error(ErrorCode.ALARM_FETCH_FAIL));
      }
   }
}
