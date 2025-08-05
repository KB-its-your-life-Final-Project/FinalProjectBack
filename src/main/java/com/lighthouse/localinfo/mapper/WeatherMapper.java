package com.lighthouse.localinfo.mapper;

import com.lighthouse.localinfo.entity.Weather;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WeatherMapper {

    // 모든 지역 조회
    List<Weather> findAll();

    // 지역 코드로 조회
    Weather findByGrid(@Param("gridX") int gridX, @Param("gridY") int gridY);

    // 날씨 데이터 업데이트
    void updateWeather(Weather weather);
}