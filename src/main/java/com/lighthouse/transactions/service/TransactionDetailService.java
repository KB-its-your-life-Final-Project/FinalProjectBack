package com.lighthouse.transactions.service;

import com.lighthouse.transactions.dto.TransactionRequestDTO;
import com.lighthouse.transactions.dto.TransactionResponseDTO;
import com.lighthouse.transactions.mapper.TransactionDetailMapper;
import com.lighthouse.transactions.vo.TransactionGraphVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDate;

@RequiredArgsConstructor
@Service

public class TransactionDetailService {
    private final TransactionDetailMapper transactionDetailMapper;

    public List<TransactionResponseDTO> getFilteredTransactions(TransactionRequestDTO request) {
        // 느림 방지를 위헤 처음에는 1년치로 뜨고, 전체 누르면 전체 조회 가능
        if (request.getStartDate() == null || request.getEndDate() == null) {
            LocalDate now = LocalDate.now();
            request.setEndDate(now.toString());
            request.setStartDate(now.minusYears(1).toString());
        }

        List<TransactionGraphVO> rawList = transactionDetailMapper.findDate(request);

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
                            .build();
                })
                .collect(Collectors.toList());
    }


}