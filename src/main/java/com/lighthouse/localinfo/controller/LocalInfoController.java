package com.lighthouse.localinfo.controller;

import com.lighthouse.localinfo.dto.LocalInfoResponseDTO;
import com.lighthouse.localinfo.dto.ReverseGeocodeResponseDTO;
import com.lighthouse.localinfo.dto.WeatherDTO;
import com.lighthouse.localinfo.service.LocalInfoService;
import com.lighthouse.localinfo.service.ReverseGeocodeService;
import com.lighthouse.response.ApiResponse;
import com.lighthouse.response.ErrorCode;
import com.lighthouse.response.SuccessCode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/localinfo")
@RequiredArgsConstructor
@Api(tags = "지역 정보 API")
@CrossOrigin(origins = "http://localhost:5173")
public class LocalInfoController {

    private final LocalInfoService localInfoService;
    private final ReverseGeocodeService reverseGeocodeService; // NaverMapsService 주입


    /**
     * 키워드로 지역 목록을 검색하는 API
     *
     * @param keyword 검색 키워드
     * @return 검색된 지역 목록
     */
    @GetMapping("/search")
    @ApiOperation(value = "지역 검색", notes = "키워드로 지역을 검색합니다.")
    public ResponseEntity<ApiResponse<List<LocalInfoResponseDTO>>> searchRegions(
            @ApiParam(value = "검색 키워드", required = true, example = "강남")
            @RequestParam String keyword) {
        List<LocalInfoResponseDTO> regions = localInfoService.searchRegions(keyword);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.LOCALINFO_FETCH_SUCCESS, regions));
    }
    /**
     * 지역명으로 날씨 정보를 조회하는 API (이 API를 통해 날씨 API 호출을 확인합니다.)
     * @param regionCd 전체 지역 주소명
     * @return 날씨 정보
     */
    @GetMapping("/weather") // 이 엔드포인트를 확인하거나 추가해주세요!
    @ApiOperation(value = "법정동코드로 날씨 조회", notes = "법정동코드로 날씨 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<WeatherDTO>> getWeatherByRegionName(
            @ApiParam(value = "지역 법정동 코드", required = true, example = "1168010800") // 예시 코드
            @RequestParam("regionCd") String regionCd) { // 파라미터 이름과 타입 변경

        WeatherDTO weatherInfo = localInfoService.getWeatherByRegionCd(regionCd); // 서비스 메서드 호출 변경

        if (weatherInfo == null) {
            return new ResponseEntity<>(
                    ApiResponse.error(ErrorCode.REGION_NOT_FOUND),
                    ErrorCode.REGION_NOT_FOUND.getStatus()
            );
        }

        return ResponseEntity.ok(ApiResponse.success(SuccessCode.LOCALINFO_FETCH_SUCCESS, weatherInfo));
    }

    /**
     * [신규] 위도/경도를 이용하여 법정동 주소 정보를 조회하는 API (네이버 역지오코딩 API 사용)
     */
    @GetMapping("/reverse-geocode")
    @ApiOperation(value = "좌표로 법정동 주소 조회", notes = "위도/경도(latitude, longitude)를 이용하여 법정동 주소 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<ReverseGeocodeResponseDTO>> reverseGeocode(
            @ApiParam(value = "위도 (Latitude)", required = true, example = "37.5665")
            @RequestParam double latitude,
            @ApiParam(value = "경도 (Longitude)", required = true, example = "126.9780")
            @RequestParam double longitude) {

        // 수정: naverMapsService 인스턴스를 통해 메서드 호출 (non-static)
        // 수정: 파라미터 순서를 (경도, 위도)로 변경 (네이버 API 요구사항)
        ReverseGeocodeResponseDTO addressInfo = reverseGeocodeService.reverseGeocode(longitude, latitude);

        if (addressInfo == null) {
            return new ResponseEntity<>(
                    ApiResponse.error(ErrorCode.REGION_NOT_FOUND),
                    ErrorCode.REGION_NOT_FOUND.getStatus()
            );
        }

        return ResponseEntity.ok(ApiResponse.success(SuccessCode.LOCALINFO_FETCH_SUCCESS, addressInfo));
    }
}
