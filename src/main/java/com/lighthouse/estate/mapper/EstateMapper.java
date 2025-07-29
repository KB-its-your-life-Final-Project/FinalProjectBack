package com.lighthouse.estate.mapper;

import com.lighthouse.estate.dto.RealEstateDTO;
import com.lighthouse.estate.dto.RealEstateSalesDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EstateMapper {
    
    /**
     * 위도/경도로 건물 정보 조회(단건 조회)
     * @param lat 위도
     * @param lng 경도
     * @return 건물 정보
     */
    RealEstateDTO getRealEstateByLocation(@Param("lat") double lat, @Param("lng") double lng);
    
    /**
     * 건물 ID 로 매매 정보 조회
     * @param estateId 건물 ID 
     * @return 매매 정보 리스트
     */
    List<RealEstateSalesDTO> getSalesByEstateId(@Param("estateId") Integer estateId);

    /**
     * 지도 사각형 영역 내 건물 정보 조회(다건 조회)
     * @param minLat 최소 위도
     * @param maxLat 최대 위도
     * @param minLng 최소 경도
     * @param maxLng 최대 경도
     * @return 건물 정보 리스트
     */
    List<RealEstateDTO> getAllRealEstateByLocation(
        @Param("minLat") double minLat,
        @Param("maxLat") double maxLat,
        @Param("minLng") double minLng,
        @Param("maxLng") double maxLng
    );
    
} 