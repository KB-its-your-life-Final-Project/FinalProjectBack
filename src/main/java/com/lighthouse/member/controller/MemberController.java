package com.lighthouse.member.controller;

import com.lighthouse.member.dto.EmailRegisterDTO;
import com.lighthouse.member.dto.KakaoRegisterDTO;
import com.lighthouse.security.vo.CustomUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.lighthouse.member.dto.MemberDTO;
import com.lighthouse.member.service.MemberService;
import com.lighthouse.response.ApiResponse;
import com.lighthouse.response.ErrorCode;
import com.lighthouse.response.SuccessCode;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
@Slf4j
public class  MemberController {
    final MemberService memberService;
    // 로그인 여부
    @GetMapping("/loggedin")
    public ResponseEntity<MemberDTO> getLoginUser(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        CustomUser user = (CustomUser) auth.getPrincipal();
        return ResponseEntity.ok(MemberDTO.of(user.getMember()));
    }

    // 모든 사용자 정보 조회
    @GetMapping("")
    public ResponseEntity<List<MemberDTO>> selectMembers() {
        return ResponseEntity.ok().body(memberService.selectMembers());
    }

    // 아이디로 사용자 정보 조회
    @GetMapping("/{id}")
    public ResponseEntity<MemberDTO> selectMemberById(@PathVariable int id) {
        MemberDTO dto = memberService.selectMemberById(id);
        if (dto == null) {
            return ResponseEntity.notFound().build(); // 명시적 처리

    // //모든 사용자 리스트
    // @GetMapping("/")
    // public ResponseEntity<ApiResponse<List<MemberDTO>>> findAll() {
    //     List<MemberDTO> dtos = memberService.findAll();
    //     return ResponseEntity.ok()
    //             .body(ApiResponse.success(SuccessCode.MEMBER_FETCH_SUCCESS, dtos));
    // }

    // //사용자 아이디로 찾기
    // @GetMapping("/{id}")
    // public ResponseEntity<ApiResponse<MemberDTO>> findById(@PathVariable Number id) {
    //     try {
    //         MemberDTO dto = memberService.findById(id);
    //         return ResponseEntity.ok()
    //                 .body(ApiResponse.success(SuccessCode.MEMBER_FETCH_SUCCESS, dto));
    //     } catch (NoSuchElementException e) {
    //         return ResponseEntity.status(HttpStatus.NOT_FOUND)
    //                 .body(ApiResponse.error(ErrorCode.MEMBER_NOT_FOUND));
    //     }
    // }

    // 이메일 중복 확인
    @GetMapping("/checkemail/{email}")
    public ResponseEntity<Boolean> checkEmail(@PathVariable String email) {
        return ResponseEntity.ok().body(memberService.checkDuplicateEmail(email));
    }

    // 이메일 회원가입
    @PostMapping("/register/email")
    public ResponseEntity<MemberDTO> registerMemberByEmail(@ModelAttribute EmailRegisterDTO dto, HttpServletRequest req) {
        log.info("email: {}", dto.getEmail());
        return ResponseEntity.ok(memberService.registerMemberByEmail(dto, req));
    }

    // 카카오 회원가입 (기존 사용자는 로그인)
    @PostMapping("/register/kakao")
    public ResponseEntity<MemberDTO> registerOrLoginMemberByKakao(@RequestBody KakaoRegisterDTO dto, HttpServletRequest req, HttpServletResponse resp) {
        log.info("카카오톡으로 로그인 POST 요청==========");
        log.info("KakaoRegisterDTO: {}", dto);
        log.info("HttpServletRequest: {}", req);
        log.info("HttpServletResponse: {}", resp);
        return ResponseEntity.ok(memberService.registerOrLoginMemberByKakaoCode(dto, req, resp));
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<Boolean> logoutUser(HttpServletResponse resp) {
        return ResponseEntity.ok(memberService.logoutUser(resp));
    }
}