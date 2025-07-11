package com.lighthouse.member.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.lighthouse.member.dto.MemberDTO;
import com.lighthouse.member.service.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
@Slf4j
public class MemberController {
    final MemberService memberService;
    @GetMapping("")
    public ResponseEntity<List<MemberDTO>> selectMembers() {
        return ResponseEntity.ok().body(memberService.selectMembers());
    }

    @GetMapping("/{username}")
    public ResponseEntity<MemberDTO> selectMemberByUSername(@PathVariable String username) {
        MemberDTO dto = memberService.selectMemberByUsername(username);
        if (dto == null) {
            return ResponseEntity.notFound().build(); // 명시적 처리
        }
        return ResponseEntity.ok().body(dto);
    }

}
