
package com.lighthouse.transactions.mapper;

import java.util.Date;
import java.util.List;

import com.lighthouse.transactions.dto.TransactionRequestDTO;
import com.lighthouse.transactions.vo.TransactionGraphVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TransactionDetailMapper {
    List<TransactionGraphVO> findDate(TransactionRequestDTO requestDTO);
    // lat, lng를 이용해 buildingName 조회
    String findBuildingNameByLatLng(@Param("lat") double lat, @Param("lng") double lng);
    // lat/lng 기준 거래 데이터 조회
    List<TransactionGraphVO> findDateByLatLng(@Param("lat") double lat, @Param("lng") double lng,
                                              @Param("tradeType") Integer tradeType,
                                              @Param("startDate") String startDate,
                                              @Param("endDate") String endDate);

    List<TransactionGraphVO> findDateByBuildingName(@Param("buildingName") String buildingName,
                                                    @Param("tradeType") Integer tradeType,
                                                    @Param("startDate") String startDate,
                                                    @Param("endDate") String endDate);
    Date findLatestTransactionDateByLatLng(@Param("lat") double lat, @Param("lng") double lng);
}