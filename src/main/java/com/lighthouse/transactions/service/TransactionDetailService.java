package com.lighthouse.transactions.service;

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
import java.util.stream.Collectors;
import java.time.LocalDate;

@RequiredArgsConstructor
@Service

public class TransactionDetailService {
    private final TransactionDetailMapper transactionDetailMapper;
    private final EstateService estateService;

    public String findBuildingNameByLatLng(double lat, double lng) {
        try {
            return transactionDetailMapper.findBuildingNameByLatLng(lat, lng);
        } catch (Exception e) {
            return null;
        }
    }

    // buildingName 기준 조회
    public List<TransactionResponseDTO> getFilteredTransactionsByBuildingName(TransactionRequestDTO request, String buildingName) {
        setDefaultDatesIfNull(request);
        List<TransactionGraphVO> rawList = transactionDetailMapper.findDateByBuildingName(
                buildingName,
                request.getTradeType(),
                request.getStartDate(),
                request.getEndDate()
        );

        return convertToResponseDTO(rawList);
    }

    // lat/lng 기준 조회
    public List<TransactionResponseDTO> getFilteredTransactionsByLatLng(TransactionRequestDTO request, double lat, double lng) {
        setDefaultDatesIfNull(request);

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

        return convertToResponseDTO(rawList);
    }


    private void setDefaultDatesIfNull(TransactionRequestDTO request) {
        if (request.getStartDate() == null || request.getEndDate() == null) {
            LocalDate now = LocalDate.now();
            request.setEndDate(now.toString());
            request.setStartDate(now.minusYears(1).toString());
        }
    }

//DTO 로 변환
private List<TransactionResponseDTO> convertToResponseDTO(List<TransactionGraphVO> rawList) {
        return rawList.stream()
                .map(vo -> {
                    String date = String.format("%04d-%02d-%02d", vo.getDealYear(), vo.getDealMonth(), vo.getDealDay());
                    String type = vo.getTradeType() == 1 ? "매매" : "전월세";
                    int price = (vo.getTradeType() == 1)
                            ? vo.getDealAmount()
                            : (vo.getDeposit() != null ? vo.getDeposit() : 0);

                    return TransactionResponseDTO.builder()
                            .date(date)
                            .type(type)
                            .price(price)
                            .estateId(vo.getEstateId())
                            .buildingName(vo.getBuildingName())
                            .build();
                })
                .collect(Collectors.toList());

    }


}