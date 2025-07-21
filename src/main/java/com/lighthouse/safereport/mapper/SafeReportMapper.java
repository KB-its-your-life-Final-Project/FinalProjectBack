package com.lighthouse.safereport.mapper;

import com.lighthouse.safereport.vo.FormDataVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SafeReportMapper {
    FormDataVO selectByRoadAddressAndBname(@Param("buildingName")String buildingName, @Param("dongName")String roadName);


}
