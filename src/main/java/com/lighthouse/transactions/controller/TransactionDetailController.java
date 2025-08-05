

package com.lighthouse.transactions.controller;

import com.lighthouse.common.geocoding.service.GeoCodingService;
import com.lighthouse.transactions.dto.TransactionRequestDTO;
import com.lighthouse.transactions.dto.TransactionResponseDTO;
import com.lighthouse.transactions.service.TransactionDetailService;

import com.lighthouse.transactions.vo.TransactionGraphVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@Slf4j
@RequiredArgsConstructor
public class TransactionDetailController {
    private final TransactionDetailService transactionDetailService;
    private final GeoCodingService geoCodingService;

    @PostMapping("/detail")
    public List<TransactionResponseDTO> getFilteredData(@RequestBody TransactionRequestDTO request) {
        log.info("요청 들어옴: {}", request);
        double lat = 0.0;
        double lng = 0.0;

      //1. 지번이 있으면 위/경도 변환
        if (request.getJibunAddress() != null && !request.getJibunAddress().isEmpty()) {
            log.info("지번 주소로 좌표 변환 시도: {}", request.getJibunAddress());

            Map<String, Double> coordinates = geoCodingService.getCoordinateFromAddress(request.getJibunAddress());
            lat = coordinates.get("lat");
            lng = coordinates.get("lng");
            log.info("주소 변환 결과 lat={}, lng={}", lat, lng);
        } else if (request.getLat() != null && request.getLng() != null && request.getLat() != 0.0 && request.getLng() != 0.0) {
            // 프론트에서 좌표를 바로 전달한 경우
            lat = request.getLat();
            lng = request.getLng();
            log.info("프론트 전달 좌표 사용 lat={}, lng={}", lat, lng);
        } else {
            // jibunAddress, lat/lng 둘 다 없는 경우
            throw new IllegalArgumentException("jibunAddress 또는 lat/lng 값 중 하나는 반드시 전달되어야 합니다.");
        }


        // 4. Mapper에서 lat/lng 오차보정 쿼리로 buildingName 조회 시도
        String buildingName = transactionDetailService.findBuildingNameByLatLng(lat, lng);

        if (buildingName != null) {
            log.info("오차보정으로 buildingName 조회 성공: {}", buildingName);
            // 5. buildingName 기준으로 거래 데이터 조회
            return transactionDetailService.getFilteredTransactionsByBuildingName(request, buildingName);
        } else {
            log.info("buildingName 조회 실패, lat/lng 기준으로 거래 데이터 조회");
            // 6. lat/lng 기준으로 거래 데이터 조회
            return transactionDetailService.getFilteredTransactionsByLatLng(request, lat, lng);
        }
    }
}
