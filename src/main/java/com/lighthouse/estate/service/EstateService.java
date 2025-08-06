package com.lighthouse.estate.service;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import com.lighthouse.common.external.naver.NaverMapClient;
import com.lighthouse.common.geocoding.service.GeoCodingService;
import com.lighthouse.estate.converter.EstateDTOConverter;
import com.lighthouse.estate.dto.EstateDTO;
import com.lighthouse.estate.dto.EstateSalesDTO;
import com.lighthouse.estate.dto.EstateSquareDTO;
import com.lighthouse.estate.entity.Estate;
import com.lighthouse.estate.entity.EstateSales;
import com.lighthouse.estate.mapper.EstateMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EstateService {

    private final EstateMapper estateMapper;
    private final EstateDTOConverter estateDTOConverter;
    private final GeoCodingService geoCodingService;

    public List<EstateDTO> getEstateByElement(EstateDTO dto) {
        try {
            List<Estate> entities = estateMapper.getEstateByElement(dto);
            return estateDTOConverter.toDTOList(entities);
        }
        catch(Exception e) {
            throw e;
        }
    }

    //위경도로 건물 정보 가져오기 (단일)
    public EstateDTO getEstateByLatLng(double lat, double lng) {
        try {
            Estate estate = Optional.ofNullable(estateMapper.getEstateByLatLng(lat, lng))
                .orElseThrow(NoSuchElementException::new);
            return estateDTOConverter.toDTO(estate);
        }
        catch(Exception e) {
            throw e;
        }
    }

    //위경도로 건물 정보 가져오기 (리스트)
    public List<EstateDTO> getAllEstateByLatLng(double lat, double lng) {
        try {
            List<Estate> entities = estateMapper.getAllEstateByLatLng(lat, lng);
            return estateDTOConverter.toDTOList(entities);
        }
        catch(Exception e) {
            throw e;
        }
    }

    //주소로 해당 건물에 대한 정보 가져오기
    public EstateDTO getEstateByAddress(String address) {
        try{
            Map<String, Double> latlng = geoCodingService.getCoordinateFromAddress(address);
            EstateDTO estateDTO = getEstateByLatLng(latlng.get("lat"), latlng.get("lng"));
            return estateDTO;
        }
        catch(Exception e) {
            throw e;
        }
    }

    //위도경도 최대최소로 구하기
    public List<EstateDTO> getEstateBySqaure(EstateSquareDTO dto) {
        try {
            List<Estate> entities = estateMapper.getEstateBySqaure(dto);
            return estateDTOConverter.toDTOList(entities);
        }
        catch(Exception e) {
            throw e;
        }
    }

    public List<EstateSalesDTO> getEstateSalesByElement(EstateSalesDTO dto) {
        try {
            List<EstateSales> entities = estateMapper.getEstateSalesByElement(dto);
            return estateDTOConverter.toDTOSalesList(entities);
        }
        catch(Exception e) {
            throw e;
        }
    }
    
}