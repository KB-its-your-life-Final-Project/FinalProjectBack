package com.lighthouse.safereport.mapper;

import com.lighthouse.estate.dto.EstateSalesDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SafeReportMapper {
    // trade_type=1인 매매 데이터만 조회 (역전세율 계산하려면 매매 데이터만 필요함)
    EstateSalesDTO getSalesByEstateIdWithTradeType(@Param("estateId") Integer estateId);
}
