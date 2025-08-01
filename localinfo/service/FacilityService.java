// src/main/java/com/lighthouse/localinfo/service/FacilityService.java (이전 FacilityServiceImpl.java였던 파일)
package com.lighthouse.localinfo.service;

import com.lighthouse.localinfo.dto.FacilityDTO; // DTO 임포트
import com.lighthouse.localinfo.mapper.FacilityMapper; // 매퍼 임포트
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FacilityService {

    private final FacilityMapper facilityMapper; // 매퍼 주입

    /**
     * 특정 법정동 코드(regionCd)에 해당하는 편의시설들의 개수 정보.
     * @param regionCd 조회할 법정동 코드
     * @return 해당 법정동의 편의시설 개수 정보 (Optional로 래핑). 없으면 Optional.empty()
     */
    public Optional<FacilityDTO> getFacilityCountsByRegionCd(String regionCd) {
        log.info("법정동 코드 [{}] 에 대한 편의시설 정보 조회 시도.", regionCd);

        Optional<FacilityDTO> facilityInfo = facilityMapper.findFacilityDetailsByRegionCd(regionCd); // 매퍼 호출

        if (facilityInfo.isEmpty()) {
            log.warn("법정동 코드 [{}] 에 대한 편의시설 정보를 찾을 수 없습니다.", regionCd);
        } else {
            log.info("법정동 코드 [{}] 편의시설 정보 조회 성공: {}", regionCd, facilityInfo.get());
        }
        return facilityInfo;
    }
}