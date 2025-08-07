package com.lighthouse.localinfo.mapper;

import com.lighthouse.localinfo.entity.Weather;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WeatherMapper {

    /**
     * sidoCd (예: "서울")를 기준으로 모든 지역의 날씨 정보를 조회
     * 스케줄러가 서울 지역만 업데이트할 때 사용됩니다.
     * @param sidoCd 시도 코드 문자열
     * @return Weather 엔티티 리스트
     */
    List<Weather> findBySidoCd(@Param("sidoCd") String sidoCd);

    /**
     * 격자 좌표(gridX, gridY)를 기준으로 가장 최신의 날씨 정보 하나를 조회
     * @param gridX 격자 X 좌표
     * @param gridY 격자 Y 좌표
     * @return Weather 엔티티
     */
    Weather findByGrid(@Param("gridX") int gridX, @Param("gridY") int gridY);

    /**
     * 새로운 날씨 정보를 DB에 삽입합니다.
     * DB에 데이터가 없어 API를 통해 새로 조회했을 때 사용
     * @param weather 저장할 Weather 엔티티
     */
    void insertWeather(Weather weather);

    /**
     * 기존 날씨 정보를 업데이트합니다.
     * 스케줄러가 주기적으로 날씨를 갱신할 때 사용
     * @param weather 업데이트할 Weather 엔티티
     */
    void updateWeather(Weather weather);

    /**
     * DB에 저장된 모든 지역의 날씨 정보를 조회
     * (기존 코드 호환성을 위해 유지)
     * @return Weather 엔티티 리스트
     */
    List<Weather> findByIdRange(@Param("startId") Long startId, @Param("endId") Long endId);
    List<Weather> findAll();
}
