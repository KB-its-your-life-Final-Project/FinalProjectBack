package com.lighthouse.wishlist.controller;

import com.lighthouse.response.ApiResponse;
import com.lighthouse.response.SuccessCode;
import com.lighthouse.security.util.JwtUtil;
import com.lighthouse.wishlist.dto.SearchHistoryRequestDTO;
import com.lighthouse.wishlist.dto.SearchHistoryResponseDTO;
import com.lighthouse.wishlist.service.SearchHistoryService;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist/history")
@RequiredArgsConstructor
@Slf4j
@Api(tags="검색 기록")
public class SearchHistoryController {
    private final JwtUtil jwtUtil;
    private final SearchHistoryService service;

    @PostMapping("")
    public ResponseEntity<ApiResponse<Void>> saveSearchHistory(@RequestBody SearchHistoryRequestDTO dto, @RequestHeader("Authorization") String authHeader){
        String token = authHeader.substring(7);
        Long memberId = Long.valueOf(jwtUtil.getSubjectFromToken(token));
        service.saveSearchHistory(memberId, dto.getKeyword());
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.SEARCH_HISTORY_SAVE_SUCCESS));
    }

    @GetMapping("")
    public ResponseEntity<ApiResponse<List<SearchHistoryResponseDTO>>> getSearchHistory(@RequestHeader("Authorization") String authHeader){
        String token = authHeader.substring(7);
        Long memberId = Long.valueOf(jwtUtil.getSubjectFromToken(token));
        List<SearchHistoryResponseDTO> result = service.findSearchHistoryByMemberId(memberId);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.SEARCH_HISTORY_SAVE_SUCCESS, result));
    }
}
