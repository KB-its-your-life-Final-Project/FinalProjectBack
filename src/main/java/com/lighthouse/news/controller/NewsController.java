package com.lighthouse.news.controller;


import com.lighthouse.news.dto.YouthContentDTO;
import com.lighthouse.news.service.YouthContentService;
import com.lighthouse.news.service.external.YouthContentClient;
import com.lighthouse.response.ApiResponse;
import com.lighthouse.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Slf4j
@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
//@CrossOrigin(origins = "${FRONT_ORIGIN}", allowCredentials = "true")
public class NewsController {
    private final YouthContentService youthContentService;
    final YouthContentClient youthContentClient;

    @GetMapping("")
    public ResponseEntity<ApiResponse<List<YouthContentDTO>>> getNews() {
        List<YouthContentDTO> dtos = youthContentClient.getYouthContents();
        return ResponseEntity.ok().body(ApiResponse.success(SuccessCode.NEWS_YOUTH_CONTENT_FETCH_SUCCESS, dtos));
    }

    @PostMapping("/sync")
    public ResponseEntity<?> syncYouthContents() {
        youthContentService.syncYouthContentsFromApi();
        return ResponseEntity.ok("청년 콘텐츠 동기화 완료");
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<YouthContentDTO>>> getUnreadContents(
            @RequestParam Long memberId,
            @RequestParam int page,
            @RequestParam int size
    ) {
        List<YouthContentDTO> contents = youthContentService.getUnreadContents(memberId, page, size);
        return ResponseEntity.ok().body(ApiResponse.success(SuccessCode.NEWS_YOUTH_CONTENT_getUnreadContents_SUCCESS, contents));
    }

    @PostMapping("/read")
    public ResponseEntity<Void> markAsRead(
            @RequestParam Long memberId,
            @RequestParam Long contentId
    ) {
        youthContentService.markContentAsRead(memberId, contentId);
        return ResponseEntity.ok().build();
    }
}
