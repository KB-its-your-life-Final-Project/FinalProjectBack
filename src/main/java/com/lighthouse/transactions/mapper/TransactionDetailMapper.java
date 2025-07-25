
package com.lighthouse.transactions.mapper;

import java.util.List;
import com.lighthouse.transactions.vo.TransactionGraphVO;

public interface TransactionDetailMapper {
    List<TransactionGraphVO> transactionGraph();
}