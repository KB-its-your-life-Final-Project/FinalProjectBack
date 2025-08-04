package com.lighthouse.regionCode.mapper;

import com.lighthouse.regionCode.dto.RegionCdRequestDTO;
import com.lighthouse.regionCode.dto.RegionCdResponseDTO;

import java.util.List;

public interface RegionCodeMapper {
    List<RegionCdResponseDTO> findAllRegionByPartialCd(RegionCdRequestDTO dto);
    RegionCdResponseDTO findByFullRegionCd(String regionCd);
}
