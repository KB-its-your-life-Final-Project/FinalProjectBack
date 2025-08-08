package com.lighthouse.member.service;

import com.lighthouse.member.dto.*;
import com.lighthouse.member.service.external.*;
import com.lighthouse.member.util.ClientIpUtil;
import com.lighthouse.member.util.ValidateUtil;
import com.lighthouse.member.mapper.MemberMapper;
import com.lighthouse.member.entity.Member;
import com.lighthouse.security.dto.TokenDTO;
import com.lighthouse.security.util.JwtCookieUtil;
import com.lighthouse.security.service.TokenService;
import com.lighthouse.security.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
//import org.springframework.mail.javamail.MimeMessageHelper;
//import javax.mail.internet.MimeMessage;
import java.util.*;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberMapper memberMapper;
//    private final JavaMailSender mailSender;
//    private final ConcurrentHashMap<String, String> verificationCodeStore = new ConcurrentHashMap<>();
//    private final ConcurrentHashMap<String, LocalDateTime> codeTimestamps = new ConcurrentHashMap<>();
    private final PasswordEncoder passwordEncoder;
    private final KakaoOidcClient kakaoOidcClient;
    private final KakaoTokenClient kakaoTokenClient;
    private final KakaoUserClient kakaoUserClient;
    private final GoogleTokenClient googleTokenClient;
    private final GoogleUserClient googleUserClient;
    private final JwtCookieUtil jwtCookieUtil;
    private final JwtUtil jwtUtil;
    private final TokenService tokenService;
    private final FileUploadService fileUploadService;

    // 모든 회원 조회
    public List<MemberResponseDTO> findAllMembers() {
        return memberMapper.findAllMembers().stream()
                .map(MemberResponseDTO::toUser)
                .toList();
    }

    // 아이디로 회원 조회
    public MemberResponseDTO findMemberById(int id) {
        Member member = Optional.ofNullable(memberMapper.findMemberById(id))
                .orElseThrow(NoSuchElementException::new);
        return MemberResponseDTO.toUser(member);
    }

    // 로그인된 회원 조회
    public MemberResponseDTO findMemberLoggedIn(HttpServletRequest req, HttpServletResponse resp) {
        log.info("MemberService.findMemberLoggedIn() 실행 ======");
        // accessToken 검증
        String accessToken = jwtCookieUtil.getAccessTokenFromRequest(req);
        if (accessToken != null) {
            boolean isAccessTokenValid = tokenService.isAccessTokenValid(accessToken);
            if (isAccessTokenValid) {
                String subject = jwtUtil.getSubjectFromToken(accessToken);
                log.info("request에서 추출된 accessToken: {}", accessToken);
                log.info("accessToken에서 추출된 subject(memberId): {}", subject);
                int memberIdFromToken = Integer.parseInt(subject);
                Member member = memberMapper.findMemberById(memberIdFromToken);
                log.info("subject로 찾은 member: {}", member);
                return MemberResponseDTO.toUser(member);
            } else {
                log.info("accessToken 만료");
            }
        }
        // accessToken 만료 또는 없음 -> refreshToken 검증
        log.info("accessToken 없음");
        String refreshToken = jwtCookieUtil.getRefreshTokenFromRequest(req);
        if (refreshToken != null) {
            // refreshToken 유효성 검사
            String subject = jwtUtil.getSubjectFromToken(refreshToken);
            int memberIdFromToken = Integer.parseInt(subject);
            log.info("request에서 추출된 refreshToken: {}", refreshToken);
            log.info("refreshToken에서 추출된 subject(memberId): {}", subject);
            boolean isRefreshTokenValid = tokenService.isRefreshTokenValid(memberIdFromToken, refreshToken);
            if (!isRefreshTokenValid) {
                log.info("refreshToken값이 만료되었거나 DB에 저장된 값과 일치하지 않습니다");
                return null;
            }
            // refreshToken 검증 성공 -> accessToken, refreshToken 재발급 및 저장 (HttpOnly 쿠키, DB)
            TokenDTO tokenDto = jwtCookieUtil.setTokensToCookies(resp, memberIdFromToken);
            tokenService.saveRefreshToken(memberIdFromToken, tokenDto);
            Member member = memberMapper.findMemberById(memberIdFromToken);
            return MemberResponseDTO.toUser(member);
        }
        // refreshToken 만료 또는 없음
        log.info("refreshToken 없음");
        return null;
    }

    // 이메일 중복 확인
    public boolean checkDuplicateEmail(String email) {
        boolean isDuplicate = memberMapper.existsByEmail(email);
        return isDuplicate;
    }

    // 이메일 유효성 검사
    public boolean isValidEmail(String email) {
        return !ValidateUtil.isEmpty(email) && ValidateUtil.isValidEmailFormat(email);
    }

    // 비밀번호 유효성 검사
    public boolean isValidPassword(String password) {
        return !ValidateUtil.isEmpty(password) && ValidateUtil.isValidPasswordFormat(password);
    }

    // 비밀번호 검증
    public boolean isVerifiedPwd(VerifyPwdRequestDTO verifyPwdReqDto) {
        Member member = memberMapper.findMemberByEmail(verifyPwdReqDto.getEmail());
        return passwordEncoder.matches(verifyPwdReqDto.getPwd(), member.getPwd());
    }

    // 이메일 인증 번호 전송
