package com.lighthouse.estate.mapper;

import com.lighthouse.estate.dto.EstateDTO;
import com.lighthouse.estate.dto.EstateSalesDTO;
import com.lighthouse.estate.dto.EstateSquareDTO;
import com.lighthouse.estate.dto.BuildingInfoDto;
import com.lighthouse.estate.entity.Estate;
import com.lighthouse.estate.entity.EstateSales;
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
    Estate getEstateByLatLng(@Param("lat") double lat, @Param("lng") double lng);

    List<Estate> getEstateByElement(EstateDTO dto);
    
    /**
     * estate ID로 건물 정보 조회
     * @param estateId 건물 ID
     * @return 건물 정보
     */
    Estate getEstateById(@Param("estateId") Integer estateId);
    List<Estate> getAllEstateByLatLng(@Param("lat") double lat, @Param("lng") double lng);
    
    /**
     * 건물 ID 로 매매 정보 조회
     * @param estateId 건물 ID 
     * @return 매매 정보 리스트
     */
    List<EstateSales> getSalesByEstateId(@Param("estateId") Integer estateId);

    /**
     * 지도 사각형 영역 내 건물 정보 조회(다건 조회)
     */
    List<Estate> getEstateBySqaure(EstateSquareDTO dto);

    List<EstateSales> getEstateSalesByElement(EstateSalesDTO dto);
    
    /**
     * 지역코드와 읍면동명으로 건물 정보 목록 조회
     * @param regionCode 지역코드
     * @param dongName 읍면동명
     * @return 건물 정보 목록
     */
    List<BuildingInfoDto> getBuildingInfosByRegionCodeAndDongName(@Param("regionCode") String regionCode, @Param("dongName") String dongName);
    
} 