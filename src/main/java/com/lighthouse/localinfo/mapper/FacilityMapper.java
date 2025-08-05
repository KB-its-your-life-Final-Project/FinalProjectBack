package com.lighthouse.localinfo.mapper;

import com.lighthouse.localinfo.dto.FacilityDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface FacilityMapper {
    /**
=     * 여러 편의시설 카운트를 포함한 통합 DTO
     *
     * @param regionCd 조회할 법정동 코드
     * @return 해당 법정동의 편의시설 개수 정보 (Optional로 래핑)
     */
    Optional<FacilityDTO> findFacilityDetailsByRegionCd(@Param("regionCd") String regionCd); // 메서드명 변경
}