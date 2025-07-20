package com.lighthouse.member.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.lighthouse.member.dto.MemberDTO;
import com.lighthouse.member.service.MemberService;
import com.lighthouse.response.ApiResponse;
import com.lighthouse.response.ErrorCode;
import com.lighthouse.response.SuccessCode;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
@Slf4j
public class MemberController {
    final MemberService memberService;

    //모든 사용자 리스트
    @GetMapping("/")
    public ResponseEntity<ApiResponse<List<MemberDTO>>> findAll() {
        List<MemberDTO> dtos = memberService.findAll();
        return ResponseEntity.ok()
                .body(ApiResponse.success(SuccessCode.MEMBER_FETCH_SUCCESS, dtos));
    }

    //사용자 아이디로 찾기
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MemberDTO>> findById(@PathVariable Number id) {
        try {
            MemberDTO dto = memberService.findById(id);
            return ResponseEntity.ok()
                    .body(ApiResponse.success(SuccessCode.MEMBER_FETCH_SUCCESS, dto));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(ErrorCode.MEMBER_NOT_FOUND));
        }
    }

}
