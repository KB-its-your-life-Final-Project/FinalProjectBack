package com.lighthouse.member.controller;

import com.lighthouse.alarm.service.AlarmSchedulerService;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
@CrossOrigin(origins = "${FRONT_ORIGIN}", allowCredentials = "true")
public class MemberController {
    final MemberService memberService;
    final AlarmSchedulerService alarmSchedulerService;

    // 모든 사용자 정보 조회
    @GetMapping("")
    public ResponseEntity<ApiResponse<List<MemberDTO>>> findAllUsers() {
        List<MemberDTO> dtos = memberService.findAllMembers();
        return ResponseEntity.ok().body(ApiResponse.success(SuccessCode.MEMBER_FETCH_SUCCESS, dtos));
    }

    // 아이디로 사용자 정보 조회
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MemberDTO>> findMemberById(@RequestBody int id) {
        try {
            MemberDTO dto = memberService.findMemberById(id);
            return ResponseEntity.ok().body(ApiResponse.success(SuccessCode.MEMBER_FETCH_SUCCESS, dto));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ErrorCode.MEMBER_NOT_FOUND));
        }
    }

    // 로그인된 사용자 정보 조회
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MemberDTO>> checkLoginStatus(HttpServletRequest req, HttpServletResponse resp) {
        try {
            MemberDTO memberDto = memberService.findMemberLoggedIn(req, resp);
            if (memberDto != null) {
                return ResponseEntity.ok().body(ApiResponse.success(SuccessCode.MEMBER_FETCH_SUCCESS, memberDto));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(ErrorCode.UNAUTHORIZED));
            }
        } catch (NoSuchElementException e) {
            log.error("사용자 정보 조회 실패", e);
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
    public ResponseEntity<ApiResponse<MemberDTO>> registerByEmail(@ModelAttribute RegisterEmailDTO registerDto, HttpServletRequest req) {
        // 이메일 형식 유효성 검사
        if (!memberService.isValidEmail(registerDto.getEmail())) {
            return ResponseEntity.ok().body(ApiResponse.error(ErrorCode.INVALID_EMAIL_FORMAT));
        }
        // 비밀번호 형식 유효성 검사
//        if (!memberService.isValidPassword(registerDto.getPassword1())) {
//            return ResponseEntity.ok().body(ApiResponse.error(ErrorCode.INVALID_PASSWORD_FORMAT));
//        }
        // 비밀번호1과 비밀번호2 일치 여부 검사
        if (!Objects.equals(registerDto.getPassword1(), registerDto.getPassword2())) {
            return ResponseEntity.ok().body(ApiResponse.error(ErrorCode.INVALID_PASSWORD_CHECK));
        }
        try {
            log.info("email: {}", registerDto.getEmail());
            MemberDTO userDto = memberService.registerByEmail(registerDto, req);
            return ResponseEntity.ok().body(ApiResponse.success(SuccessCode.MEMBER_REGISTER_SUCCESS, userDto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(ErrorCode.MEMBER_REGISTER_FAIL));
        }
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<MemberDTO>> login(@RequestBody LoginDTO loginDto, HttpServletRequest req, HttpServletResponse resp) {
        log.info("로그인 POST 요청==========");
        log.info("LoginDTO: {}", loginDto);
        log.info("createType = " + loginDto.getCreatedType());
        // 이메일 로그인
        if (loginDto.getCreatedType() == 1) {
            String email = loginDto.getEmail();
            // 이메일 형식 유효성 검사
            if (!memberService.isValidEmail(email)) {
                return ResponseEntity.ok().body(ApiResponse.error(ErrorCode.INVALID_EMAIL_FORMAT));
            }
            // 회원가입 여부 검사
            boolean isMember = memberService.checkDuplicateEmail(email);
            if (isMember) {
                try {
                    MemberDTO memberDto = memberService.loginByEmail(loginDto, req, resp);
                    if (memberDto == null) {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(ErrorCode.INVALID_PASSWORD));
                    }
                    
                    // 로그인 성공 후 알림 체크
                    try {
                        String regIp = req.getRemoteAddr();
                        alarmSchedulerService.checkAlarmsOnLogin(memberDto.getId(), regIp);
                    } catch (Exception e) {
                        log.error("로그인 후 알림 체크 실패: memberId={}", memberDto.getId(), e);
                        // 알림 체크 실패는 로그인 성공에 영향을 주지 않음
                    }
                    
                    return ResponseEntity.ok().body(ApiResponse.success(SuccessCode.MEMBER_LOGIN_EMAIL_SUCCESS, memberDto));
                } catch (Exception e) {
                    log.error("이메일 로그인 실패", e);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(ErrorCode.EMAIL_LOGIN_FAIL));
                }
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(ErrorCode.MEMBER_NOT_FOUND));
            // 카카오 로그인
        } else if (loginDto.getCreatedType() == 2) {
            try {
                MemberDTO memberDto = memberService.loginOrRegisterByKakaoCode(loginDto, req, resp);
                
                // 로그인 성공 후 알림 체크 
                try {
                    String regIp = req.getRemoteAddr();
                    alarmSchedulerService.checkAlarmsOnLogin(memberDto.getId(), regIp);
                } catch (Exception e) {
                    log.error("로그인 후 알림 체크 실패: memberId={}", memberDto.getId(), e);
                    // 알림 체크 실패는 로그인 성공에 영향을 주지 않음
                }
                
                return ResponseEntity.ok().body(ApiResponse.success(SuccessCode.MEMBER_KAKAO_REGISTER_LOGIN_SUCCESS, memberDto));
            } catch (Exception e) {
                log.error("카카오 회원가입/로그인 실패", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(ErrorCode.MEMBER_REGISTER_FAIL));
            }
            // 구글 로그인
        } else if (loginDto.getCreatedType() == 3) {
            try {
                MemberDTO memberDto = memberService.loginOrRegisterByGoogleCode(loginDto, req, resp);
                
                // 로그인 성공 후 알림 체크 
                try {
                    String regIp = req.getRemoteAddr();
                    alarmSchedulerService.checkAlarmsOnLogin(memberDto.getId(), regIp);
                } catch (Exception e) {
                    log.error("로그인 후 알림 체크 실패: memberId={}", memberDto.getId(), e);
                    // 알림 체크 실패는 로그인 성공에 영향을 주지 않음
                }
                
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
}