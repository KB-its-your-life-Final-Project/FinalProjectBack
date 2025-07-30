package com.lighthouse.estate.controller;

import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.lighthouse.estate.dto.EstateDTO;
import com.lighthouse.estate.service.EstateService;
import com.lighthouse.response.ApiResponse;
import com.lighthouse.response.ErrorCode;
import com.lighthouse.response.SuccessCode;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/estate")
public class EstateController {
  private final EstateService estateService;

  @GetMapping("")
  public ResponseEntity<ApiResponse<EstateDTO>> getEstateByLatLng(@RequestParam double lat, @RequestParam double lng) {
    try {
      EstateDTO dto = estateService.getEstateByLatLng(lat, lng);
      return ResponseEntity.ok().body(ApiResponse.success(SuccessCode.MEMBER_FETCH_SUCCESS, dto));
    } catch (NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ErrorCode.MEMBER_NOT_FOUND));
    }
  }
}
