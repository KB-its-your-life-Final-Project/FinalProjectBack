package com.lighthouse.member.controller;

import com.lighthouse.member.dto.*;
import com.lighthouse.member.service.MemberService;
import com.lighthouse.response.ApiResponse;
import com.lighthouse.response.ErrorCode;
import com.lighthouse.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import static com.lighthouse.member.util.ValidateUtil.isValidImgType;

@Slf4j
@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
@CrossOrigin(origins = "${FRONT_ORIGIN}", allowCredentials = "true")
public class MemberController {
    final MemberService memberService;

    // 모든 회원 정보 조회
    @GetMapping("")
    public ResponseEntity<ApiResponse<List<MemberResponseDTO>>> findAllMembers() {
        List<MemberResponseDTO> dtos = memberService.findAllMembers();
        return ResponseEntity.ok().body(ApiResponse.success(SuccessCode.MEMBER_FETCH_SUCCESS, dtos));
    }

    // 아이디로 회원 정보 조회
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MemberResponseDTO>> findMemberById(@PathVariable int id) {
        try {
            MemberResponseDTO dto = memberService.findMemberById(id);
            return ResponseEntity.ok().body(ApiResponse.success(SuccessCode.MEMBER_FETCH_SUCCESS, dto));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ErrorCode.MEMBER_NOT_FOUND));
        }
    }

    // 로그인된 회원 정보 조회
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MemberResponseDTO>> checkLoginStatus(HttpServletRequest req, HttpServletResponse resp) {
        try {
            MemberResponseDTO memberDto = memberService.findMemberLoggedIn(req, resp);
            if (memberDto != null) {
                return ResponseEntity.ok().body(ApiResponse.success(SuccessCode.MEMBER_FETCH_SUCCESS, memberDto));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(ErrorCode.UNAUTHORIZED));
            }
        } catch (NoSuchElementException e) {
            log.error("회원 정보 조회 실패", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(ErrorCode.UNAUTHORIZED));
        }
    }

    // 이메일 중복 확인
    @GetMapping("/checkemail/{email}")
    public ResponseEntity<ApiResponse<Boolean>> checkDuplicateEmail(@PathVariable String email) {
        boolean isDuplicate = memberService.checkDuplicateEmail(email);
        log.info("isDuplicate: {}", isDuplicate);
        if (!isDuplicate) {
            return ResponseEntity.ok().body(ApiResponse.success(SuccessCode.MEMBER_USABLE_EMAIL_SUCCESS, isDuplicate));
        }
        return ResponseEntity.ok().body(ApiResponse.error(ErrorCode.DUPLICATE_EMAIL, isDuplicate));
    }

    // 비밀번호 검증
    @PostMapping("/verify/pwd") // Get은 @RequestBody 사용 불가
    public ResponseEntity<ApiResponse<Boolean>> verifyPwd(@RequestBody VerifyPwdRequestDTO verifyPwdReqDto) {
        boolean isVerified = memberService.isVerifiedPwd(verifyPwdReqDto);
        log.info("isVerified: {}", isVerified);
        if (isVerified) {
            return ResponseEntity.ok().body(ApiResponse.success(SuccessCode.MEMBER_PASSWORD_VERIFICATION_SUCCESS, true));
        }
        return ResponseEntity.ok().body(ApiResponse.error(ErrorCode.INVALID_PASSWORD, false));
    }
    // 인증 코드 발송
//    @PostMapping("/sendcode")
//    public ResponseEntity<ApiResponse<Boolean>> sendVerificationCode(@RequestParam String email) {
//        if (!memberService.isValidEmail(email)) {
//            return ResponseEntity.ok().body(ApiResponse.error(ErrorCode.INVALID_EMAIL_FORMAT));
//        }
//        try {
//            memberService.sendVerificationCode(email);
//            return ResponseEntity.ok().body(ApiResponse.success(SuccessCode.MEMBER_VERIFICATION_CODE_SENT, true));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(ErrorCode.EMAIL_SEND_FAIL));
//        }
//    }

    // 인증 코드 검사
