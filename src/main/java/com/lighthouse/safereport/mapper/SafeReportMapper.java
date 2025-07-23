package com.lighthouse.safereport.mapper;

import com.lighthouse.safereport.vo.FormData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SafeReportMapper {
    FormData selectByCoord(@Param("lat")double lat, @Param("lng")double lng);
}
