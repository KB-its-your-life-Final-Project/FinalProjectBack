package com.lighthouse.transactions.converter;

import com.lighthouse.transactions.dto.TransactionResponseDTO;
import com.lighthouse.transactions.vo.TransactionGraphVO;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


@Component
public class TransactionDetailConverter {
    public TransactionResponseDTO toDTO(TransactionGraphVO vo){
        String date = String.format("%04d-%02d-%02d", vo.getDealYear(), vo.getDealMonth(), vo.getDealDay());
        String type = vo.getTradeType() == 1 ? "매매" : "전월세";
        return TransactionResponseDTO
                .builder()
                .date(date)
                .type(type)
                .dealAmount(vo.getDealAmount())
                .deposit(vo.getDeposit())
                .monthlyRent(vo.getMonthlyRent())
                .buildingName(vo.getBuildingName())
                .build();
    }
    public List<TransactionResponseDTO> toDTOList(Collection<TransactionGraphVO> entities) {
        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}
