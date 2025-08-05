package com.lighthouse.lawdCode.service;

import com.lighthouse.lawdCode.dto.LawdCdRequestDTO;
import com.lighthouse.lawdCode.dto.LawdCdResponseDTO;
import com.lighthouse.lawdCode.mapper.LawdCodeMapper;
import com.lighthouse.response.CustomException;
import com.lighthouse.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LawdCodeService {
    private final LawdCodeMapper mapper;
    public LawdCdResponseDTO findRegionByRegionCd(String regionCd) {
        LawdCdResponseDTO result = mapper.findByFullRegionCd(regionCd);
        if(result == null) {
            throw new CustomException(ErrorCode.WISHLIST_BAD_REQUEST);
        }
        return result;
    }
    public List<LawdCdResponseDTO> findAllRegionCdByPartialCd(LawdCdRequestDTO dto) {
        return mapper.findAllRegionByPartialCd(dto);
    }
}
