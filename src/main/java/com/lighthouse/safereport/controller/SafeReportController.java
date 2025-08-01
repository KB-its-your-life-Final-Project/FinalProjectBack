package com.lighthouse.safereport.controller;

import com.lighthouse.response.ApiResponse;
import com.lighthouse.response.SuccessCode;
import com.lighthouse.response.ErrorCode;
import com.lighthouse.safereport.dto.SafeReportRequestDto;
import com.lighthouse.safereport.dto.SafeReportResponseDto;
import com.lighthouse.safereport.dto.RecentSafeReportResponseDto;
import com.lighthouse.safereport.dto.RecentSafeReportDetailResponseDto;
import com.lighthouse.safereport.service.SafeReportService;
import com.lighthouse.safereport.service.RecentSafeReportService;
import com.lighthouse.safereport.entity.RentalRatioAndBuildyear;
import com.lighthouse.security.util.JwtCookieUtil;
import com.lighthouse.security.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponses;

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
    
    // 사용자로부터 건물, 예산 전달 받아 안심레포트 제공
    @PostMapping("/requestSafeReport")
    @ApiOperation(
        value = "안심 레포트 데이터 요청",
        notes = "건물의 위도/경도와 예산을 받아서 안심 레포트 정보를 생성합니다. " +
                "건축년도, 거래금액, 전세가율, 위반여부, 층수/용도 정보를 포함합니다."
    )
    @ApiResponses({
        @io.swagger.annotations.ApiResponse(code = 200, message = "성공적으로 안심 레포트 데이터를 조회했습니다."),
        @io.swagger.annotations.ApiResponse(code = 404, message = "요청한 위치의 건물 정보를 찾을 수 없습니다."),
        @io.swagger.annotations.ApiResponse(code = 500, message = "서버 내부 오류가 발생했습니다.")
    })
    public ResponseEntity<ApiResponse<SafeReportResponseDto>> receiveForm(
        @ApiParam(value = "안심 레포트 요청 데이터", required = true) 
        @RequestBody SafeReportRequestDto dto,
        HttpServletRequest request
    ){
        // 건축년도, 거래 금액, 전세가율 얻기
        RentalRatioAndBuildyear rentalRatioAndBuildyear = safeReportService.generateSafeReport(dto);
        // 위반 여부와 층수/용도 정보 통합 조회 (건축물 대장 정보)
        SafeReportService.BuildingInfoResult buildingInfo = safeReportService.getBuildingInfo(dto);

        // 둘 다 없는 경우에만 404 에러 반환
        boolean hasRentalInfo = rentalRatioAndBuildyear != null;
        boolean hasBuildingInfo = buildingInfo != null && 
                                 (buildingInfo.getViolationStatus() != null || 
                                  (buildingInfo.getFloorAndPurposeList() != null && !buildingInfo.getFloorAndPurposeList().isEmpty()));
        
        if (!hasRentalInfo && !hasBuildingInfo) {
            log.warn("모든 정보 없음: lat={}, lng={}", dto.getLat(), dto.getLng());
            return ResponseEntity.status(404)
                    .body(ApiResponse.error(ErrorCode.BUILDINGINFO_NOT_FOUND));
        }

        // 부분 데이터라도 있으면 성공 응답
        SafeReportResponseDto responseDto = new SafeReportResponseDto(
            rentalRatioAndBuildyear, 
            buildingInfo != null ? buildingInfo.getViolationStatus() : null, 
            buildingInfo != null ? buildingInfo.getFloorAndPurposeList() : null
        );
        
        // 최근 본 안심레포트에 저장
        try {
            Long userId = getUserId(request);
            if (userId != null) {
                recentSafeReportService.saveRecentSafeReport(userId, dto, responseDto);
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
        Long userId = getUserId(request);
        if (userId == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error(ErrorCode.UNAUTHORIZED));
        }
        
        List<RecentSafeReportResponseDto> recentReports = recentSafeReportService.getRecentReports(userId);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.SAFEREPORT_FETCH_SUCCESS, recentReports));
    }

    // 특정 안심 레포트 상세 조회
    @GetMapping("/recentSafeReport/{id}")
    @ApiOperation(
        value = "최근 본 안심레포트 상세 조회",
        notes = "특정 안심레포트의 상세 정보를 조회합니다."
    )
    public ResponseEntity<ApiResponse<RecentSafeReportDetailResponseDto>> getRecentReportDetail(
        @PathVariable Long id,
        HttpServletRequest request
    ){
        Long userId = getUserId(request);
        if (userId == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error(ErrorCode.UNAUTHORIZED));
        }
        
        RecentSafeReportDetailResponseDto report = recentSafeReportService.getRecentReportDetail(id, userId);
        
        if (report == null) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error(ErrorCode.RECENT_SAFEREPORT_NOT_FOUND));
        }
        
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.SAFEREPORT_FETCH_SUCCESS, report));
    }
    
    // 최근 본 안심레포트 삭제
    @DeleteMapping("/recentSafeReport/{id}")
    @ApiOperation(
        value = "최근 본 안심레포트 삭제",
        notes = "특정 안심레포트를 최근 본 목록에서 삭제합니다."
    )
    public ResponseEntity<ApiResponse<Void>> deleteRecentReport(
        @PathVariable Long id,
        HttpServletRequest request
    ){
        Long userId = getUserId(request);
        if (userId == null) {
            return ResponseEntity.status(401)
                    .body(ApiResponse.error(ErrorCode.UNAUTHORIZED));
        }
        
        recentSafeReportService.deleteRecentReport(id, userId);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.SAFEREPORT_FETCH_SUCCESS, null));
    }
    
    // 사용자 ID 추출 메서드 (JWT 토큰에서)
    private Long getUserId(HttpServletRequest request) {
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
                return Long.valueOf(subject);
            }
            
            // 3. accessToken에서 사용자 ID 추출
            String subject = jwtUtil.getSubjectFromToken(accessToken);
            return Long.valueOf(subject);
            
        } catch (Exception e) {
            log.warn("JWT 토큰에서 사용자 ID 추출 실패: {}", e.getMessage());
            return null;
        }
    }
}