//    @PostMapping("/verifycode")
//    public ResponseEntity<ApiResponse<Boolean>> verifyCode(@RequestParam String email, @RequestParam String verificationCode) {
//        boolean isVerifiedCode = memberService.verifyCode(email, verificationCode);
//        if (isVerifiedCode) {
//            return ResponseEntity.ok().body(ApiResponse.success(SuccessCode.MEMBER_VERIFICATION_CODE_SUCCESS, true));
//        }
//        return ResponseEntity.ok().body(ApiResponse.error(ErrorCode.INVALID_VERIFICATION_CODE, false));
//    }

    // 이메일 회원가입
    @PostMapping("/register/email")
    public ResponseEntity<ApiResponse<MemberResponseDTO>> registerByEmail(@ModelAttribute RegisterEmailRequestDTO registerReqDto, HttpServletRequest req) {
        // 이메일 형식 유효성 검사
        if (!memberService.isValidEmail(registerReqDto.getEmail())) {
            return ResponseEntity.ok().body(ApiResponse.error(ErrorCode.INVALID_EMAIL_FORMAT));
        }
        // 비밀번호 형식 유효성 검사
//        if (!memberService.isValidPassword(registerDto.getPassword1())) {
//            return ResponseEntity.ok().body(ApiResponse.error(ErrorCode.INVALID_PASSWORD_FORMAT));
//        }
        // 비밀번호1과 비밀번호2 일치 여부 검사
        if (!Objects.equals(registerReqDto.getPassword1(), registerReqDto.getPassword2())) {
            return ResponseEntity.ok().body(ApiResponse.error(ErrorCode.INVALID_PASSWORD_CHECK));
        }
        try {
            log.info("email: {}", registerReqDto.getEmail());
            MemberResponseDTO userDto = memberService.registerByEmail(registerReqDto, req);
            return ResponseEntity.ok().body(ApiResponse.success(SuccessCode.MEMBER_REGISTER_SUCCESS, userDto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(ErrorCode.MEMBER_REGISTER_FAIL));
        }
    }

    // 회원 탈퇴
    @PostMapping("/unregister")
    public ResponseEntity<ApiResponse<MemberResponseDTO>> unregister(HttpServletRequest req, HttpServletResponse resp) {
        try {
            MemberResponseDTO memberDto = memberService.findMemberLoggedIn(req, resp);
            if (memberDto == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(ErrorCode.UNAUTHORIZED));
            }
            MemberResponseDTO unregisteredMemberDto = memberService.unregister(memberDto);
            return ResponseEntity.ok().body(ApiResponse.success(SuccessCode.MEMBER_UNREGISTER_SUCCESS, unregisteredMemberDto));
        } catch (Exception e) {
            log.error("회원 탈퇴 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(ErrorCode.MEMBER_UNREGISTER_FAIL));
        }
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<MemberResponseDTO>> login(@RequestBody LoginRequestDTO loginReqDto, HttpServletRequest req, HttpServletResponse resp) {
        log.info("로그인 POST 요청==========");
        log.info("LoginRequestDTO: {}", loginReqDto);
        log.info("createType = " + loginReqDto.getCreatedType());
        // 이메일 로그인
        if (loginReqDto.getCreatedType() == 1) {
            String email = loginReqDto.getEmail();
            // 이메일 형식 유효성 검사
            if (!memberService.isValidEmail(email)) {
                return ResponseEntity.ok().body(ApiResponse.error(ErrorCode.INVALID_EMAIL_FORMAT));
            }
            // 회원가입 여부 검사
            boolean isMember = memberService.checkDuplicateEmail(email);
            if (isMember) {
                try {
                    MemberResponseDTO memberDto = memberService.loginByEmail(loginReqDto, req, resp);
                    if (memberDto == null) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(ErrorCode.INVALID_PASSWORD));
                    }
                    return ResponseEntity.ok().body(ApiResponse.success(SuccessCode.MEMBER_LOGIN_EMAIL_SUCCESS, memberDto));
                } catch (Exception e) {
                    log.error("이메일 로그인 실패", e);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(ErrorCode.EMAIL_LOGIN_FAIL));
                }
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(ErrorCode.MEMBER_NOT_FOUND));
            // 카카오 로그인
        } else if (loginReqDto.getCreatedType() == 2) {
            try {
                MemberResponseDTO memberDto = memberService.loginOrRegisterByKakaoCode(loginReqDto, req, resp);
                return ResponseEntity.ok().body(ApiResponse.success(SuccessCode.MEMBER_KAKAO_REGISTER_LOGIN_SUCCESS, memberDto));
            } catch (Exception e) {
                log.error("카카오 회원가입/로그인 실패", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(ErrorCode.MEMBER_REGISTER_FAIL));
            }
            // 구글 로그인
        } else if (loginReqDto.getCreatedType() == 3) {
            try {
                MemberResponseDTO memberDto = memberService.loginOrRegisterByGoogleCode(loginReqDto, req, resp);
                return ResponseEntity.ok().body(ApiResponse.success(SuccessCode.MEMBER_GOOGLE_REGISTER_LOGIN_SUCCESS, memberDto));
            } catch (Exception e) {
                log.error("구글 회원가입/로그인 실패", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(ErrorCode.MEMBER_REGISTER_FAIL));
            }
            // 지원하지 않는 로그인 타입
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(ErrorCode.INVALID_LOGIN_TYPE));
        }
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Boolean>> logout(HttpServletResponse resp) {
        try {
            boolean isTokenRemoved = memberService.logout(resp);
            return ResponseEntity.ok().body(ApiResponse.success(SuccessCode.MEMBER_LOGOUT_SUCCESS, isTokenRemoved));
        } catch (Exception e) {
            log.error("로그아웃 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(ErrorCode.MEMBER_LOGOUT_FAIL));
        }
    }

    // 회원 정보 (이름, 비밀번호) 변경
    @PutMapping("/change")
    public ResponseEntity<ApiResponse<MemberResponseDTO>> changeMemberInfo(@RequestBody ChangeRequestDTO changeReqDto, HttpServletRequest req, HttpServletResponse resp) {
        log.info("회원 정보 변경 PUT 요청==========");
        try {
            MemberResponseDTO memberDto = memberService.findMemberLoggedIn(req, resp);
            if (memberDto == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(ErrorCode.UNAUTHORIZED));
            }
            int changeType = changeReqDto.getChangeType();
            log.info("ChangeRequestDTO: {}", changeReqDto);
            log.info("changeType = " + changeType);
            if (changeType == 1) {
                String newName = changeReqDto.getName();
                try {
                    MemberResponseDTO updatedMemberDto = memberService.changeMemberInfo(changeType, newName, memberDto);
                    if (updatedMemberDto != null) {
                        return ResponseEntity.ok().body(ApiResponse.success(SuccessCode.MEMBER_UPDATE_NAME_SUCCESS, updatedMemberDto));
                    } else {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(ErrorCode.INVALID_NAME_INPUT));
                    }
                } catch (Exception e) {
                    log.error("회원 정보 변경 실패. 요청자: {}", changeReqDto.getName(), e);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(ErrorCode.MEMBER_UPDATE_FAIL));
                }
                // 비밀번호 변경
            } else if (changeType == 2) {
                String newPwd = changeReqDto.getPwd();
                try {
                    MemberResponseDTO updatedMemberDto = memberService.changeMemberInfo(changeType, newPwd, memberDto);
                    if (updatedMemberDto != null) {
                        return ResponseEntity.ok().body(ApiResponse.success(SuccessCode.MEMBER_UPDATE_PWD_SUCCESS, updatedMemberDto));
                    } else {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(ErrorCode.INVALID_PASSWORD_INPUT));
                    }
                } catch (Exception e) {
                    log.error("회원 정보 변경 실패. 요청자: {}", changeReqDto.getName(), e);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(ErrorCode.MEMBER_UPDATE_FAIL));
                }
                // 지원하지 않는 변경 타입
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(ErrorCode.INVALID_UPDATE_TYPE));
            }
        } catch (Exception e) {
            log.error("회원 탈퇴 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(ErrorCode.MEMBER_UPDATE_FAIL));
        }
    }

    // 회원 프로필사진 변경
    @PostMapping("/profileimg")
    public ResponseEntity<ApiResponse<MemberResponseDTO>> uploadProfileImage(@RequestParam("file") MultipartFile file, HttpServletRequest req, HttpServletResponse resp) {
        log.info("회원 프로필사진 변경 POST 요청==========");
        log.info("=== CORS Headers Debug ===");
        log.info("Origin: {}", req.getHeader("Origin"));
        log.info("Content-Type: {}", req.getContentType());
        log.info("Method: {}", req.getMethod());
        try {
            MemberResponseDTO memberDto = memberService.findMemberLoggedIn(req, resp);
            if (memberDto == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(ErrorCode.UNAUTHORIZED));
            }
            log.info("파일명: {}, 크기: {}bytes", file.getOriginalFilename(), file.getSize());
            // 파일 유무
            if (file.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(ErrorCode.INVALID_FILE_EMPTY));
            }
            // 파일 크기 검사 (5MB 초과 시 에러)
            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(ErrorCode.INVALID_FILE_SIZE));
            }
            // 파일 형식 검사
            String contentType = file.getContentType();
            if (!isValidImgType(contentType)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(ErrorCode.INVALID_FILE_TYPE));
            }
            MemberResponseDTO updatedMember = memberService.uploadProfileImg(memberDto, file);
            if (updatedMember != null) {
                log.info("프로필 이미지 업로드 성공 - 회원ID: {}", updatedMember.getId());
                return ResponseEntity.ok()
                        .body(ApiResponse.success(SuccessCode.MEMBER_UPDATE_PROFILEIMAGE_SUCCESS, updatedMember));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(ApiResponse.error(ErrorCode.MEMBER_UPDATE_FAIL));
            }
        } catch (Exception e) {
            log.error("프로필 이미지 업로드 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(ErrorCode.MEMBER_UPDATE_FAIL));
        }
    }

    // 회원 프로필사진 삭제 (기본 이미지로 변경)
    @DeleteMapping("/profileimg")
    public ResponseEntity<ApiResponse<MemberResponseDTO>> deleteProfileImage(HttpServletRequest req, HttpServletResponse resp) {
        log.info("회원 프로필사진 삭제 DELETE 요청==========");
        try {
            MemberResponseDTO memberDto = memberService.findMemberLoggedIn(req, resp);
            if (memberDto == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(ErrorCode.UNAUTHORIZED));
            }
            MemberResponseDTO updatedMember = memberService.deleteProfileImg(memberDto);
            if (updatedMember != null) {
                log.info("프로필사진 삭제 성공, 요청자: {}",updatedMember.getName());
                return ResponseEntity.ok().body(ApiResponse.success(SuccessCode.MEMBER_UPDATE_PROFILEIMAGE_SUCCESS, updatedMember));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(ErrorCode.UNAUTHORIZED));
            }
        } catch (Exception e) {
            log.error("프로필사진 삭제 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(ErrorCode.MEMBER_UPDATE_FAIL));
        }
    }
}