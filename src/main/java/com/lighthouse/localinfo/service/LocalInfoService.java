package com.lighthouse.localinfo.service;

import com.lighthouse.localinfo.dto.LocalInfoResponseDTO;
import com.lighthouse.localinfo.entity.Weather;
import com.lighthouse.localinfo.mapper.LocalInfoMapper;
import com.lighthouse.localinfo.mapper.WeatherMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocalInfoService {

    private final LocalInfoMapper localInfoMapper;
    private final WeatherMapper weatherMapper;
    private final WeatherService weatherService; // WeatherService를 주입받음

    public List<LocalInfoResponseDTO> findAllRegions(){
        return localInfoMapper.findAllRegions();
    }

    /**
     * [수정됨] 날씨 조회의 모든 과정을 총괄하는 최종 메소드
     */
    public Weather getWeatherByRegionCd(String regionCd) {
        // 1. regionCd로 지역 정보(격자 좌표, 지역 이름 등) 조회
        LocalInfoResponseDTO regionInfo = localInfoMapper.findByRegionCd(regionCd).orElse(null);
        if (regionInfo == null) {
            log.warn("DB에 해당 regionCd({})가 없습니다.", regionCd);
            return null;
        }

        // 2. 해당 지역의 격자 좌표로 DB에서 날씨 정보 조회
        Weather dbWeather = weatherMapper.findByGrid(regionInfo.getGridX(), regionInfo.getGridY());

        // 3. DB에 유효한 날씨 정보가 있으면 바로 반환 (캐시 히트)
        if (dbWeather != null && dbWeather.getTemperature() != null) {
            log.info("DB에서 유효한 날씨 정보 조회 성공: {}", regionInfo.getLocataddNm());
            return dbWeather;
        }

        // 4. DB에 없거나 불완전하면 API 호출
        log.info("DB 날씨 정보가 없거나 불완전하여 API 호출: {}", regionInfo.getLocataddNm());
        Weather apiWeather = weatherService.fetchWeatherFromApi(regionInfo.getGridX(), regionInfo.getGridY());


            //  DB에 저장 또는 업데이트
            if (dbWeather == null) { // 데이터가 아예 없었으면 INSERT
                weatherMapper.insertWeather(apiWeather);
                log.info("새 날씨 정보를 DB에 저장했습니다: {}", regionInfo.getLocataddNm());
            } else { // 불완전한 데이터가 있었으면 UPDATE
                apiWeather.setId(dbWeather.getId()); // 기존 id 설정
                weatherMapper.updateWeather(apiWeather);
                log.info("기존 날씨 정보를 DB에서 업데이트했습니다: {}", regionInfo.getLocataddNm());
            }
            return apiWeather;
        }

}
