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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
     * api_lawd_cd 테이블에서 시군구 코드(앞 5자리) Set으로 반환
     * @param dto 조회 조건
     * @return 중복 제거된 시군구 코드 Set
     */
    public Set<Integer> getUniqueRegionCodesAsSet(LawdCdRequestDTO dto) {
        List<LawdCdResponseDTO> lawdCodes = findAll(dto);
        return lawdCodes.stream()
                .map(LawdCdResponseDTO::getRegionCd)
                .filter(regionCd -> regionCd != null && regionCd.length() >= 5)
                .map(regionCd -> regionCd.substring(0, 5))
                .map(Integer::parseInt)
                .collect(Collectors.toSet());

    }

    /**
     * api_lawd_cd 테이블에서 시군구 코드(앞 5자리) 중복 제거된 List로 반환
     * @param dto 조회 조건
     * @return 중복 제거된 시군구 코드 List
     */
    public List<Integer> getUniqueRegionCodesAsList(LawdCdRequestDTO dto) {
        Set<Integer> uniqueRegionCodes = getUniqueRegionCodesAsSet(dto);
        return uniqueRegionCodes.stream()
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * 페이지네이션(offset, limit)을 사용하여 모든 고유 시군구코드 조회
     */
    public List<Integer> getAllUniqueRegionCodesWithPagination() {
        log.debug("LawdCodeService.getAllUniqueRegionCodesWithPagination() 실행=======");
        Set<Integer> allUniqueCodes = new HashSet<>();
        int currentOffset = 0;
        int limit = 1000; // 한 번에 조회건수 설정

        while (true) {
            LawdCdRequestDTO dto = new LawdCdRequestDTO();
            dto.setLimit(limit);
            dto.setOffset(currentOffset);
            List<Integer> pageResult = getUniqueRegionCodesAsList(dto);
            if (pageResult.isEmpty()) {
                break; // 더 이상 데이터가 없으면 중단
            }
            allUniqueCodes.addAll(pageResult);
            currentOffset += limit;
            log.debug("페이지 처리 완료 - offset: {}, 조회된 고유 코드 수: {}",
                    currentOffset - limit, pageResult.size());
        }

        List<Integer> result = allUniqueCodes.stream()
                .sorted()
                .collect(Collectors.toList());

        log.debug("모든 시군구 코드 조회 완료 - 총 {}개", result.size());
        return result;
    }
}
