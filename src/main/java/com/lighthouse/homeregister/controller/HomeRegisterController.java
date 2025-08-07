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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/myhome")
@RequiredArgsConstructor
@Slf4j
@Api(tags = "MyHomeRegister", description = "나의 집 등록 관련 API")
public class HomeRegisterController {

    private final HomeRegisterService homeRegisterService;
    private final JwtUtil jwtUtil;
    private final AlarmSchedulerService alarmSchedulerService;

    @GetMapping("/info")
    @ApiOperation(value = "나의 집 정보 조회", notes = "사용자의 집 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<HomeRegisterResponseDTO>> getHomeInfo(@CookieValue("accessToken") String token) {
        try {
            Integer userId = Integer.valueOf(jwtUtil.getSubjectFromToken(token));
            
            HomeRegister homeInfo = homeRegisterService.getHomeInfo(userId);
            
            if (homeInfo == null) {
                return ResponseEntity.ok(ApiResponse.success(SuccessCode.HOME_REGISTER_SUCCESS, null));
            }
            
            HomeRegisterResponseDTO response = HomeRegisterResponseDTO.builder()
                .estateId(homeInfo.getEstateId())
                .buildingName(homeInfo.getBuildingName())
                    .buildingNumber(homeInfo.getBuildingNumber())
                .actionType("EXIST")
                .contractStart(homeInfo.getContractStart() != null ? homeInfo.getContractStart().toString() : null)
                .contractEnd(homeInfo.getContractEnd() != null ? homeInfo.getContractEnd().toString() : null)
                .rentType(homeInfo.getRentType())
                .jeonseAmount(homeInfo.getJeonseAmount())
                .monthlyDeposit(homeInfo.getMonthlyDeposit())
                .monthlyRent(homeInfo.getMonthlyRent())
                .regDate(homeInfo.getRegDate() != null ? homeInfo.getRegDate().toString() : null)
                .build();
                
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
            @CookieValue("accessToken") String token,
            HttpServletRequest req) {
        
        try {
            // 사용자 ID 추출
            Integer userId = Integer.valueOf(jwtUtil.getSubjectFromToken(token));
            
            HomeRegisterResponseDTO response = homeRegisterService.registerHome(requestDTO, userId, req);
            
            // 집 등록 성공 후 알림 체크
            try {
                String regIp = req.getRemoteAddr();
                alarmSchedulerService.checkUserAlarmsOnLogin(userId, regIp);
                log.info("집 등록 후 알림 체크 완료: userId={}", userId);
            } catch (Exception e) {
                log.error("집 등록 후 알림 체크 실패: userId={}", userId, e);
                // 알림 체크 실패는 집 등록 성공에 영향을 주지 않음
            }
            
            return ResponseEntity.ok(ApiResponse.success(SuccessCode.HOME_REGISTER_SUCCESS, response));
            
        } catch (RuntimeException e) {
            if (e.getMessage().contains("부동산 정보를 찾을 수 없습니다")) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error(ErrorCode.ESTATE_NOT_FOUND_BY_COORDINATES));
            }
            log.error("집 정보 등록 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error(ErrorCode.HOME_REGISTER_FAIL));
        } catch (Exception e) {
            log.error("집 정보 등록 중 예상치 못한 오류 발생", e);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error(ErrorCode.HOME_REGISTER_FAIL));
        }
    }
}
