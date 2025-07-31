package com.lighthouse.safereport.controller;

import com.lighthouse.response.ApiResponse;
import com.lighthouse.response.SuccessCode;
import com.lighthouse.response.ErrorCode;
import com.lighthouse.safereport.dto.SafeReportRequestDto;
import com.lighthouse.safereport.dto.SafeReportResponseDto;
import com.lighthouse.safereport.service.SafeReportService;
import com.lighthouse.safereport.entity.RentalRatioAndBuildyear;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
@Slf4j
@Api(tags = "SafeReport", description = "안전 리포트 관련 API")
public class SafeReportController {
    private final SafeReportService service;
    
    // 사용자로부터 건물, 예산 전달 받아 처리
    @PostMapping("/requestData")
    @ApiOperation(
        value = "안전 리포트 데이터 요청",
        notes = "건물의 위도/경도와 예산을 받아서 안전 리포트 정보를 생성합니다. " +
                "건축년도, 거래금액, 전세가율, 위반여부, 층수/용도 정보를 포함합니다."
    )
    @ApiResponses({
        @io.swagger.annotations.ApiResponse(code = 200, message = "성공적으로 안전 리포트 데이터를 조회했습니다."),
        @io.swagger.annotations.ApiResponse(code = 404, message = "요청한 위치의 건물 정보를 찾을 수 없습니다."),
        @io.swagger.annotations.ApiResponse(code = 500, message = "서버 내부 오류가 발생했습니다.")
    })
    public ResponseEntity<ApiResponse<SafeReportResponseDto>> receiveForm(
        @ApiParam(value = "안전 리포트 요청 데이터", required = true) 
        @RequestBody SafeReportRequestDto dto
    ){
        // 건축년도, 거래 금액, 전세가율 얻기
        RentalRatioAndBuildyear rentalRatioAndBuildyear = service.generateSafeReport(dto);
        // 위반 여부와 층수/용도 정보 통합 조회 (건축물 대장 정보)
        SafeReportService.BuildingInfoResult buildingInfo = service.getBuildingInfo(dto);

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
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.SAFEREPORT_FETCH_SUCCESS, responseDto));
    }
}

