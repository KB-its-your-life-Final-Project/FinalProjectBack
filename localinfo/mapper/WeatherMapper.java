package com.lighthouse.localinfo.mapper;

import com.lighthouse.localinfo.dto.WeatherDTO;
import com.lighthouse.localinfo.vo.WeatherVO;
import org.springframework.stereotype.Component;

@Component
public class WeatherMapper {

    // WeatherVO를 WeatherDTO로 변환
    public WeatherDTO toDTO(WeatherVO weatherVO) {
        if (weatherVO == null) {
            return null;
        }

        WeatherDTO weatherDTO = new WeatherDTO();
        weatherDTO.setSkyCondition(weatherVO.getSkyCondition());
        weatherDTO.setTemperature(weatherVO.getTemperature());
        weatherDTO.setMaxTemperature(weatherVO.getMaxTemperature());
        weatherDTO.setMinTemperature(weatherVO.getMinTemperature());

        return weatherDTO;
    }


}