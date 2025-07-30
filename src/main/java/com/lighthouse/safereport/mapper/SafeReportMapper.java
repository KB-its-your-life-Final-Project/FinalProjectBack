package com.lighthouse.safereport.mapper;

import com.lighthouse.safereport.vo.ViolationStatusVO;
import com.lighthouse.safereport.vo.FloorAndPurpose;
import com.lighthouse.estate.dto.RealEstateSalesDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SafeReportMapper {
    // trade_type=1인 매매 데이터만 조회
    RealEstateSalesDTO getSalesByEstateIdWithTradeType(@Param("estateId") Integer estateId);
    
    // 위반 여부 조회
    ViolationStatusVO getViolationStatus(@Param("lat") double lat, @Param("lng") double lng);
    
    // 층수와 용도 정보 모두 조회
    List<FloorAndPurpose> getFloorAndPurposeList(@Param("lat") double lat, @Param("lng") double lng);
}
