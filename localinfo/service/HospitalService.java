package com.lighthouse.localinfo.service;

import com.lighthouse.localinfo.dto.HospitalDTO;
import com.lighthouse.localinfo.mapper.HospitalMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class HospitalService {

    private final HospitalMapper hospitalMapper; // 매퍼 주입

    /**
     * 특정 법정동 코드(regionCd)에 해당하는 병원 정보.
     * @param regionCd 조회할 법정동 코드
     * @return 해당 법정동의 병원 개수 정보 (Optional로 래핑). 없으면 Optional.empty()
     */
    public Optional<HospitalDTO> getHospitalCountsByRegionCd(String regionCd) {
        log.info("법정동 코드 [{}] 에 대한 편의시설 정보 조회 시도.", regionCd);

        Optional<HospitalDTO> hospitalInfo = hospitalMapper.findHospitalDetailsByRegionCd(regionCd); // 매퍼 호출

        if (hospitalInfo.isEmpty()) {
            log.warn("법정동 코드 [{}] 에 대한 편의시설 정보를 찾을 수 없습니다.", regionCd);
        } else {
            log.info("법정동 코드 [{}] 편의시설 정보 조회 성공: {}", regionCd, hospitalInfo.get());
        }
        return hospitalInfo;
    }
}