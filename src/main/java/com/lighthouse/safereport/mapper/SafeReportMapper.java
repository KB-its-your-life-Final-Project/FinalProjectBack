package com.lighthouse.safereport.mapper;

import com.lighthouse.safereport.vo.FormDataVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SafeReportMapper {
    FormDataVO selectByCoord(@Param("lat")double lat, @Param("lng")double lng);
}
