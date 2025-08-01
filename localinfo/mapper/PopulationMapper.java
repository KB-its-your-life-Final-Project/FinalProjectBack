package com.lighthouse.localinfo.mapper;

import com.lighthouse.localinfo.dto.PopulationDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface PopulationMapper {

    /**
     * 특정 법정동 코드(regionCd)에 해당하는 인구 정보 조회
     * @param regionCd 조회할 법정동 코드
     * @return 해당 법정동의 인구 정보 (Optional로 래핑)
     */
    Optional<PopulationDTO> findByRegionCd(@Param("regionCd") String regionCd);
}