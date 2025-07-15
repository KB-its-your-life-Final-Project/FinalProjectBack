package com.lighthouse.member.controller;

import com.lighthouse.member.dto.ChangePasswordDTO;
import com.lighthouse.member.dto.MemberUpdateDTO;
import com.lighthouse.utils.UploadFiles;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.lighthouse.member.dto.MemberDTO;
import com.lighthouse.member.service.MemberService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
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

    @GetMapping("/checkusername/{username}") //아이디 중복 체크
    public ResponseEntity<Boolean> checkUsername(@PathVariable String username) {
        return ResponseEntity.ok().body(memberService.checkDuplicate(username));
    }

    @GetMapping("/{username}/profileimg")
    public void getAvatar(@PathVariable String username,
                          HttpServletResponse response) {
        String profileImgPath = "c:/upload/profileImg/" + username + ".png";
        File file = new File(profileImgPath);
        if (!file.exists()) {  // 아바타 등록이 없는 경우, 디폴트 아바타 이미지 사용
            file = new File("C:/upload/profileImg/unknown.png");
        }

        UploadFiles.downloadImage(response, file);
    }

    @PutMapping("/{username}")
    public ResponseEntity<MemberDTO> updateProfile(MemberUpdateDTO member) {
        return ResponseEntity.ok(memberService.update(member));
    }

    @PutMapping("/{username}/changepassword")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordDTO changePasswordDTO) {
        memberService.changePassword(changePasswordDTO);
        return ResponseEntity.ok().build();
    }
}
