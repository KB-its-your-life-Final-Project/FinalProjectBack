package com.lighthouse.alarm.controller;

import com.lighthouse.alarm.dto.AlarmResponseDto;
import com.lighthouse.alarm.dto.AlarmSettingRequestDto;
import com.lighthouse.alarm.service.AlarmService;
import com.lighthouse.member.dto.MemberResponseDTO;
import com.lighthouse.member.service.MemberService;
import com.lighthouse.response.ApiResponse;
import com.lighthouse.response.ErrorCode;
import com.lighthouse.response.SuccessCode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/api/alarm")
@RequiredArgsConstructor
@Slf4j
@Api(tags="알림 관리")
public class AlarmController {
   private final AlarmService alarmService;
   private final MemberService memberService;
   
   // 특정 알림 설정 업데이트
   // 알림 설정 페이지에서 사용
   @PostMapping("/settings")
   @ApiOperation(value = "알림 설정 변경", notes = "사용자가 특정 알림 타입의 수신 여부를 설정합니다.")
   public ResponseEntity<ApiResponse<Void>> updateAlarmSetting(
           @RequestBody AlarmSettingRequestDto requestDto, 
           HttpServletRequest request,
           HttpServletResponse response){
      try{
         // findMemberLoggedIn을 바로 호출하여 토큰 검증 및 사용자 정보 조회
         MemberResponseDTO memberDto = memberService.findMemberLoggedIn(request, response);
         if (memberDto == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(ErrorCode.UNAUTHORIZED));
         }
         
         int memberId = memberDto.getId();
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
   public ResponseEntity<ApiResponse<List<AlarmResponseDto>>> getAlarmList(
           HttpServletRequest request,
           HttpServletResponse response){
      try{
         // findMemberLoggedIn을 바로 호출하여 토큰 검증 및 사용자 정보 조회
         MemberResponseDTO memberDto = memberService.findMemberLoggedIn(request, response);
         if (memberDto == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(ErrorCode.UNAUTHORIZED));
         }
         
         int memberId = memberDto.getId();
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
            HttpServletRequest request,
            HttpServletResponse response){
        try{
          // findMemberLoggedIn을 바로 호출하여 토큰 검증 및 사용자 정보 조회
          MemberResponseDTO memberDto = memberService.findMemberLoggedIn(request, response);
          if (memberDto == null) {
             return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                     .body(ApiResponse.error(ErrorCode.UNAUTHORIZED));
          }
          
          int memberId = memberDto.getId();
          alarmService.setAlarmRead(memberId, alarmId);
          return ResponseEntity.ok(ApiResponse.success(SuccessCode.ALARM_READ_SUCCESS));
       }catch(Exception e){
          log.error("알림 읽음 처리 실패: alarmId={}, error={}", alarmId, e.getMessage(), e);
          return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                  .body(ApiResponse.error(ErrorCode.ALARM_READ_FAIL));
       }
    }
   
   // 미확인인 알림 개수 조회 (프론트에서 주기적으로 호출-알림 아이콘에 표시하려고)
   @GetMapping("/count")
   @ApiOperation(value = "미확인 알림 개수 조회", notes = "사용자의 미확인 알림 개수를 조회합니다.")
   public ResponseEntity<ApiResponse<Integer>> getUnreadAlarmCount(
           HttpServletRequest request,
           HttpServletResponse response){
       try{
         // findMemberLoggedIn을 바로 호출하여 토큰 검증 및 사용자 정보 조회
         MemberResponseDTO memberDto = memberService.findMemberLoggedIn(request, response);
         if (memberDto == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(ErrorCode.UNAUTHORIZED));
         }
         
         int memberId = memberDto.getId();
         List<AlarmResponseDto> alarmList = alarmService.getAlarmList(memberId);
         return ResponseEntity.ok(ApiResponse.success(SuccessCode.ALARM_FETCH_SUCCESS, alarmList.size()));
      }catch(Exception e){
         log.error("알림 개수 조회 실패",e);
         return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                 .body(ApiResponse.error(ErrorCode.ALARM_FETCH_FAIL));
      }
    }
}
