package com.lighthouse.safereport.controller;

import com.lighthouse.response.ApiResponse;
import com.lighthouse.response.SuccessCode;
import com.lighthouse.response.ErrorCode;
import com.lighthouse.safereport.dto.SafeReportRequestDto;
import com.lighthouse.safereport.dto.SafeReportResponseDto;
import com.lighthouse.safereport.dto.RecentSafeReportResponseDto;
import com.lighthouse.safereport.service.SafeReportService;
import com.lighthouse.safereport.service.RecentSafeReportService;
import com.lighthouse.security.util.JwtCookieUtil;
import com.lighthouse.security.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
@Slf4j
@Api(tags = "SafeReport", description = "안심 레포트 관련 API")
public class SafeReportController {
    private final SafeReportService safeReportService;
    private final RecentSafeReportService recentSafeReportService;
    private final JwtCookieUtil jwtCookieUtil;
    private final JwtUtil jwtUtil;
    
    // 사용자로부터 건물, 예산 전달 받아 안심레포트 생성 + 제공
    @PostMapping("/requestSafeReport")
    @ApiOperation(
        value = "안심 레포트 데이터 요청",
        notes = "건물의 위도/경도와 예산을 받아서 안심 레포트 정보를 생성합니다. " +
                "건축년도, 거래금액, 전세가율, 위반여부, 층수/용도 정보를 포함합니다."
    )
    public ResponseEntity<ApiResponse<SafeReportResponseDto>> generateSafeReport(
        @ApiParam(value = "안심 레포트 요청 데이터", required = true) 
        @RequestBody SafeReportRequestDto dto,
        HttpServletRequest request
    ){
        // 안심레포트 데이터 생성
        SafeReportResponseDto responseDto = safeReportService.generateCompleteSafeReport(dto);
        
        // 데이터가 없으면 404 에러 반환
        if (responseDto == null) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error(ErrorCode.BUILDINGINFO_NOT_FOUND));
        }
        
        // 최근 본 안심레포트에 저장
        try {
            Integer userId = getUserId(request);
            if (userId != null && safeReportService.shouldSaveToRecentReports(responseDto)) {
                recentSafeReportService.saveRecentSafeReport(userId, dto);
            }
        } catch (Exception e) {
            log.warn("최근 본 안심레포트 저장 실패: {}", e.getMessage());
        }
        
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.SAFEREPORT_FETCH_SUCCESS, responseDto));
    }
    


    // 최근 본 안심 레포트 목록 조회
    @GetMapping("/recentSafeReport")
    @ApiOperation(
        value = "최근 본 안심레포트 목록 조회",
        notes = "사용자가 최근에 본 안심레포트 목록을 조회합니다."
    )
    public ResponseEntity<ApiResponse<List<RecentSafeReportResponseDto>>> getRecentReports(
        HttpServletRequest request
    ){
        Integer userId = getUserId(request);
        if (userId == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error(ErrorCode.UNAUTHORIZED));
        }
        
        List<RecentSafeReportResponseDto> recentReports = recentSafeReportService.getRecentReports(userId);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.RECENT_SAFEREPORT_LIST_SUCCESS, recentReports));
    }

    // 특정 안심 레포트 상세 조회
    @GetMapping("/recentSafeReport/{id}")
    @ApiOperation(
        value = "최근 본 안심레포트 상세 조회",
        notes = "특정 안심레포트의 상세 정보를 조회합니다."
    )
    public ResponseEntity<ApiResponse<SafeReportResponseDto>> getRecentReportDetail(
        @PathVariable Integer id,
        HttpServletRequest request
    ){
        Integer userId = getUserId(request);
        if (userId == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error(ErrorCode.UNAUTHORIZED));
        }
        
        SafeReportResponseDto report = recentSafeReportService.getRecentReportDetail(id, userId);
        
        if (report == null) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error(ErrorCode.RECENT_SAFEREPORT_NOT_FOUND));
        }
        
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.RECENT_SAFEREPORT_DETAIL_SUCCESS, report));
    }
    
    // 최근 본 안심레포트 삭제
    @DeleteMapping("/recentSafeReport/{id}")
    @ApiOperation(
        value = "최근 본 안심레포트 삭제",
        notes = "특정 안심레포트를 최근 본 목록에서 삭제합니다."
    )
    public ResponseEntity<ApiResponse<Void>> deleteRecentReport(
        @PathVariable Integer id,
        HttpServletRequest request
    ){
        Integer userId = getUserId(request);
        if (userId == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error(ErrorCode.UNAUTHORIZED));
        }
        
        recentSafeReportService.deleteRecentReport(id, userId);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.RECENT_SAFEREPORT_DELETE_SUCCESS, null));
    }
    
    // 사용자 ID 추출 메서드 (JWT 토큰에서)
    private Integer getUserId(HttpServletRequest request) {
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

