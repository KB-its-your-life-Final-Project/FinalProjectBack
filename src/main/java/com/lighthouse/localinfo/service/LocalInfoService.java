package com.lighthouse.localinfo.service;

import com.lighthouse.localinfo.dto.LocalInfoResponseDTO;
import com.lighthouse.localinfo.dto.WeatherDTO;
import com.lighthouse.localinfo.mapper.LocalInfoMapper;
import com.lighthouse.localinfo.mapper.WeatherMapper;
import com.lighthouse.localinfo.vo.WeatherVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class LocalInfoService {

    private final LocalInfoMapper localInfoMapper;
    private final WeatherService weatherService;
    private final PopulationService populationService;
    private final WeatherMapper weatherMapper;

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
     * @return 최종적으로 조합된 날씨 정보 DTO
     */
    public WeatherDTO getWeatherByRegionCd(String regionCd) {
        // 1. 지역 좌표 조회 (regionCd 사용)
        LocalInfoResponseDTO regionInfo = getRegionByRegionCd(regionCd);
        if (regionInfo == null) {
            // 지역 정보가 DB에 없으면 null 반환
            return null;
        }

        // [개선된 위치] 격자 좌표 유효성 검사: API 호출 전에 확인
        // (LocalInfoResponseDTO의 gridX, gridY가 DB의 int null을 받을 경우 0으로 초기화될 수 있음)
        if (regionInfo.getGridX() == 0 && regionInfo.getGridY() == 0) {
            // log.warn("경고: 지역 [{}] ({}) 에 대한 격자 좌표가 0,0 입니다. 유효하지 않은 좌표입니다.", regionCd, regionInfo.getLocataddNm());
            // 유효하지 않은 좌표로 판단하고 null 반환 (또는 적절한 에러 처리)
            return null;
        }
        // 만약 LocalInfoResponseDTO의 gridX, gridY 필드가 Integer 타입이라면,
        // if (regionInfo.getGridX() == null || regionInfo.getGridY() == null) { ... } 로 체크해야 합니다.


        // 2. 날씨 정보 요청 (WeatherService는 순수 날씨 데이터인 VO를 반환)
        WeatherVO weatherDataVO = weatherService.getWeatherFromKMA(regionInfo.getGridX(), regionInfo.getGridY());
        if (weatherDataVO == null) {
            // 날씨 정보 조회에 실패하면 null 반환 (WeatherService 내부에서 이미 로그 남김)
            return null;
        }

        // 3. 주입받은 weatherMapper 객체를 사용하여 VO를 DTO로 변환합니다.
        WeatherDTO finalWeatherDTO = weatherMapper.toDTO(weatherDataVO);

        // 4. DTO에 부족한 지역 정보를 추가로 채워줍니다.
        finalWeatherDTO.setRegionCd(regionInfo.getRegionCd());
        finalWeatherDTO.setRegion(regionInfo.getRegion());
        finalWeatherDTO.setGridX(regionInfo.getGridX());
        finalWeatherDTO.setGridY(regionInfo.getGridY());
        finalWeatherDTO.setLocataddNm(regionInfo.getLocataddNm());

        return finalWeatherDTO;
    }
}