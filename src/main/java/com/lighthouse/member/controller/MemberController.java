package com.lighthouse.member.controller;

import com.lighthouse.member.dto.ChangePasswordDTO;
import com.lighthouse.member.dto.EmailRegisterDTO;
import com.lighthouse.member.dto.KakaoRegisterDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.lighthouse.member.dto.MemberDTO;
import com.lighthouse.member.service.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
@Slf4j
public class  MemberController {
    final MemberService memberService;
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
        }
        return ResponseEntity.ok().body(dto);
    }

    // 이메일 중복 확인
    @GetMapping("/checkemail/{email}")
    public ResponseEntity<Boolean> checkEmail(@PathVariable String email) {
        return ResponseEntity.ok().body(memberService.checkDuplicateEmail(email));
    }

    // 이메일 회원가입
    @PostMapping("/register/email")
    public ResponseEntity<MemberDTO> registerMemberByEmail(@ModelAttribute EmailRegisterDTO dto, HttpServletRequest req) {
        return ResponseEntity.ok(memberService.registerMemberByEmail(dto, req));
    }

    // 카카오 회원가입 (기존 사용자는 로그인)
    @PostMapping("/register/kakao")
    public ResponseEntity<MemberDTO> registerOrLoginMemberByKakao(@RequestBody KakaoRegisterDTO dto, HttpServletRequest req) {
        return ResponseEntity.ok(memberService.registerOrLoginMemberByKakaoCode(dto, req));
    }

    // 사용자 비밀번호 수정
    @PutMapping("/{id}/changepassword")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordDTO dto) {
        memberService.changePassword(dto);
        return ResponseEntity.ok().build();
    }
}