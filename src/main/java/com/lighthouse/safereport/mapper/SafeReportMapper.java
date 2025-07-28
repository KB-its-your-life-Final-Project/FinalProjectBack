package com.lighthouse.safereport.mapper;

import com.lighthouse.safereport.vo.BuildingTypeAndPurpose;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SafeReportMapper {
    // 건물의 정보 (위반 여부, 용도 획득)
    BuildingTypeAndPurpose getViolateAndPurpose(@Param("lat")double lat, @Param("lng")double lng);
}
