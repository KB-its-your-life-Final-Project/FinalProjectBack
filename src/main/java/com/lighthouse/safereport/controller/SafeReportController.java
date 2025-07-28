package com.lighthouse.safereport.controller;

import com.lighthouse.response.ApiResponse;
import com.lighthouse.response.SuccessCode;
import com.lighthouse.response.ErrorCode;
import com.lighthouse.safereport.dto.SafeReportRequestDto;
import com.lighthouse.safereport.dto.SafeReportResponseDto;
import com.lighthouse.safereport.service.SafeReportService;
import com.lighthouse.safereport.vo.BuildingTypeAndPurpose;
import com.lighthouse.safereport.vo.RentalRatioAndBuildyear;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.annotations.Api;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
@Slf4j
@Api(tags = "SafeReport")
public class SafeReportController {
    private final SafeReportService service;
    // 사용자로부터 건물, 예산 전달 받아 처리
    @PostMapping("/requestData")
    public ResponseEntity<ApiResponse<SafeReportResponseDto>> receiveForm(@RequestBody SafeReportRequestDto dto){
        // 건축년도, 거래 금액, 전세가율 얻기
        RentalRatioAndBuildyear rentalRatioAndBuildyear = service.generateSafeReport(dto);
        // 위반 여부, 건물 용도 얻기
        BuildingTypeAndPurpose typeAndPurpose = service.generateSafeBuilding(dto);

        if (rentalRatioAndBuildyear == null) {
            log.warn("부동산 정보 없음: lat={}, lng={}", dto.getLat(), dto.getLng());
            return ResponseEntity.status(404)
                    .body(ApiResponse.error(ErrorCode.BUILDINGINFO_NOT_FOUND));
        }

        if (typeAndPurpose == null) {
            log.warn("건물 정보 없음 (위반 여부/용도): lat={}, lng={}", dto.getLat(), dto.getLng());
            return ResponseEntity.status(404)
                    .body(ApiResponse.error(ErrorCode.SAFEBUILDING_NOT_FOUND)); // 적절한 에러코드 사용
        }
        SafeReportResponseDto responseDto = new SafeReportResponseDto(rentalRatioAndBuildyear, typeAndPurpose);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.SAFEREPORT_FETCH_SUCCESS, responseDto));

    }
}

