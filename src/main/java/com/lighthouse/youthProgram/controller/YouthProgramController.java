package com.lighthouse.youthProgram.controller;


import com.lighthouse.youthProgram.dto.YouthProgramDTO;
import com.lighthouse.youthProgram.service.YouthProgramService;
import com.lighthouse.youthProgram.service.external.YouthProgramClient;
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
@RequestMapping("/api/youthprogram")
@RequiredArgsConstructor
@CrossOrigin(origins = "${FRONT_ORIGIN}", allowCredentials = "true")
public class YouthProgramController {
    private final YouthProgramService youthProgramService;
    final YouthProgramClient youthProgramClient;

    @GetMapping("")
    public ResponseEntity<ApiResponse<List<YouthProgramDTO>>> getYouthPrograms() {
        List<YouthProgramDTO> dtos = youthProgramClient.getYouthPrograms();
        return ResponseEntity.ok().body(ApiResponse.success(SuccessCode.NEWS_YOUTH_PROGRAM_FETCH_SUCCESS, dtos));
    }

    @PostMapping("/sync")
    public ResponseEntity<?> syncYouthPrograms() {
        youthProgramService.syncYouthProgramsFromApi();
        return ResponseEntity.ok("청년 프로그램 동기화 완료");
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<YouthProgramDTO>>> getUnreadPrograms(
            @RequestParam Long memberId,
            @RequestParam int page,
            @RequestParam int size
    ) {
        List<YouthProgramDTO> programsList = youthProgramService.getUnreadPrograms(memberId, page, size);
        return ResponseEntity.ok().body(ApiResponse.success(SuccessCode.NEWS_YOUTH_PROGRAM_GET_UNREAD_SUCCESS, programsList));
    }

    @PostMapping("/read")
    public ResponseEntity<Void> markAsRead(
            @RequestParam Long memberId,
            @RequestParam Long contentId
    ) {
        youthProgramService.markProgramAsRead(memberId, contentId);
        return ResponseEntity.ok().build();
    }
}
