package com.lighthouse.member.controller;

import com.lighthouse.member.dto.ChangePasswordDTO;
import com.lighthouse.member.dto.MemberRegisterDTO;
import com.lighthouse.member.dto.SocialLoginDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.lighthouse.member.dto.MemberDTO;
import com.lighthouse.member.service.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
    @GetMapping("/{username}")
    public ResponseEntity<MemberDTO> selectMemberByUsername(@PathVariable String username) {
        MemberDTO dto = memberService.selectMemberByUsername(username);
        if (dto == null) {
            return ResponseEntity.notFound().build(); // 명시적 처리
        }
        return ResponseEntity.ok().body(dto);
    }

    // 아이디 중복 확인
    @GetMapping("/checkusername/{username}")
    public ResponseEntity<Boolean> checkUsername(@PathVariable String username) {
        return ResponseEntity.ok().body(memberService.checkDuplicateUsername(username));
    }

    // 회원가입
    @PostMapping("")
    public ResponseEntity<MemberDTO> registerMember(@ModelAttribute MemberRegisterDTO dto) {
        return ResponseEntity.ok(memberService.registerMember(dto));
    }

    // 카카오/구글 로그인
    @PostMapping("/social")
    public ResponseEntity<?> socialLogin(@RequestBody SocialLoginDTO dto) {
        MemberDTO memberDTO = memberService.findOrRegisterByUsername(dto.getUsername());
        return ResponseEntity.ok(Map.of("success", true, "user", memberDTO));
    }

    // 사용자 비밀번호 수정
    @PutMapping("/{username}/changepassword")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordDTO dto) {
        memberService.changePassword(dto);
        return ResponseEntity.ok().build();
    }
}