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
import com.lighthouse.estate.dto.BuildingInfoDto;
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
            log.warn("Failed to getEstateByElement: message={}", e.getMessage());
            return null;
        }
    }

    //위경도로 건물 정보 가져오기 (단일)
    public EstateDTO getEstateByLatLng(double lat, double lng) {
        try {
            // 위도/경도가 0.0인 경우는 유효하지 않은 값으로 처리
            if (lat == 0.0 && lng == 0.0) {
                log.warn("유효하지 않은 위도/경도 값: lat={}, lng={}", lat, lng);
                return null;
            }
            
            Estate estate = estateMapper.getEstateByLatLng(lat, lng);
            if (estate == null) {
                log.warn("해당 위도/경도에 대한 부동산 정보를 찾을 수 없습니다: lat={}, lng={}", lat, lng);
                return null;
            }
            return estateDTOConverter.toDTO(estate);
        }
        catch(Exception e) {
            log.warn("Failed to getEstateByLatLng: message={}", e.getMessage());
            return null;
        }
    }

    //ID로 건물 정보 가져오기
    public EstateDTO getEstateById(Integer estateId) {
        try {
            Estate estate = Optional.ofNullable(estateMapper.getEstateById(estateId))
                .orElseThrow(NoSuchElementException::new);
            return estateDTOConverter.toDTO(estate);
        }
        catch(Exception e) {
            log.warn("Failed to getEstateById: message={}", e.getMessage());
            return null;
        }
    }
    //위경도로 건물 정보 가져오기 - 예외 없는 버전.
    public EstateDTO getEstateByLatLngWithNull(double lat, double lng) {
        return Optional.ofNullable(estateMapper.getEstateByLatLng(lat, lng))
                .map(estateDTOConverter::toDTO)
                .orElse(null);
    }



    //위경도로 건물 정보 가져오기 (리스트)
    public List<EstateDTO> getAllEstateByLatLng(double lat, double lng) {
        try {
            List<Estate> entities = estateMapper.getAllEstateByLatLng(lat, lng);
            return estateDTOConverter.toDTOList(entities);
        }
        catch(Exception e) {
            log.warn("Failed to getAllEstateByLatLng: message={}", e.getMessage());
            return null;
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
            log.warn("Failed to getEstateByAddress: message={}", e.getMessage());
            return null;
        }
    }

    //위도경도 최대최소로 구하기
    public List<EstateDTO> getEstateBySqaure(EstateSquareDTO dto) {
        try {
            List<Estate> entities = estateMapper.getEstateBySqaure(dto);
            return estateDTOConverter.toDTOList(entities);
        }
        catch(Exception e) {
            log.warn("Failed to getEstateBySqaure: message={}", e.getMessage());
            return null;
        }
    }

    public List<EstateSalesDTO> getEstateSalesByElement(EstateSalesDTO dto) {
        try {
            List<EstateSales> entities = estateMapper.getEstateSalesByElement(dto);
            return estateDTOConverter.toDTOSalesList(entities);
        }
        catch(Exception e) {
            log.warn("Failed to getEstateSalesByElement: message={}", e.getMessage());
            return null;
        }
    }

    public List<EstateDTO> getNearyByLatLng (double lat, double lng) {
        try {
            List<Estate> entites = estateMapper.getNearyByEstate(lat, lng);
            return estateDTOConverter.toDTOList(entites);
        }
        catch(Exception e) {
            log.warn("Failed to getNearyByLatLng: message={}", e.getMessage());
            return null;
        }
    }

    // 지역코드와 읍면동명으로 건물 정보 목록 조회
    public List<BuildingInfoDto> getBuildingInfosByRegionCodeAndDongName(String regionCode, String dongName) {
        try {
            log.info("건물 정보 목록 조회 시작 - regionCode: {}, dongName: {}", regionCode, dongName);
            List<BuildingInfoDto> buildingInfos = estateMapper.getBuildingInfosByRegionCodeAndDongName(regionCode, dongName);
            log.info("건물 정보 목록 조회 완료: {}개", buildingInfos != null ? buildingInfos.size() : 0);
            return buildingInfos;
        } catch (Exception e) {
            log.warn("Failed to getBuildingInfosByRegionCodeAndDongName: message={}", e.getMessage());
            return null;
        }
    }
}