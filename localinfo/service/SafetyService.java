package com.lighthouse.localinfo.service;

import com.lighthouse.localinfo.dto.SafatyDTO;
import com.lighthouse.localinfo.mapper.SafetyMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SafetyService {

    private final SafetyMapper safetyMapper; // 매퍼 주입

    public Optional<SafatyDTO> getSafetyCountsByRegionCd(String regionCd) {
        log.info("법정동 코드 [{}] 에 대한 정보 조회 시도.", regionCd);

        Optional<SafatyDTO> safetyInfo = safetyMapper.findSafetyDetailsByRegionCd(regionCd); // 매퍼 호출

        if (safetyInfo.isEmpty()) {
            log.warn("법정동 코드 [{}] 에 대한 정보를 찾을 수 없습니다.", regionCd);
        } else {
            log.info("법정동 코드 [{}] 편의시설 정보 조회 성공: {}", regionCd, safetyInfo.get());
        }
        return safetyInfo;
    }
}{
}