//    public void sendVerificationCode(String email) throws Exception {
//        String code = generateCode();
//        verificationCodeStore.put(email, code);
//        codeTimestamps.put(email, LocalDateTime.now());
//        MimeMessage message = mailSender.createMimeMessage();
//        MimeMessageHelper helper = new MimeMessageHelper(message, true);
//        helper.setTo(email);
//        helper.setSubject("[Lighthouse] 이메일 인증 코드입니다.");
//        helper.setText("Verification Code: " + code, true);
//        mailSender.send(message);
//    }

    // 이메일 인증 번호 검사
//    public boolean verifyCode(String email, String code) {
//        String stored = verificationCodeStore.get(email);
//        LocalDateTime sentAt = codeTimestamps.get(email);
//        if (stored == null || sentAt == null) return false;
//        if (LocalDateTime.now().isAfter(sentAt.plusMinutes(3))) return false; // 3분 유효
//        return stored.equals(code);
//    }

    // 이메일 인증 번호 생성
//    private String generateCode() {
//        return String.valueOf((int) ((Math.random() * 900000) + 100000)); // 6자리 숫자
//    }

    // 이메일 회원가입
    @Transactional
    public MemberResponseDTO registerByEmail(RegisterEmailRequestDTO dto, HttpServletRequest req) {
        log.info("MemberService.registerByEmail() 실행 ======");
        log.info("email: {}", dto.getEmail());
        log.info("name: {}", dto.getName());
        log.info("password: {}", dto.getPassword1());
        String clientIp = ClientIpUtil.getClientIp(req);
        Member member = dto.toMember();
        member.setPwd(passwordEncoder.encode(member.getPwd())); // 암호화
        member.setRegIp(clientIp);
        member.setRecentIp(clientIp);
        memberMapper.insertMember(member);
        Member newMember = memberMapper.findMemberByEmail(member.getEmail());
        return MemberResponseDTO.toUser(newMember);
    }

    // 회원 탈퇴
    @Transactional
    public MemberResponseDTO unregister(MemberResponseDTO memberDto) {
        log.info("MemberService.unregister() 실행 ======");
        try {
            Member member = memberMapper.findMemberById(memberDto.getId());
            member.setIsDelete(2);
            memberMapper.updateMember(member);
            Member deletedMember = memberMapper.findMemberById(member.getId());
            return MemberResponseDTO.toUser(deletedMember);
        } catch (Exception e) {
            log.error("회원 탈퇴 중 오류 발생: ", e);
            return null;
        }
    }

    // 이메일 로그인
    @Transactional
    public MemberResponseDTO loginByEmail(LoginRequestDTO loginReqDto, HttpServletRequest req, HttpServletResponse resp) {
        log.info("MemberService.loginByEmail() 실행 ======");
        // 비밀번호 검증
        String email = loginReqDto.getEmail();
        String pwd = loginReqDto.getPwd();
        Member member = memberMapper.findMemberByEmail(email);
        log.info("MemberService: email로 회원 조회, member: {}", member);
        VerifyPwdRequestDTO verifyPwdReqDto = new VerifyPwdRequestDTO(email, pwd);
        boolean isVerifiedPassword = isVerifiedPwd(verifyPwdReqDto);
        log.info("비밀번호 일치 여부: {}", isVerifiedPassword);
        if (!isVerifiedPassword) {
            log.info("비밀번호 불일치");
            return null;
        } else {
            log.info("비밀번호 일치");
            // IP주소 업데이트
            String clientIp = ClientIpUtil.getClientIp(req);
            member.setRecentIp(clientIp);
            memberMapper.updateMember(member);
            // 토큰 발급 및 저장 (HttpOnly 쿠키, DB)
            int memberId = member.getId();
            log.info("Token sub로 사용할 memberId: {}", memberId);
            TokenDTO tokenDto = jwtCookieUtil.setTokensToCookies(resp, memberId);
            tokenService.saveRefreshToken(memberId, tokenDto);
            Member newMember = memberMapper.findMemberById(member.getId());
            return MemberResponseDTO.toUser(newMember);
        }
    }

    // 카카오 로그인 또는 회원가입
    @Transactional
    public MemberResponseDTO loginOrRegisterByKakaoCode(LoginRequestDTO dto, HttpServletRequest req, HttpServletResponse resp) {
        log.info("MemberService.loginOrRegisterByKakaoCode() 실행 ======");
        String clientIp = ClientIpUtil.getClientIp(req);
        String kakaoAccessToken = kakaoTokenClient.getKakaoAccessToken(dto.getCode());
        String kakaoUserId = kakaoOidcClient.getKakaoUserId(kakaoAccessToken);

        // 등록된 회원 -> IP주소 업데이트 / 미등록 회원 -> 회원가입
        Member member = memberMapper.findMemberByKakaoId(kakaoUserId);
        log.info("MemberService: kakaoUserId로 회원 조회, memberVo: {}", member);
        if (member != null) {
            member.setRecentIp(clientIp);
            memberMapper.updateMember(member);
        } else {
            String kakaoNickname = kakaoUserClient.getKakaoNickname(kakaoAccessToken);
            member = dto.toMember();
            member.setName(kakaoNickname);
            member.setEmail("");
            member.setKakaoId(kakaoUserId);
            member.setRegIp(clientIp);
            member.setRecentIp(clientIp);
            member.setPwd("");
            member.setGoogleId("");
            member.setPhone("");
            memberMapper.insertMember(member);
            member = memberMapper.findMemberByKakaoId(kakaoUserId);
        }
        // 토큰 발급 및 저장 (HttpOnly 쿠키, DB)
        int memberId = member.getId();
        TokenDTO tokenDto = jwtCookieUtil.setTokensToCookies(resp, memberId);
        tokenService.saveRefreshToken(memberId, tokenDto);
        Member loggedInMember = memberMapper.findMemberById(member.getId());
        return MemberResponseDTO.toUser(loggedInMember);
    }

    // 구글 로그인 또는 회원가입
    @Transactional
    public MemberResponseDTO loginOrRegisterByGoogleCode(LoginRequestDTO dto, HttpServletRequest req, HttpServletResponse resp) {
        log.info("MemberService.loginOrRegisterByGoogleCode() 실행 ======");
        String clientIp = ClientIpUtil.getClientIp(req);
        String googleAccessToken = googleTokenClient.getGoogleAccessToken(dto.getCode());
        Map googleUserInfoMap = googleUserClient.getGoogleUserInfo(googleAccessToken);
        String googleId = googleUserInfoMap.get("id").toString();

        // 등록된 회원 -> IP주소 업데이트 / 미등록 회원 -> 회원가입
        Member member = memberMapper.findMemberByGoogleId(googleId);
        log.info("MemberService: googleId로 회원 조회, memberVo: {}", member);
        if (member != null) {
            member.setRecentIp(clientIp);
            memberMapper.updateMember(member);
        } else {
            String email = googleUserInfoMap.get("email").toString();
            String name = googleUserInfoMap.get("name").toString();
            member = dto.toMember();
            member.setName(name);
            member.setEmail(email);
            member.setGoogleId(googleId);
            member.setRegIp(clientIp);
            member.setRecentIp(clientIp);
            member.setPwd("");
            member.setKakaoId("");
            member.setPhone("");
            memberMapper.insertMember(member);
            member = memberMapper.findMemberByGoogleId(googleId);
        }
        // 토큰 발급 및 저장 (HttpOnly 쿠키, DB)
        int memberId = member.getId();
        TokenDTO tokenDto = jwtCookieUtil.setTokensToCookies(resp, memberId);
        tokenService.saveRefreshToken(memberId, tokenDto);
        Member loggedInMember = memberMapper.findMemberById(member.getId());
        return MemberResponseDTO.toUser(loggedInMember);
    }

    // 로그아웃
    public boolean logout(HttpServletResponse resp) {
        try {
            jwtCookieUtil.clearTokensFromCookies(resp);
            log.info("로그아웃 성공");
            return true;
        } catch (Exception e) {
            log.error("accessToken, refreshToken 제거 실패", e);
            return false;
        }
    }

    // 회원 정보 (이름, 비밀번호) 변경
    public MemberResponseDTO changeMemberInfo(int changeType, String changeInfo, MemberResponseDTO memberDto) {
        try {
            Member member = memberMapper.findMemberById(memberDto.getId());
            if (changeType == 1) {
                String newName = changeInfo;
                if (Objects.equals(newName, member.getName())) {
                    log.info("기존 이름과 동일한 이름을 입력했습니다");
                    return null;
                }
                if (!ValidateUtil.isValidNameFormat(newName)) {
                    log.info("올바르지 않은 형식의 이름을 입력했습니다");
                    return null;
                }
                member.setName(newName);
            } else if (changeType == 2) {
                String newPwd = changeInfo;
                if (passwordEncoder.matches(newPwd, member.getPwd())) {
                    log.info("기존 비밀번호와 동일한 비밀번호를 입력했습니다");
                    return null;
                }
                if (!ValidateUtil.isValidPasswordFormat(newPwd)) {
                    log.info("올바르지 않은 형식의 비밀번호를 입력했습니다");
                    return null;
                }
                member.setPwd(passwordEncoder.encode(newPwd));
            }
            log.info("정보 변경 후 member: {}", member);
            memberMapper.updateMember(member);
            Member updatedMember = memberMapper.findMemberById(member.getId());
            log.info("DB updatedMember: {}", updatedMember);
            return MemberResponseDTO.toUser(updatedMember);
        } catch (Exception e) {
            log.error("회원 정보 (이름, 비밀번호) 변경 중 오류 발생: ", e);
            return null;
        }
    }

    // 회원 프로필사진 업로드
    public MemberResponseDTO uploadProfileImg(MemberResponseDTO memberDto, MultipartFile file) {
        try {
            Member member = memberMapper.findMemberById(memberDto.getId());
            fileUploadService.deleteFile(member.getProfileImg());  // 기존 파일 삭제
            String uploadedImgUrl = fileUploadService.uploadProfileImg(file, memberDto.getId());
            log.info("member: {}", member);
            log.info("uploadedImgUrl: {}", uploadedImgUrl);
            member.setProfileImg(uploadedImgUrl);
            memberMapper.updateMember(member);
            Member updatedMember = memberMapper.findMemberById(member.getId());
            return MemberResponseDTO.toUser(updatedMember);
        } catch (Exception e) {
            log.error("프로필사진 업로드 중 오류 발생: ", e);
            return null;
        }
    }

    // 회원 프로필사진 삭제 (기본 이미지로 변경)
    public MemberResponseDTO deleteProfileImg(MemberResponseDTO memberDto) {
        try {
            Member member = memberMapper.findMemberById(memberDto.getId());
            if (!ValidateUtil.isEmpty(member.getProfileImg())) {
                fileUploadService.deleteFile(member.getProfileImg());
            }
            member.setProfileImg("");
            Member updatedMember = memberMapper.findMemberById(member.getId());
            return MemberResponseDTO.toUser(updatedMember);
        } catch (Exception e) {
            log.error("프로필사진 삭제 중 오류 발생: ", e);
            return null;
        }
    }
}