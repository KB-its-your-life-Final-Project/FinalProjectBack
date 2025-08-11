package com.lighthouse.transactions.service;

import com.lighthouse.transactions.converter.TransactionDetailConverter;
import com.lighthouse.transactions.dto.TransactionRequestDTO;
import com.lighthouse.transactions.dto.TransactionResponseDTO;
import com.lighthouse.transactions.mapper.TransactionDetailMapper;
import com.lighthouse.transactions.vo.TransactionGraphVO;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import com.lighthouse.estate.service.EstateService;
import com.lighthouse.estate.dto.EstateDTO;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
@Service

public class TransactionDetailService {
    private final TransactionDetailMapper transactionDetailMapper;
    private final EstateService estateService;
    private final TransactionDetailConverter transactionDetailConverter;
    public String findBuildingNameByLatLng(double lat, double lng) {
        try {
            return transactionDetailMapper.findBuildingNameByLatLng(lat, lng);
        } catch (Exception e) {
            return null;
        }
    }

    // buildingName 기준 조회
    public List<TransactionResponseDTO> getFilteredTransactionsByBuildingName(TransactionRequestDTO request, String buildingName) {
        List<TransactionGraphVO> rawList = transactionDetailMapper.findDateByBuildingName(
                buildingName,
                request.getTradeType(),
                request.getStartDate(),
                request.getEndDate()
        );

        return transactionDetailConverter.toDTOList(rawList);
    }

    // lat/lng 기준 조회
    public List<TransactionResponseDTO> getFilteredTransactionsByLatLng(TransactionRequestDTO request, double lat, double lng) {
        if(request.getStartDate() == null && request.getEndDate() == null) {
            fillDateRangeIfMissing(request, lat, lng);
        }

        try {
            EstateDTO estateDTO = estateService.getEstateByLatLng(lat, lng);
            String buildingName = estateDTO.getBuildingName();
            if (buildingName != null && !buildingName.isEmpty()) {
                return getFilteredTransactionsByBuildingName(request, buildingName);
            }
        } catch (NoSuchElementException e) {

        }
        List<TransactionGraphVO> rawList = transactionDetailMapper.findDateByLatLng(
                lat,
                lng,
                request.getTradeType(),
                request.getStartDate(),
                request.getEndDate()
        );

        return transactionDetailConverter.toDTOList(rawList);
    }

    /**
     * startDate, endDate가 없으면 최신 거래일 기준으로 endDate 설정 후 1년 전 startDate 세팅
     */
    public void fillDateRangeIfMissing(TransactionRequestDTO request, double lat, double lng) {
        Date latestDate = transactionDetailMapper.findLatestTransactionDateByLatLng(lat, lng);
        if (latestDate != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            // endDate는 최신 거래일, String 변환
            LocalDate endLocalDate = latestDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            request.setEndDate(endLocalDate.format(formatter));

            // startDate는 최신 거래일로부터 1년 전, String 변환
            LocalDate startLocalDate = endLocalDate.minusYears(1);
            request.setStartDate(startLocalDate.format(formatter));
        }
    }
}