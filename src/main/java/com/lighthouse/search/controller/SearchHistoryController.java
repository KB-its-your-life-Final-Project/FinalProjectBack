package com.lighthouse.search.controller;

import com.lighthouse.response.ApiResponse;
import com.lighthouse.response.SuccessCode;
import com.lighthouse.search.dto.SearchHistoryDTO;
import com.lighthouse.security.util.JwtUtil;
import com.lighthouse.search.service.SearchHistoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
@Api(tags="Search History",description="로그인 회원의 검색 기록을 저장")
public class SearchHistoryController {
    private final JwtUtil jwtUtil;
    private final SearchHistoryService service;

    @PostMapping("")
    @ApiOperation(
            value = "검색 기록 저장 요청",
            notes = "매물 검색 또는 지역 검색 내용을 저장합니다."
    )
    public ResponseEntity<ApiResponse<Void>> saveSearchHistory(@RequestBody SearchHistoryDTO dto, @ApiIgnore @CookieValue("accessToken") String token){
        Long memberId = Long.valueOf(jwtUtil.getSubjectFromToken(token));
        dto.setMemberId(memberId);
        service.saveSearchHistory(dto);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.SEARCH_HISTORY_SAVE_SUCCESS));
    }

    @GetMapping("")
    @ApiOperation(
            value = "검색 기록 조회",
            notes = "설정한 조건에 맞는 유저의 검색 기록을 모두 가져옵니다."
    )
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
