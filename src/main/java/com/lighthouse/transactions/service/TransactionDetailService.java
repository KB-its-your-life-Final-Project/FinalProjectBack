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

@Service
@RequiredArgsConstructor
public class TransactionDetailService {
    private TransactionDetailMapper transactionDetailMapper;

    public List<TransactionResponseDTO> getFilteredTransactions(TransactionRequestDTO request) {
        List<TransactionGraphVO> rawList = transactionDetailMapper.findDate(request);

        return rawList.stream()
                .map(vo -> {
                    String date = String.format("%04d-%02d-%02d", vo.getDealYear(), vo.getDealMonth(), vo.getDealDay());
                    String type = vo.getTradeType() == 1 ? "매매" : "전월세";
                    int price = (vo.getTradeType() == 1)
                            ? vo.getDealAmount() / 10000
                            : (vo.getDeposit() != null ? vo.getDeposit() / 10000 : 0);

                    return TransactionResponseDTO.builder()
                            .date(date)
                            .type(type)
                            .price(price)
                            .build();
                })
                .collect(Collectors.toList());
    }


}