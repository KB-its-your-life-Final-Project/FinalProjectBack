/*package com.lighthouse.transactions.controller;

import com.lighthouse.transactions.service.TransactionDetailService;
import com.lighthouse.transactions.vo.MonthlyTransactionVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/transactions")
@Slf4j
@RequiredArgsConstructor
public class TransactionDetailController {

    private final TransactionDetailService transactiondetailService;
    @GetMapping
    public List<MonthlyTransactionVO> getMonthlyAvgTransactions(
            @RequestParam(defaultValue = "2020") int startYear) {
        return transactiondetailService.getMonthlyAvgTransactions(startYear);
    }

}*/
