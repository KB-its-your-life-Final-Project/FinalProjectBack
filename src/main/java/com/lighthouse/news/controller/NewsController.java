package com.lighthouse.news.controller;


import com.lighthouse.news.dto.NewsDTO;
import com.lighthouse.news.service.NewsService;
import com.lighthouse.response.ApiResponse;
import com.lighthouse.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@Slf4j
@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
//@CrossOrigin(origins = "${FRONT_ORIGIN}", allowCredentials = "true")
public class NewsController {
    final NewsService newsService;

    @GetMapping("")
    public ResponseEntity<ApiResponse<List<NewsDTO>>> getNews() {
        List<NewsDTO> dtos = newsService.getNews();
        return ResponseEntity.ok().body(ApiResponse.success(SuccessCode.NEWS_YOUTH_CONTENT_FETCH_SUCCESS, dtos));
    }
}
