package com.lighthouse.homeregister.controller;

import com.lighthouse.homeregister.dto.HomeRegisterRequestDTO;
import com.lighthouse.homeregister.dto.HomeRegisterResponseDTO;
import com.lighthouse.homeregister.entity.HomeRegister;
import com.lighthouse.homeregister.service.HomeRegisterService;
import com.lighthouse.response.ApiResponse;
import com.lighthouse.response.SuccessCode;
import com.lighthouse.security.util.JwtUtil;
import com.lighthouse.response.ErrorCode;
import com.lighthouse.alarm.service.AlarmSchedulerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.NoSuchElementException;
import com.lighthouse.member.dto.MemberResponseDTO;
import com.lighthouse.member.service.MemberService;
import org.springframework.http.HttpStatus;
import com.lighthouse.member.entity.Member;

@RestController
@RequestMapping("/api/myhome")
@RequiredArgsConstructor
@Slf4j
@Api(tags = "MyHomeRegister", description = "나의 집 등록 관련 API")
public class HomeRegisterController {

    private final HomeRegisterService homeRegisterService;
    private final JwtUtil jwtUtil;
    private final AlarmSchedulerService alarmSchedulerService;
    private final MemberService memberService;

    @GetMapping("/info")
    @ApiOperation(value = "나의 집 정보 조회", notes = "사용자의 집 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<HomeRegisterResponseDTO>> getHomeInfo(HttpServletRequest req, HttpServletResponse resp) {
        try {
            // findMemberLoggedIn을 바로 호출하여 토큰 검증 및 사용자 정보 조회
            MemberResponseDTO memberDto = memberService.findMemberLoggedIn(req, resp);
            if (memberDto == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(ErrorCode.UNAUTHORIZED));
            }
            
            int memberId = memberDto.getId();
            HomeRegisterResponseDTO response = homeRegisterService.getHomeInfo(memberId);
            
            if (response == null) {
                return ResponseEntity.ok(ApiResponse.success(SuccessCode.HOME_REGISTER_SUCCESS, null));
            }
            
            return ResponseEntity.ok(ApiResponse.success(SuccessCode.HOME_REGISTER_SUCCESS, response));
            
        } catch (Exception e) {
            log.error("집 정보 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error(ErrorCode.HOME_REGISTER_FAIL));
        }
    }

    @PostMapping("/register")
    @ApiOperation(value = "나의 집 정보 등록", notes = "사용자의 집 정보를 등록합니다.")
    public ResponseEntity<ApiResponse<HomeRegisterResponseDTO>> registerHome(
            @RequestBody HomeRegisterRequestDTO requestDTO,
            HttpServletRequest req,
            HttpServletResponse resp) {
        
        try {
            // findMemberLoggedIn을 바로 호출하여 토큰 검증 및 사용자 정보 조회
            MemberResponseDTO memberDto = memberService.findMemberLoggedIn(req, resp);
            if (memberDto == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(ErrorCode.UNAUTHORIZED));
            }
            
            int memberId = memberDto.getId();
            HomeRegisterResponseDTO response = homeRegisterService.registerHome(requestDTO, memberId, req);
            
            log.info("집 정보 등록/수정 완료: memberId={}, actionType={}", memberId, response.getActionType());
            
            // 집 등록/수정 성공 후 알림 체크
            try {
                String regIp = req.getRemoteAddr();
                alarmSchedulerService.checkAlarmsOnHomeUpdate(memberId, regIp);
                log.info("집 등록/수정 후 알림 체크 완료: memberId={}, actionType={}", memberId, response.getActionType());
            } catch (Exception e) {
                log.error("집 등록/수정 후 알림 체크 실패: memberId={}", memberId, e);
                // 알림 체크 실패는 집 등록/수정 성공에 영향을 주지 않음
            }
            
            // 집 정보 등록/수정 시 단계별 알림 처리
            if ("NEW".equals(response.getActionType())) {
                // 새로 등록하는 경우: 첫 번째 단계별 알림 생성
                try {
                    String regIp = req.getRemoteAddr();
                    alarmSchedulerService.createInitialHouseContractAlarm(memberId, regIp);
                    log.info("집 정보 등록 시 첫 번째 단계별 알림 생성 완료: memberId={}", memberId);
                } catch (Exception e) {
                    log.error("집 정보 등록 시 첫 번째 단계별 알림 생성 실패: memberId={}", memberId, e);
                    // 알림 생성 실패는 집 등록 성공에 영향을 주지 않음
                }
            } else if ("UPDATE".equals(response.getActionType())) {
                // 수정하는 경우: 기존 Type 1 알림들을 모두 삭제하고 새로운 1단계 알림 생성
                try {
                    String regIp = req.getRemoteAddr();
                    alarmSchedulerService.resetHouseContractAlarms(memberId, regIp);
                    log.info("집 정보 수정 시 알림 초기화 완료: memberId={}", memberId);
                } catch (Exception e) {
                    log.error("집 정보 수정 시 알림 초기화 실패: memberId={}", memberId, e);
                    // 알림 초기화 실패는 집 수정 성공에 영향을 주지 않음
                }
            }
            
            return ResponseEntity.ok(ApiResponse.success(SuccessCode.HOME_REGISTER_SUCCESS, response));
            
        } catch (Exception e) {
            log.error("집 정보 등록/수정 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error(ErrorCode.HOME_REGISTER_FAIL));
        }
    }
}
