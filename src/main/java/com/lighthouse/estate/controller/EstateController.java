package com.lighthouse.estate.controller;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.NoSuchElementException;

import io.swagger.annotations.ApiOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.lighthouse.estate.dto.EstateDTO;
import com.lighthouse.estate.dto.EstateSalesDTO;
import com.lighthouse.estate.dto.EstateSquareDTO;
import com.lighthouse.estate.service.EstateService;
import com.lighthouse.response.ApiResponse;
import com.lighthouse.response.ErrorCode;
import com.lighthouse.response.SuccessCode;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/estate")
@Api(tags="Estate Info", description= "부동산 기본 정보와 매물 정보를 조건, 위경도, 주소, 범위로 조회")
public class EstateController {
  private final EstateService estateService;

  @GetMapping("")
  @ApiOperation(
          value = "조건별 부동산 정보 조회",
          notes = "부동산 조건을 입력하여 해당 조건에 맞는 부동산 기본 정보를 조회합니다."
  )
    public ResponseEntity<ApiResponse<List<EstateDTO>>> getEstateByElement(@ModelAttribute EstateDTO dto) {
    try {
      List<EstateDTO> estateList = estateService.getEstateByElement(dto);
      return ResponseEntity.ok().body(ApiResponse.success(SuccessCode.ESTATE_FETCH_SUCCESS, estateList));
    }
    catch(Exception e) {
      return ResponseEntity.ok().body(ApiResponse.error(ErrorCode.ESTATE_NOT_FOUND));
    }
  }


  //위경도로 estate 정보 찾기
  @GetMapping("/latlng")
  @ApiOperation(
          value = "위도/경도로 부동산 정보 조회",
          notes = "위도(lat)와 경도(lng) 좌표를 이용하여 해당 위치의 부동산 기본 정보를 조회합니다."

  )
  public ResponseEntity<ApiResponse<EstateDTO>> getEstateByLatLng(@RequestParam double lat, @RequestParam double lng) {
    try {
      EstateDTO dto = estateService.getEstateByLatLng(lat, lng);
      return ResponseEntity.ok().body(ApiResponse.success(SuccessCode.ESTATE_FETCH_SUCCESS, dto));
    } catch (NoSuchElementException e) {
        return ResponseEntity.ok().body(ApiResponse.error(ErrorCode.ESTATE_NOT_FOUND));
    }
  }

  //주소로 estate 정보 찾기
  @GetMapping("/address/{address}")
  @ApiOperation(
          value = "주소로 부동산 정보 조회",
          notes = "주소를 입력하여 해당 위치의 부동산 기본 정보를 조회합니다."

  )
  public ResponseEntity<ApiResponse<EstateDTO>> getEstateByAddress(@PathVariable String address) {
    try {
      //인코딩 변경
      String decodedAddress = URLDecoder.decode(address, StandardCharsets.UTF_8);

      EstateDTO dto = estateService.getEstateByAddress(decodedAddress);
      return ResponseEntity.ok().body(ApiResponse.success(SuccessCode.ESTATE_FETCH_SUCCESS, dto));
    }
    catch(NoSuchElementException e) {
      return ResponseEntity.ok().body(ApiResponse.error(ErrorCode.ESTATE_NOT_FOUND));
    }
  }

  //최소최대 위경도로 범위내의 estate 정보 찾기
  @GetMapping("/sqaure")
  @ApiOperation(
          value = "범위 내 부동산 정보 조회",
          notes = "최소/최대 위도(lat)와 경도(lng)를 지정하여 해당 범위 안의 모든 부동산 정보를 조회합니다."
  )
  public ResponseEntity<ApiResponse<List<EstateDTO>>> getEstateBySquare(@ModelAttribute EstateSquareDTO dto) {
    try {
      List<EstateDTO> estateList = estateService.getEstateBySqaure(dto);
      return ResponseEntity.ok().body(ApiResponse.success(SuccessCode.ESTATE_FETCH_SUCCESS, estateList));
    } catch (Exception e) {
      return ResponseEntity.ok().body(ApiResponse.error(ErrorCode.ESTATE_NOT_FOUND));
    }
  }

  //Estate Sales 정보 가져오기
  @GetMapping("/sales")
  @ApiOperation(
          value = "실거래가 정보 조회",
          notes = "부동산 실거래가 조건을 입력하여 해당 정보를 조회합니다."
  )
  public ResponseEntity<ApiResponse<List<EstateSalesDTO>>> getEstateSalesByElement(@ModelAttribute EstateSalesDTO dto) {
    try {
      List<EstateSalesDTO> estateSalesList = estateService.getEstateSalesByElement(dto);
      return ResponseEntity.ok().body(ApiResponse.success(SuccessCode.ESTATE_FETCH_SUCCESS, estateSalesList));
    }
    catch(Exception e) {
      return ResponseEntity.ok().body(ApiResponse.error(ErrorCode.ESTATE_NOT_FOUND));
    }
  }
}