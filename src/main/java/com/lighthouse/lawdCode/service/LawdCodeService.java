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
    private final LawdCodeMapper lawdCodeMapper;

    public List<LawdCdResponseDTO> findAll(LawdCdRequestDTO dto) {
        if(dto.getOffset() == null) {
            dto.setOffset(0);
        }
        return lawdCodeMapper.findAll(dto);
    }

    public LawdCdResponseDTO findRegionByRegionCd(String regionCd) {
        LawdCdResponseDTO result = lawdCodeMapper.findByFullRegionCd(regionCd);
        if(result == null) {
            throw new CustomException(ErrorCode.LAWDCD_TARGET_NOT_FOUND);
        }
        return result;
    }
    public List<LawdCdResponseDTO> findAllRegionCdByPartialCd(LawdCdRequestDTO dto) {
        return lawdCodeMapper.findAllRegionByPartialCd(dto);
    }
}
