package com.lighthouse.localinfo.mapper;

import com.lighthouse.localinfo.dto.WeatherDTO;
import com.lighthouse.localinfo.entity.Weather;
import org.springframework.stereotype.Component;

@Component
public class WeatherMapper {

    public WeatherDTO toDTO(Weather weather) {
        if (weather == null) {
            return null;
        }

        WeatherDTO weatherDTO = new WeatherDTO();
        weatherDTO.setRegionCd(weather.getRegionCd());
        weatherDTO.setRegion(weather.getRegion());
        weatherDTO.setGridX(weather.getGridX());
        weatherDTO.setGridY(weather.getGridY());
        weatherDTO.setLocataddNm(weather.getLocataddNm());
        weatherDTO.setSkyCondition(weather.getSkyCondition());
        weatherDTO.setTemperature(weather.getTemperature());
        weatherDTO.setMaxTemperature(weather.getMaxTemperature());
        weatherDTO.setMinTemperature(weather.getMinTemperature());

        return weatherDTO;
    }
}