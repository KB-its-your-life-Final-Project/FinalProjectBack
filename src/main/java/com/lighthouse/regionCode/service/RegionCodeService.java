package com.lighthouse.regionCode.service;

import com.lighthouse.regionCode.dto.RegionCdRequestDTO;
import com.lighthouse.regionCode.dto.RegionCdResponseDTO;
import com.lighthouse.regionCode.mapper.RegionCodeMapper;
import com.lighthouse.response.CustomException;
import com.lighthouse.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RegionCodeService {
    private final RegionCodeMapper mapper;
    public RegionCdResponseDTO findRegionByRegionCd(String regionCd) {
        RegionCdResponseDTO result = mapper.findByFullRegionCd(regionCd);
        if(result == null) {
            throw new CustomException(ErrorCode.WISHLIST_BAD_REQUEST);
        }
        return result;
    }
    public List<RegionCdResponseDTO> findAllRegionCdByPartialCd(RegionCdRequestDTO dto) {
        return mapper.findAllRegionByPartialCd(dto);
    }
}
