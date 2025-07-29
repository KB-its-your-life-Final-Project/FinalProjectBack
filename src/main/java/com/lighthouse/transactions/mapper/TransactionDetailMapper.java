
package com.lighthouse.transactions.mapper;

import java.util.List;

import com.lighthouse.transactions.dto.TransactionRequestDTO;
import com.lighthouse.transactions.vo.TransactionGraphVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TransactionDetailMapper {
    List<TransactionGraphVO> findDate(TransactionRequestDTO requestDTO);
}