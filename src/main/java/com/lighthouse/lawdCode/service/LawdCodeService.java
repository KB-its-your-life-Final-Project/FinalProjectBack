package com.lighthouse.lawdCode.service;

import com.lighthouse.lawdCode.converter.LawdCdDTOConverter;
import com.lighthouse.lawdCode.dto.LawdCdRequestDTO;
import com.lighthouse.lawdCode.dto.LawdCdResponseDTO;
import com.lighthouse.lawdCode.mapper.LawdCodeMapper;
import com.lighthouse.response.CustomException;
import com.lighthouse.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
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

    /**
     * LawdCdResponseDTO 리스트에서 고유한 시군구코드 추출
     * @param lawdCodes 시군구코드 응답 DTO 리스트
     * @return 중복 제거된 시군구코드 Set
     */
    public Set<Integer> getAllUniqueRegionCodesAsSet(List<LawdCdResponseDTO> lawdCodes) {
        return lawdCodes.stream()
                .map(LawdCdResponseDTO::getRegionCd)
                .filter(regionCd -> regionCd != null && regionCd.length() >= 5)
                .map(regionCd -> regionCd.substring(0, 5))
                .map(Integer::parseInt)
                .collect(Collectors.toSet());
    }

    /**
     * 페이지네이션(offset, limit)을 사용하여 모든 고유 시군구코드 조회
     * @return 정렬 + 중복 제거된 모든 시군구코드 List
     */
    public List<Integer> getAllUniqueRegionCodesWithPagination() {
        log.debug("LawdCodeService.getAllUniqueRegionCodesWithPagination() 실행=======");
        Set<Integer> allUniqueCodes = new HashSet<>();
        int currentOffset = 0;
        int limit = 1000;
        while (true) {
            LawdCdRequestDTO dto = LawdCdRequestDTO.createForPagination(currentOffset, limit);
            List<LawdCdResponseDTO> pageResult = findAll(dto);
            if (pageResult.isEmpty()) {
                break;
            }
            Set<Integer> pageUniqueCodes = getAllUniqueRegionCodesAsSet(pageResult);
            allUniqueCodes.addAll(pageUniqueCodes);
            currentOffset += limit;
            log.debug("✅ 페이지 처리 완료 - offset: {}, 조회된 고유 코드 수: {}",
                    currentOffset - limit, pageUniqueCodes.size());
        }

        List<Integer> result = new ArrayList<>(new TreeSet<>(allUniqueCodes)); // 리스트화, 정렬
        log.debug("✅ 모든 시군구 코드 조회 완료 - 총 {}개", result.size());
        return result;
    }
}
