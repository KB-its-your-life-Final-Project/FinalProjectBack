package com.lighthouse.lawdCode.mapper;

import com.lighthouse.lawdCode.dto.LawdCdRequestDTO;
import com.lighthouse.lawdCode.dto.LawdCdResponseDTO;

import java.util.List;

public interface LawdCodeMapper {
    List<LawdCdResponseDTO> findAll(LawdCdRequestDTO dto);
    List<LawdCdResponseDTO> findAllRegionByPartialCd(LawdCdRequestDTO dto);
    LawdCdResponseDTO findByFullRegionCd(String regionCd);
}
