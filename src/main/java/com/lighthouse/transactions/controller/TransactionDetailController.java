

package com.lighthouse.transactions.controller;

import com.lighthouse.transactions.service.TransactionDetailService;

import com.lighthouse.transactions.vo.TransactionGraphVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/transactions")
@Slf4j
@RequiredArgsConstructor
public class TransactionDetailController {
    @Autowired
    private final TransactionDetailService transactionDetailService;

    @GetMapping
    public List<TransactionGraphVO> transactionGraph() {

        return transactionDetailService.transactionGraph();


    }
}
