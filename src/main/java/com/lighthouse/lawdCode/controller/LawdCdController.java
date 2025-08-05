package com.lighthouse.lawdCode.controller;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.NoSuchElementException;
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
import com.lighthouse.lawdCode.dto.LawdCdRequestDTO;
import com.lighthouse.lawdCode.dto.LawdCdResponseDTO;
import com.lighthouse.lawdCode.service.LawdCodeService;
import com.lighthouse.response.ApiResponse;
import com.lighthouse.response.ErrorCode;
import com.lighthouse.response.SuccessCode;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/lawdCd")
@Api(tags="lawdCd 정보 조회")
public class LawdCdController {
  private final LawdCodeService lawdCodeService;

  @GetMapping("")
  public ResponseEntity<ApiResponse<List<LawdCdResponseDTO>>> getAllLawdCd(@ModelAttribute LawdCdRequestDTO dto) {
      List<LawdCdResponseDTO> result = lawdCodeService.findAll(dto);
      return ResponseEntity.ok().body(ApiResponse.success(SuccessCode.LAWDCD_FETCH_SUCCESS, result));
  }

}