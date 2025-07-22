package com.lighthouse.safereport.controller;

import com.lighthouse.safereport.dto.FormDataDto;
import com.lighthouse.safereport.service.SafeReportService;
import com.lighthouse.safereport.vo.FormDataVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;


@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
@Slf4j
public class SafeReportController {
    private final SafeReportService service;
    @PostMapping("/formdata")
//    public ResponseEntity<ApiResponse<String>> receiveForm(@RequestBody FormDataDto dto){
//        return ResponseEntity.ok(new ApiResponse<>(true, "전송 성공", "전송 성공", 200));
//    }
    public ResponseEntity<FormDataVO> receiveForm(@RequestBody FormDataDto dto){
        FormDataVO result = service.getReportByRoadAddress(dto.getLat(), dto.getLng());

        if(result == null){
            log.warn("해당 건물명과 법정동에 대한 데이터가 없습니다:{}, {}",dto.getBuildingName(),dto.getDongName());
            return ResponseEntity.notFound().build();
        }
        // 점수 계산하는 로직
        int budget = dto.getBudget();
        int dealAmount = result.getDealAmount();
        int buildYear = result.getBuildYear();

        // 전세가율 계산
        double ratio = (budget/ (double)dealAmount)*100;
        int ratioScore = 0;

        if(ratio<=70)ratioScore=0;
        else if(ratio<=80)ratioScore=1;
        else if(ratio<=90)ratioScore=2;
        else ratioScore=3;

        // 연식 점수 계산 (건축 연도 기반)
        int currentYear = LocalDate.now().getYear();
        int age = currentYear-buildYear;
        int ageScore = 0;

        if(age<=10)ageScore=1;
        else if(age<=20)ageScore=2;
        else if(age<=30)ageScore=3;
        else ageScore=4;

        //최종 점수
        int totalScore = ratioScore+ageScore;
        result.setScore(totalScore);

        System.out.println(dto);
        System.out.println("최종 응답 데이터: " + result.getDealAmount() + ", " + result.getBuildYear() + ", " + result.getScore());

        return ResponseEntity.ok(result);
    }
}

