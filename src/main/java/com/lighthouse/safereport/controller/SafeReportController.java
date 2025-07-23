package com.lighthouse.safereport.controller;

import com.lighthouse.safereport.dto.SafeReportRequestDto;
import com.lighthouse.safereport.service.SafeReportService;
import com.lighthouse.safereport.vo.FormData;
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

    public ResponseEntity<FormData> receiveForm(@RequestBody SafeReportRequestDto dto){
        FormData result = service.generateSafeReport(dto);
        if(result == null){
            log.warn("해당 좌표에 대한 데이터가 없습니다:{}, {}",dto.getLat(),dto.getLng());
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(result);
    }
}

