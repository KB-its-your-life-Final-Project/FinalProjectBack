package com.lighthouse.alarm.controller;

import com.lighthouse.alarm.dto.AlarmResponseDto;
import com.lighthouse.alarm.dto.AlarmSettingRequestDto;
import com.lighthouse.alarm.mapper.AlarmMapper;
import com.lighthouse.alarm.service.AlarmService;
import com.lighthouse.response.ApiResponse;
import com.lighthouse.response.ErrorCode;
import com.lighthouse.response.SuccessCode;
import com.lighthouse.security.util.JwtCookieUtil;
import com.lighthouse.security.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/alarm")
@RequiredArgsConstructor
@Slf4j
public class AlarmController {
   private final JwtCookieUtil jwtCookieUtil;
   private final JwtUtil jwtUtil;
   private final AlarmMapper alarmMapper;
   private final AlarmService alarmService;


   // 특정 알림 설정 업데이트
   // 알림 설정 페이지에서 사용
   @PutMapping("/settings")
   public ResponseEntity<ApiResponse<Void>> updateAlarmSetting(@RequestBody AlarmSettingRequestDto requestdto, HttpServletRequest request){
       // 특정 타입의 알림 설정 변경( get_alarm 속성 업데이트)
      try{
         Integer memberId = getMemberId(request);

         if (memberId == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(ErrorCode.UNAUTHORIZED_USER));
         }
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
   public ResponseEntity<ApiResponse<List<AlarmResponseDto>>> getAlarmList(
           HttpServletRequest request
   ){
       // 사용자의 알림 목록 조회
      try{
         Integer memberId = getMemberId(request);

         if (memberId == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(ErrorCode.UNAUTHORIZED_USER));
         }
         // 보내야 할 알림 목록들 조회(알림 받기로 한 것만)
         alarmService.getAlarmList(memberId);
         return ResponseEntity.ok(ApiResponse.success(SuccessCode.ALARM_FETCH_SUCCESS));
      }catch(Exception e){
         log.error("알림 목록 조회 실패",e);
         return ResponseEntity.internalServerError()
                 .body(ApiResponse.error(ErrorCode.ALARM_FETCH_FAIL));
      }
   }

   // 알림 읽음 처리
   @PutMapping("/{alarmId}/read")
   public ResponseEntity<ApiResponse<Void>> markAlarmRead(@PathVariable int alarmId, HttpServletRequest request){
       // is_checked를 읽음 처리
      try{
         Integer memberId = getMemberId(request);

         if (memberId == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(ErrorCode.UNAUTHORIZED_USER));
         }
         alarmService.setAlarmRead(memberId, alarmId);
         return ResponseEntity.ok(ApiResponse.success(SuccessCode.ALARM_READ_SUCCESS));
      }catch(Exception e){
         log.error("알림 읽음 처리 실패",e);
         return ResponseEntity.internalServerError()
                 .body(ApiResponse.error(ErrorCode.ALARM_FETCH_FAIL));
      }

   }

   private Integer getMemberId(HttpServletRequest request) {
      try {
         // 1. 쿠키에서 accessToken 추출
         String accessToken = jwtCookieUtil.getAccessTokenFromRequest(request);
         if (accessToken == null) {
            // 2. accessToken이 없으면 refreshToken으로 재시도
            String refreshToken = jwtCookieUtil.getRefreshTokenFromRequest(request);
            if (refreshToken == null) {
               log.info("JWT 토큰이 없습니다.");
               return null;
            }
            // refreshToken에서 사용자 ID 추출
            String subject = jwtUtil.getSubjectFromToken(refreshToken);
            return Integer.valueOf(subject);
         }

         // 3. accessToken에서 사용자 ID 추출
         String subject = jwtUtil.getSubjectFromToken(accessToken);
         return Integer.valueOf(subject);

      } catch (Exception e) {
         log.warn("JWT 토큰에서 사용자 ID 추출 실패: {}", e.getMessage());
         return null;
      }
   }
}
