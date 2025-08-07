package com.lighthouse.alarm.controller;

import com.lighthouse.alarm.dto.AlarmResponseDto;
import com.lighthouse.alarm.dto.AlarmSettingRequestDto;
import com.lighthouse.alarm.mapper.AlarmMapper;
import com.lighthouse.alarm.service.AlarmService;
import com.lighthouse.response.ApiResponse;
import com.lighthouse.response.ErrorCode;
import com.lighthouse.response.SuccessCode;
import com.lighthouse.security.util.JwtUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/alarm")
@RequiredArgsConstructor
@Slf4j
@Api(tags="알림 관리")
public class AlarmController {
   private final JwtUtil jwtUtil;
   private final AlarmMapper alarmMapper;
   private final AlarmService alarmService;
   
   // 특정 알림 설정 업데이트
   // 알림 설정 페이지에서 사용
   @PostMapping("/settings")
   @ApiOperation(value = "알림 설정 변경", notes = "사용자가 특정 알림 타입의 수신 여부를 설정합니다.")
   public ResponseEntity<ApiResponse<Void>> updateAlarmSetting(
           @RequestBody AlarmSettingRequestDto requestDto, 
           @CookieValue(value = "accessToken", required = false) String token){
       // 특정 타입의 알림 설정 변경 (get_alarm 속성 업데이트)
      try{
         if (token == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(ErrorCode.UNAUTHORIZED));
         }
         
         Integer memberId = Integer.valueOf(jwtUtil.getSubjectFromToken(token));
         
         log.info("알림 설정 변경 요청: memberId={}, type={}, getAlarm={}", 
                 memberId, requestDto.getType(), requestDto.getGetAlarm());
         
         // 알림 설정 업데이트
         alarmService.updateAlarmSetting(memberId, requestDto.getType(), requestDto.getGetAlarm());
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
   @ApiOperation(value = "알림 목록 조회", notes = "사용자의 미확인 알림 목록을 조회합니다.")
   public ResponseEntity<ApiResponse<List<AlarmResponseDto>>> getAlarmList(@CookieValue(value = "accessToken", required = false) String token){
       // 사용자의 알림 목록 조회
      try{
         if (token == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(ErrorCode.UNAUTHORIZED));
         }
         
         Integer memberId = Integer.valueOf(jwtUtil.getSubjectFromToken(token));
         
         // 보내야 할 알림 목록들 조회 (알림 받기로 한 것만)
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
   @ApiOperation(value = "알림 읽음 처리", notes = "특정 알림을 읽음 처리합니다.")
   public ResponseEntity<ApiResponse<Void>> markAlarmRead(
           @ApiParam(value = "알림 ID", required = true, example = "1") 
           @PathVariable int alarmId, 
           @CookieValue(value = "accessToken", required = false) String token){
       // is_checked를 읽음 처리
      try{
         if (token == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(ErrorCode.UNAUTHORIZED));
         }
         
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
   @ApiOperation(value = "미확인 알림 개수 조회", notes = "사용자의 미확인 알림 개수를 조회합니다.")
   public ResponseEntity<ApiResponse<Integer>> getUnreadAlarmCount(@CookieValue(value = "accessToken", required = false) String token){
       try{
         if (token == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(ErrorCode.UNAUTHORIZED));
         }
         
         Integer memberId = Integer.valueOf(jwtUtil.getSubjectFromToken(token));
         
         List<AlarmResponseDto> alarmList = alarmService.getAlarmList(memberId);
         return ResponseEntity.ok(ApiResponse.success(SuccessCode.ALARM_FETCH_SUCCESS, alarmList.size()));
      }catch(Exception e){
         log.error("알림 개수 조회 실패",e);
         return ResponseEntity.internalServerError()
                 .body(ApiResponse.error(ErrorCode.ALARM_FETCH_FAIL));
      }
    }
    
    // 테스트용 알림 생성 (개발 완료 후 삭제 예정)
    @PostMapping("/test")
    @ApiOperation(value = "테스트 알림 생성", notes = "테스트용 알림을 생성합니다.")
    public ResponseEntity<ApiResponse<Void>> createTestAlarm(@CookieValue(value = "accessToken", required = false) String token){
        try{
         if (token == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(ErrorCode.UNAUTHORIZED));
         }
         
         Integer memberId = Integer.valueOf(jwtUtil.getSubjectFromToken(token));
         
         // 테스트 알림 생성
         alarmService.createAlarm(memberId, 1, "테스트 알림입니다.", "127.0.0.1");
         log.info("테스트 알림 생성 완료: memberId={}", memberId);
         
         return ResponseEntity.ok(ApiResponse.success(SuccessCode.ALARM_UPDATE_SUCCESS));
      }catch(Exception e){
         log.error("테스트 알림 생성 실패",e);
         return ResponseEntity.internalServerError()
                 .body(ApiResponse.error(ErrorCode.ALARM_SETTING_FAIL));
      }
    }
}
