package com.lighthouse.member.controller;

import com.lighthouse.member.dto.RegisterEmailDTO;
import com.lighthouse.member.dto.RegisterGoogleDTO;
import com.lighthouse.member.dto.RegisterKakaoDTO;
import com.lighthouse.security.dto.LoginEmailDTO;
import com.lighthouse.security.vo.CustomUser;
import com.lighthouse.member.dto.MemberDTO;
import com.lighthouse.member.service.MemberService;
import com.lighthouse.response.ApiResponse;
import com.lighthouse.response.ErrorCode;
import com.lighthouse.response.SuccessCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    // 로그인 여부 확인
    @GetMapping("/loggedin")
    public ResponseEntity<ApiResponse<MemberDTO>> findLoggedinUser(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        CustomUser user = (CustomUser) auth.getPrincipal();
        MemberDTO dto = MemberDTO.toAdmin(user.getUser());
        return ResponseEntity.ok().body(ApiResponse.success(SuccessCode.MEMBER_LOGIN_SUCCESS, dto));
    }

    // 모든 사용자 정보 조회
    @GetMapping("")
    public ResponseEntity<ApiResponse<List<MemberDTO>>> findAllUsers() {
        List<MemberDTO> dtos = memberService.findAllMembers();
        return ResponseEntity.ok().body(ApiResponse.success(SuccessCode.MEMBER_FETCH_SUCCESS, dtos));
    }

    // 아이디로 사용자 정보 조회
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MemberDTO>> findMemberById(@PathVariable int id) {
        try {
            MemberDTO dto = memberService.findMemberById(id);
            return ResponseEntity.ok().body(ApiResponse.success(SuccessCode.MEMBER_FETCH_SUCCESS, dto));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ErrorCode.MEMBER_NOT_FOUND));
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
    // 인증 코드 발송
    @PostMapping("/sendcode")
    public ResponseEntity<ApiResponse<Boolean>> sendVerificationCode(@RequestParam String email) {
        if (!memberService.isValidEmail(email)) {
            return ResponseEntity.ok().body(ApiResponse.error(ErrorCode.INVALID_EMAIL_FORMAT));
        }
        try {
            memberService.sendVerificationCode(email);
            return ResponseEntity.ok().body(ApiResponse.success(SuccessCode.MEMBER_VERIFICATION_CODE_SENT, true));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(ErrorCode.EMAIL_SEND_FAIL));
        }
    }

    // 인증 코드 검사
    @PostMapping("/verifycode")
    public ResponseEntity<ApiResponse<Boolean>> verifyCode(@RequestParam String email, @RequestParam String verificationCode) {
        boolean isVerifiedCode = memberService.verifyCode(email, verificationCode);
        if (isVerifiedCode) {
            return ResponseEntity.ok().body(ApiResponse.success(SuccessCode.MEMBER_VERIFICATION_CODE_SUCCESS, true));
        }
        return ResponseEntity.ok().body(ApiResponse.error(ErrorCode.INVALID_VERRIFICATION_CODE, false));
    }

    // 이메일 회원가입
    @PostMapping("/register/email")
    public ResponseEntity<ApiResponse<MemberDTO>> registerMemberByEmail(@ModelAttribute RegisterEmailDTO registerDto, HttpServletRequest req) {
        // 이메일 형식 유효성 검사
        if (!memberService.isValidEmail(registerDto.getEmail())) {
            return ResponseEntity.ok().body(ApiResponse.error(ErrorCode.INVALID_EMAIL_FORMAT));
        }

        // 비밀번호 형식 유효성 검사
        if (!memberService.isValidPassword(registerDto.getPassword())) {
            return ResponseEntity.ok().body(ApiResponse.error(ErrorCode.INVALID_PASSWORD_FORMAT));
        }
        try {
            log.info("email: {}", registerDto.getEmail());
            MemberDTO userDto = memberService.registerMemberByEmail(registerDto, req);
            return ResponseEntity.ok().body(ApiResponse.success(SuccessCode.MEMBER_REGISTER_SUCCESS, userDto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(ErrorCode.MEMBER_REGISTER_FAIL));
        }
    }

    // 카카오 회원가입, 기존 사용자는 로그인
    @PostMapping("/register/kakao")
    public ResponseEntity<ApiResponse<MemberDTO>> registerOrLoginMemberByKakao(@RequestBody RegisterKakaoDTO registerDto, HttpServletRequest req, HttpServletResponse resp) {
        log.info("카카오톡으로 로그인 POST 요청==========");
        log.info("RegisterKakaoDTO: {}", registerDto);
        log.info("HttpServletRequest: {}", req);
        log.info("HttpServletResponse: {}", resp);
        try {
            MemberDTO userDto = memberService.registerOrLoginMemberByKakaoCode(registerDto, req, resp);
            return ResponseEntity.ok().body(ApiResponse.success(SuccessCode.MEMBER_KAKAO_REGISTER_LOGIN_SUCCESS, userDto));
        } catch (Exception e) {
            log.error("카카오 회원가입/로그인 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(ErrorCode.MEMBER_REGISTER_FAIL));
        }
    }

    // 구글 회원가입, 기존 사용자는 로그인
    @PostMapping("/register/google")
    public ResponseEntity<ApiResponse<MemberDTO>> registerOrLoginMemberByGoogle(@RequestBody RegisterGoogleDTO registerDto, HttpServletRequest req, HttpServletResponse resp) {
        log.info("구글로 로그인 POST 요청==========");
        log.info("RegisterGoogleDTO: {}", registerDto);
        log.info("HttpServletRequest: {}", req);
        log.info("HttpServletResponse: {}", resp);
        try {
            MemberDTO userDto = memberService.registerOrLoginMemberByGoogleCode(registerDto, req, resp);
            return ResponseEntity.ok().body(ApiResponse.success(SuccessCode.MEMBER_GOOGLE_REGISTER_LOGIN_SUCCESS, userDto));
        } catch (Exception e) {
            log.error("구글 회원가입/로그인 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(ErrorCode.MEMBER_REGISTER_FAIL));
        }
    }

    // 이메일 로그인
    @PostMapping("/loginEmail")
    public ResponseEntity<ApiResponse<MemberDTO>> loginMemberByEmail(@RequestBody LoginEmailDTO loginDto, HttpServletRequest req, HttpServletResponse resp) {
        log.info("이메일로 로그인 POST 요청==========");
        log.info("LoginEmailDTO: {}", loginDto);
        log.info("HttpServletRequest: {}", req);
        log.info("HttpServletResponse: {}", resp);
        try {
            MemberDTO userDto = memberService.loginMemberByEmail(loginDto, req, resp);
            return ResponseEntity.ok().body(ApiResponse.success(SuccessCode.MEMBER_LOGIN_SUCCESS, userDto));
        } catch (Exception e) {
            log.error("이메일 로그인 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(ErrorCode.MEMBER_REGISTER_FAIL));
        }
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Boolean>> logoutMember(HttpServletResponse resp) {
        try {
            boolean userDto = memberService.logoutMember(resp);
            return ResponseEntity.ok().body(ApiResponse.success(SuccessCode.MEMBER_LOGOUT_SUCCESS, userDto));
        } catch (Exception e) {
            log.error("로그아웃 실패", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(ErrorCode.MEMBER_LOGOUT_FAIL));
        }
    }
}