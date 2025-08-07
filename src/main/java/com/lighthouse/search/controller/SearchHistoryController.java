package com.lighthouse.search.controller;

import com.lighthouse.response.ApiResponse;
import com.lighthouse.response.SuccessCode;
import com.lighthouse.search.dto.SearchHistoryDTO;
import com.lighthouse.security.util.JwtUtil;
import com.lighthouse.search.service.SearchHistoryService;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@RestController
@RequestMapping("/api/search/history")
@RequiredArgsConstructor
@Slf4j
@Api(tags="검색 기록")
public class SearchHistoryController {
    private final JwtUtil jwtUtil;
    private final SearchHistoryService service;

    @PostMapping("")
    public ResponseEntity<ApiResponse<Void>> saveSearchHistory(@RequestBody SearchHistoryDTO dto, @ApiIgnore @CookieValue("accessToken") String token){
        Long memberId = Long.valueOf(jwtUtil.getSubjectFromToken(token));
        dto.setMemberId(memberId);
        service.saveSearchHistory(dto);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.SEARCH_HISTORY_SAVE_SUCCESS));
    }

    @GetMapping("")
    public ResponseEntity<ApiResponse<List<SearchHistoryDTO>>> getSearchHistory(
            @ApiIgnore @CookieValue("accessToken") String token,
            @ModelAttribute SearchHistoryDTO dto
    ) {
        Long memberId = Long.valueOf(jwtUtil.getSubjectFromToken(token));
        dto.setMemberId(memberId);
        List<SearchHistoryDTO> result = service.findAllSearchHistoryByCondition(dto);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.SEARCH_HISTORY_GETLIST_SUCCESS, result));
    }

}
