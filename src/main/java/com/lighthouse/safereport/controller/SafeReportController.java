package com.lighthouse.safereport.controller;

import com.lighthouse.response.ApiResponse;
import com.lighthouse.response.SuccessCode;
import com.lighthouse.response.ErrorCode;
import com.lighthouse.safereport.dto.SafeReportRequestDto;
import com.lighthouse.safereport.service.SafeReportService;
import com.lighthouse.safereport.vo.BuildingTypeAndPurpose;
import com.lighthouse.safereport.vo.SafeResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
@Slf4j
public class SafeReportController {
    private final SafeReportService service;
    @PostMapping("/requestData")
    public ResponseEntity<ApiResponse<SafeResult>> receiveForm(@RequestBody SafeReportRequestDto dto){
        // 건축년도, 거래 금액, 전세가율 얻기
        SafeResult result = service.generateSafeReport(dto);
        // 위반 여부, 건물 용도 얻기
        BuildingTypeAndPurpose typeAndPurpose = service.generateSafeBuilding(dto);

        if(result == null){
            log.warn("해당 좌표에 대한 데이터가 없습니다:{}, {}",dto.getLat(),dto.getLng());
            return ResponseEntity.status(404)
                .body(ApiResponse.error(ErrorCode.SAFEREPORT_NOT_FOUND));
        }
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.SAFEREPORT_FETCH_SUCCESS, result));
    }
}

