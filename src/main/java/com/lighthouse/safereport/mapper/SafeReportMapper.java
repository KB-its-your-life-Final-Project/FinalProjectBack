package com.lighthouse.safereport.mapper;

import com.lighthouse.safereport.vo.FormDataVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SafeReportMapper {
    FormDataVO selectByRoadAddressAndBname(@Param("buildingName")String buildingName, @Param("dongName")String roadName);

    // 도로명 주소만 조회
    List<String> selectAllRoadAddr();

    // 좌표 업데이트
    int updateCoordinates(
            @Param("road_addr") String roadAddr,
            @Param("lat") Double lat,
            @Param("lng") Double lng
    );
}
