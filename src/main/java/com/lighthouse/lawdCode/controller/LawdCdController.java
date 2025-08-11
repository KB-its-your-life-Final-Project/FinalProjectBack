package com.lighthouse.lawdCode.controller;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.NoSuchElementException;

import com.lighthouse.lawdCode.dto.*;
import com.lighthouse.lawdCode.service.SelectAddressService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.lighthouse.estate.dto.EstateDTO;
import com.lighthouse.estate.service.EstateService;
import com.lighthouse.lawdCode.service.LawdCodeService;
import com.lighthouse.response.ApiResponse;
import com.lighthouse.response.ErrorCode;
import com.lighthouse.response.SuccessCode;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/lawdCd")
@Api(tags="lawdCd 정보")
public class LawdCdController {
    private final LawdCodeService lawdCodeService;
    private final SelectAddressService selectAddressService;
    
    @GetMapping("")
    public ResponseEntity<ApiResponse<List<LawdCdResponseDTO>>> getAllLawdCd(@ModelAttribute LawdCdRequestDTO dto) {
        List<LawdCdResponseDTO> result = lawdCodeService.findAll(dto);
        return ResponseEntity.ok().body(ApiResponse.success(SuccessCode.LAWDCD_FETCH_SUCCESS, result));
    }

    // 시/도 목록 조회
    @GetMapping("/sido")
    @ApiOperation(
        value = "시/도 목록 조회",
        notes = "전국의 시/도 목록을 조회합니다. " +
                "서울, 부산, 대구, 인천, 광주, 대전, 울산, 세종, 경기, 강원, 충북, 충남, 전북, 전남, 경북, 경남, 제주를 포함합니다."
    )
    public ResponseEntity<ApiResponse<List<SidoDto>>> getSidoList() {
        try {
            List<SidoDto> sidoList = selectAddressService.getSidoList();
            return ResponseEntity.ok().body(ApiResponse.success(SuccessCode.LAWDCD_FETCH_SUCCESS, sidoList));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(ErrorCode.SERVER_NOT_RESPONDING));
        }
    }

    // 시/군/구 목록 조회 (시/도별)
    @GetMapping("/sigugun/{sidoCd}")
    @ApiOperation(
        value = "시/군/구 목록 조회",
        notes = "선택된 시/도의 시/군/구 목록을 조회합니다. " +
                "예: 서울(11) 선택 시 종로구, 중구, 용산구 등의 구 목록을 반환합니다."
    )
    public ResponseEntity<ApiResponse<List<SigugunDto>>> getSigugunList(
        @ApiParam(value = "시/도 코드 (예: 서울=11, 부산=26, 대구=27)", required = true, example = "11") 
        @PathVariable String sidoCd
    ) {
        try {
            List<SigugunDto> sigugunList = selectAddressService.getSigugunList(sidoCd);
            return ResponseEntity.ok().body(ApiResponse.success(SuccessCode.LAWDCD_FETCH_SUCCESS, sigugunList));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(ErrorCode.SERVER_NOT_RESPONDING));
        }
    }

    // 읍/면/동 목록 조회 (시/군/구별)
    @GetMapping("/dong/{sidoCd}/{sggCd}")
    @ApiOperation(
        value = "읍/면/동 목록 조회",
        notes = "선택된 시/군/구의 읍/면/동 목록을 조회합니다. " +
                "예: 종로구(110) 선택 시 원서동, 훈정동, 묘동 등의 동 목록을 반환합니다."
    )
    public ResponseEntity<ApiResponse<List<DongDto>>> getDongList(
        @ApiParam(value = "시/도 코드 (예: 서울=11, 부산=26)", required = true, example = "11")
        @PathVariable String sidoCd,
        @ApiParam(value = "시/군/구 코드 (예: 종로구=110, 중구=140)", required = true, example = "110")
        @PathVariable String sggCd
    ) {
        try {
            List<DongDto> dongList = selectAddressService.getDongList(sidoCd, sggCd);
            return ResponseEntity.ok().body(ApiResponse.success(SuccessCode.LAWDCD_FETCH_SUCCESS, dongList));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(ErrorCode.SERVER_NOT_RESPONDING));
        }
    }
    
    // 건물명 목록 조회
    @GetMapping("/buildings")
    @ApiOperation(
        value = "건물명 목록 조회",
        notes = "지역코드와 읍면동명을 이용하여 해당 지역의 건물명 목록을 조회합니다. " +
                "예: 지역코드 11110, 읍면동명 '목동'으로 검색 시 해당 지역의 모든 건물명을 반환합니다."
    )
    public ResponseEntity<ApiResponse<BuildingResponseDto>> getBuildingList(
        @ApiParam(value = "5자리 지역코드 (sido_cd + sgg_cd)", required = true, example = "11110") 
        @RequestParam String regionCode,
        @ApiParam(value = "읍면동 한글 이름", required = true, example = "목동") 
        @RequestParam String dongName
    ) {
        try {
            BuildingResponseDto buildingList = selectAddressService.getBuildingList(regionCode, dongName);
            return ResponseEntity.ok().body(ApiResponse.success(SuccessCode.LAWDCD_FETCH_SUCCESS, buildingList));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(ErrorCode.SERVER_NOT_RESPONDING));
        }
    }
}