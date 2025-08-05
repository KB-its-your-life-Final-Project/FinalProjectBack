package com.lighthouse.localinfo.service;

import com.lighthouse.localinfo.dto.PopulationDTO; // DTO 임포트
import com.lighthouse.localinfo.mapper.PopulationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PopulationService {

    private final PopulationMapper populationMapper;

    /**
     * 특정 법정동 코드(regionCd)에 해당하는 인구 정보 조회
     * @param regionCd 조회할 법정동 코드
     * @return 해당 법정동의 인구 정보. 찾을 수 없으면 null 반환.
     */
    public PopulationDTO getPopulationByRegionCd(String regionCd) {
        log.info("법정동 코드 [{}] 에 대한 인구 정보 조회 시도.", regionCd);

        // --- 핵심 수정 부분 ---
        // populationMapper.findByRegionCd(regionCd)는 Optional<PopulationDTO>를 반환합니다.
        // .orElse(null)을 사용하여 Optional에서 값을 추출하거나, 값이 없으면 null을 반환합니다.
        PopulationDTO population = populationMapper.findByRegionCd(regionCd)
                .orElse(null); // <-- 여기에 .orElse(null) 추가

        if (population == null) { // 값이 null인지 확인
            log.warn("법정동 코드 [{}] 에 대한 인구 정보를 찾을 수 없습니다.", regionCd);
        } else {
            log.info("법정동 코드 [{}] 인구 정보 조회 성공: {}", regionCd, population);
        }

        return population; // DTO 객체 반환 (없으면 null)
    }
}