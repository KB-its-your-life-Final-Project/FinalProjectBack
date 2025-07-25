package com.lighthouse.transactions.service;

import com.lighthouse.transactions.mapper.TransactionDetailMapper;
import com.lighthouse.transactions.vo.TransactionGraphVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionDetailService {

    @Autowired
    private TransactionDetailMapper transactionDetailMapper;

    public List<TransactionGraphVO> transactionGraph() {
        return transactionDetailMapper.transactionGraph();
    }
}
