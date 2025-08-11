package com.lighthouse.localinfo.mapper;

import com.lighthouse.localinfo.dto.LocalInfoResponseDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface LocalInfoMapper {
    /**
     * @param regionCd 조회할 전체 지역 주소명
     * @return 지역 상세 정보 (grid, xy 좌표 포함)를 담은 Optional 객체
     */
    Optional<LocalInfoResponseDTO> findByRegionCd(@Param("regionCd") String regionCd); // 추가

    /**
     *
     * @return 법정동 테이블에서 모든 읍면동 지역 정보를 담은 객체
     */
    List<LocalInfoResponseDTO> findAllRegions();
}