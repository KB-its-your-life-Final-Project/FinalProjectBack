package com.batch.toCoord.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface buildingToCoordMapper {
    // 지번 주소만 조회
    List<String> selectAlljibunAddr();

    // 좌표 업데이트
    int updateCoordinates(
            @Param("jibunAddr") String jibunAddr,
            @Param("lat") Double lat,
            @Param("lng") Double lng
    );
}
