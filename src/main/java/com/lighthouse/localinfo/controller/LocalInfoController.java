package com.lighthouse.localinfo.controller;

import com.lighthouse.localinfo.dto.*;
import com.lighthouse.localinfo.entity.Weather;
import com.lighthouse.localinfo.service.*;
import com.lighthouse.response.ApiResponse;
import com.lighthouse.response.ErrorCode;
import com.lighthouse.response.SuccessCode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/localinfo")
@CrossOrigin(origins = "${FRONT_ORIGIN}")
@RequiredArgsConstructor
@Api(tags = "Local Info", description = "지역 정보, 인구, 편의시설, 병원, 안전도, 날씨 조회 API")
public class LocalInfoController {

    private final LocalInfoService localInfoService;
    private final ReverseGeocodeService reverseGeocodeService;
    private final PopulationService populationService;
    private final FacilityService facilityService;
    private final HospitalService hospitalService;
    private final SafetyService safetyService;

    /**
     * 모든 검색 가능 지역 목록을 반환하는 API
     */
    @ApiOperation(
            value = "검색 가능 지역 목록 조회"
    )
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<LocalInfoResponseDTO>>> getAllRegions() {
        List<LocalInfoResponseDTO> regions = localInfoService.findAllRegions();
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.LOCALINFO_FETCH_SUCCESS, regions));
    }


    /**
     * 위도/경도를 이용하여 법정동 주소 정보를 조회하는 API (네이버 역지오코딩 API 사용)
     */
    @GetMapping("/reverse-geocode")
    @ApiOperation(
            value = "위경도 기반 주소 조회"
    )
    public ResponseEntity<ApiResponse<ReverseGeocodeResponseDTO>> reverseGeocode(
            @RequestParam double latitude,
            @RequestParam double longitude) {

        ReverseGeocodeResponseDTO addressInfo = reverseGeocodeService.reverseGeocode(longitude, latitude);

        if (addressInfo == null) {
            return new ResponseEntity<>(
                    ApiResponse.error(ErrorCode.REGION_NOT_FOUND),
                    ErrorCode.REGION_NOT_FOUND.getStatus()
            );
        }

        return ResponseEntity.ok(ApiResponse.success(SuccessCode.LOCALINFO_FETCH_SUCCESS, addressInfo));
    }

    @GetMapping("/population")
    @ApiOperation(
            value = "인구 정보 조회"
    )
    public ResponseEntity<ApiResponse<PopulationDTO>> getPopulationByRegionCd(
            @RequestParam("regionCd") String regionCd) {

        PopulationDTO populationInfo = populationService.getPopulationByRegionCd(regionCd);

        if (populationInfo == null) {
            return new ResponseEntity<>(
                    ApiResponse.error(ErrorCode.REGION_NOT_FOUND),
                    ErrorCode.REGION_NOT_FOUND.getStatus()
            );
        }

        return ResponseEntity.ok(ApiResponse.success(SuccessCode.LOCALINFO_FETCH_SUCCESS, populationInfo));
    }

    /**
     * 법정동코드로 편의시설 (예: 자전거 대수) 정보를 조회하는 API
     */
    @GetMapping("/facilities-count")
    @ApiOperation(
            value = "편의시설 개수 조회"
    )
    public ResponseEntity<ApiResponse<FacilityDTO>> getFacilityCountsByRegionCd(
            @RequestParam("regionCd") String regionCd) {

        Optional<FacilityDTO> facilityInfoOptional = facilityService.getFacilityCountsByRegionCd(regionCd);

        if (facilityInfoOptional == null || facilityInfoOptional.isEmpty()) {
            return new ResponseEntity<>(
                    ApiResponse.error(ErrorCode.REGION_NOT_FOUND),
                    ErrorCode.REGION_NOT_FOUND.getStatus()
            );
        }

        return ResponseEntity.ok(ApiResponse.success(SuccessCode.LOCALINFO_FETCH_SUCCESS, facilityInfoOptional.get()));
    }

    @GetMapping("/hospitals-count")
    @ApiOperation(
            value = "병원 개수 조회"
    )
    public ResponseEntity<ApiResponse<HospitalDTO>> getHospitalCountsByRegionCd(
            @RequestParam("regionCd") String regionCd) {

        Optional<HospitalDTO> hospitalInfoOptional = hospitalService.getHospitalCountsByRegionCd(regionCd);

        if (hospitalInfoOptional == null || hospitalInfoOptional.isEmpty()) {
            return new ResponseEntity<>(
                    ApiResponse.error(ErrorCode.REGION_NOT_FOUND),
                    ErrorCode.REGION_NOT_FOUND.getStatus()
            );
        }

        return ResponseEntity.ok(ApiResponse.success(SuccessCode.LOCALINFO_FETCH_SUCCESS, hospitalInfoOptional.get()));
    }

    @GetMapping("/safety-count")
    @ApiOperation(
            value = "안전도 조회"
    )
    public ResponseEntity<ApiResponse<SafetyDTO>> getSafetyCountsByRegionCd(
            @RequestParam("regionCd") String regionCd) {

        Optional<SafetyDTO> safetyInfoOptional = safetyService.getSafetyCountsByRegionCd(regionCd);

        if (safetyInfoOptional == null || safetyInfoOptional.isEmpty()) {
            return new ResponseEntity<>(
                    ApiResponse.error(ErrorCode.REGION_NOT_FOUND),
                    ErrorCode.REGION_NOT_FOUND.getStatus()
            );
        }

        return ResponseEntity.ok(ApiResponse.success(SuccessCode.LOCALINFO_FETCH_SUCCESS, safetyInfoOptional.get()));
    }
    @GetMapping("/weather")
    @ApiOperation(
            value = "날씨 정보 조회"
    )
    public ResponseEntity<ApiResponse<Weather>> getWeatherByRegionCd(
            @RequestParam("regionCd") String regionCd) {

        try {
            Weather weather = localInfoService.getWeatherByRegionCd(regionCd);

            if (weather == null) {
                return new ResponseEntity<>(
                        ApiResponse.error(ErrorCode.WEATHER_NOT_FOUND),
                        ErrorCode.WEATHER_NOT_FOUND.getStatus()
                );
            }

            // 날씨 데이터를 포함하여 반환
            return ResponseEntity.ok(ApiResponse.success(SuccessCode.WEATHER_FETCH_SUCCESS, weather));

        } catch (Exception e) {

            return new ResponseEntity<>(
                    ApiResponse.error(ErrorCode.SERVER_NOT_RESPONDING),
                    ErrorCode.SERVER_NOT_RESPONDING.getStatus()
            );
        }
    }
}