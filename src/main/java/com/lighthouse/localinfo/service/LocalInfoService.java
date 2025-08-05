package com.lighthouse.localinfo.service;

import com.lighthouse.localinfo.dto.LocalInfoResponseDTO;
import com.lighthouse.localinfo.mapper.LocalInfoMapper;
import com.lighthouse.localinfo.mapper.WeatherMapper;
import com.lighthouse.localinfo.entity.Weather;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class LocalInfoService {

    private final LocalInfoMapper localInfoMapper;
    private final WeatherService weatherService;

    /**
     * 키워드를 포함하는 지역 목록을 검색합니다.
     */
    public List<LocalInfoResponseDTO> searchRegions(String keyword) {
        return localInfoMapper.searchByKeyword(keyword);
    }

    /**
     * 법정동 코드(regionCd)로 특정 지역의 상세 정보를 조회
     */
    public LocalInfoResponseDTO getRegionByRegionCd(String regionCd) {
        return localInfoMapper.findByRegionCd(regionCd)
                .orElse(null);
    }

    /**
     * [수정] 지역코드(regionCd)로 날씨 정보를 조회하는 통합 메서드
     * @param regionCd 조회할 지역의 법정동 코드
     * @return 최종적으로 조합된 날씨 정보 Entity
     */
    public Weather getWeatherByRegionCd(String regionCd) {
        // 1. 지역 좌표 조회 (regionCd 사용)
        LocalInfoResponseDTO regionInfo = getRegionByRegionCd(regionCd);
        if (regionInfo == null) {
            // 지역 정보가 DB에 없으면 null 반환
            return null;
        }

        if (regionInfo.getGridX() == 0 && regionInfo.getGridY() == 0) {
            // log.warn("경고: 지역 [{}] ({}) 에 대한 격자 좌표가 0,0 입니다. 유효하지 않은 좌표입니다.", regionCd, regionInfo.getLocataddNm());
            // 유효하지 않은 좌표로 판단하고 null 반환 (또는 적절한 에러 처리)
            return null;
        }
        // 2. 날씨 정보 요청 (WeatherService는 순수 날씨 데이터인 Entity를 반환)
        Weather weatherDataEntity = weatherService.getWeatherFromKMA(regionInfo.getGridX(), regionInfo.getGridY());
        if (weatherDataEntity == null) {
            // 날씨 정보 조회에 실패하면 null 반환 (WeatherService 내부에서 이미 로그 남김)
            return null;
        }
        weatherDataEntity.setRegion(regionInfo.getRegion());    // 지역명만 설정
        weatherDataEntity.setGridX(regionInfo.getGridX());
        weatherDataEntity.setGridY(regionInfo.getGridY());

        return weatherDataEntity;
    }
}