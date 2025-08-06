package com.lighthouse.search.controller;

import com.lighthouse.response.ApiResponse;
import com.lighthouse.response.SuccessCode;
import com.lighthouse.security.util.JwtUtil;
import com.lighthouse.search.dto.SearchHistoryRequestDTO;
import com.lighthouse.search.dto.SearchHistoryResponseDTO;
import com.lighthouse.search.service.SearchHistoryService;
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
    public ResponseEntity<ApiResponse<Void>> saveSearchHistory(@RequestBody SearchHistoryRequestDTO dto, @CookieValue("accessToken") String token){
        Long memberId = Long.valueOf(jwtUtil.getSubjectFromToken(token));
        service.saveSearchHistory(memberId, dto.getKeyword());
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.SEARCH_HISTORY_SAVE_SUCCESS));
    }

    @GetMapping("")
    public ResponseEntity<ApiResponse<List<SearchHistoryResponseDTO>>> getSearchHistory(@CookieValue("accessToken") String token){
        Long memberId = Long.valueOf(jwtUtil.getSubjectFromToken(token));
        List<SearchHistoryResponseDTO> result = service.findSearchHistoryByMemberId(memberId);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.SEARCH_HISTORY_SAVE_SUCCESS, result));
    }

}
