package com.lighthouse.transactions.service;

import com.lighthouse.transactions.converter.TransactionDetailConverter;
import com.lighthouse.transactions.dto.TransactionRequestDTO;
import com.lighthouse.transactions.dto.TransactionResponseDTO;
import com.lighthouse.transactions.mapper.TransactionDetailMapper;
import com.lighthouse.transactions.vo.TransactionGraphVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.lighthouse.estate.service.EstateService;
import com.lighthouse.estate.dto.EstateDTO;
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
}