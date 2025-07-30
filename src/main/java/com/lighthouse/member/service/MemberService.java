package com.lighthouse.member.service;

import com.lighthouse.member.dto.*;
import com.lighthouse.member.service.external.*;
import com.lighthouse.member.util.ClientIpUtils;
import com.lighthouse.member.mapper.MemberMapper;
import com.lighthouse.member.util.ValidateUtils;
import com.lighthouse.member.entity.Member;
import com.lighthouse.security.dto.TokenDTO;
import com.lighthouse.security.mapper.MemberTokenMapper;
import com.lighthouse.security.util.JwtCookieManager;
import com.lighthouse.security.service.TokenService;
import com.lighthouse.security.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
//import org.springframework.mail.javamail.MimeMessageHelper;
//import javax.mail.internet.MimeMessage;
import java.util.Map;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberMapper memberMapper;
    private final MemberTokenMapper memberTokenMapper;
    //    private final JavaMailSender mailSender;
//    private final ConcurrentHashMap<String, String> verificationCodeStore = new ConcurrentHashMap<>();
//    private final ConcurrentHashMap<String, LocalDateTime> codeTimestamps = new ConcurrentHashMap<>();
    private final PasswordEncoder passwordEncoder;
    private final KakaoOidcClient kakaoOidcClient;
    private final KakaoTokenClient kakaoTokenClient;
    private final KakaoUserClient kakaoUserClient;
    private final GoogleTokenClient googleTokenClient;
    private final GoogleUserClient googleUserClient;
    private final JwtCookieManager jwtCookieManager;
    private final JwtUtil jwtUtil;
    private final TokenService tokenService;

    // 모든 사용자 조회
    public List<MemberDTO> findAllMembers() {
        return memberMapper.findAllMembers().stream()
                .map(MemberDTO::toUser)
                .toList();
    }

    // 아이디로 사용자 조회
    public MemberDTO findMemberById(int id) {
        Member member = Optional.ofNullable(memberMapper.findMemberById(id))
                .orElseThrow(NoSuchElementException::new);
        return MemberDTO.toUser(member);
    }

    // 이메일로 사용자 조회
    public MemberDTO findMemberByEmail(String email) {
        Member member = Optional.ofNullable(memberMapper.findMemberByEmail(email))
                .orElseThrow(NoSuchElementException::new);
        return MemberDTO.toUser(member);
    }

    // 카카오 회원ID로 사용자 조회
    public MemberDTO findMemberByKakaoId(String kakaoUserId) {
        Member member = Optional.ofNullable(memberMapper.findMemberByKakaoId(kakaoUserId))
                .orElseThrow(NoSuchElementException::new);
        return MemberDTO.toUser(member);
    }

    // 구글 ID로 사용자 조회
    public MemberDTO findMemberByGoogleId(String googleId) {
        Member member = Optional.ofNullable(memberMapper.findMemberByGoogleId(googleId))
                .orElseThrow(NoSuchElementException::new);
        return MemberDTO.toUser(member);
    }

    // 로그인된 사용자 조회
    public MemberDTO findMemberLoggedIn(HttpServletRequest req, HttpServletResponse resp) {
        log.info("MemberService.findMemberLoggedIn() 실행 ======");
        // accessToken 검증
        String accessToken = jwtCookieManager.getAccessTokenFromRequest(req);
        if (accessToken != null) {
            String subject = jwtUtil.getSubjectFromToken(accessToken);
            log.info("request에서 추출된 accessToken: {}", accessToken);
            log.info("accessToken에서 추출된 subject(memberId): {}", subject);
            int memberIdFromToken = Integer.parseInt(subject);
            Member member = memberMapper.findMemberById(memberIdFromToken);
            log.info("subject로 찾은 member: {}", member);
            return MemberDTO.toUser(member);
        }
        // accessToken 만료 -> refreshToken 검증
        log.info("accessToken 없음 {}", accessToken);
        String refreshToken = jwtCookieManager.getRefreshTokenFromRequest(req);
        if (refreshToken != null) {
            // refreshToken 유효성 검사
            String subject = jwtUtil.getSubjectFromToken(refreshToken);
            int memberIdFromToken = Integer.parseInt(subject);
            boolean isRefreshTokenValid = tokenService.isRefreshTokenValid(memberIdFromToken, refreshToken);
            if (!isRefreshTokenValid) {
                log.info("refreshToken값이 일치하지 않습니다");
                return null;
            }
            // refreshToken 검증 성공 -> accessToken, refreshToken 발급 및 저장 (HttpOnly 쿠키, DB)
            TokenDTO tokenDto = jwtCookieManager.setTokensToCookies(resp, memberIdFromToken);
            tokenService.saveRefreshToken(memberIdFromToken, tokenDto);
            Member member = memberMapper.findMemberById(memberIdFromToken);
            return MemberDTO.toUser(member);
        }
        // refreshToken 만료
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
        return !ValidateUtils.isEmpty(email) && ValidateUtils.isValidEmailFormat(email);
    }

    // 비밀번호 유효성 검사
    public boolean isValidPassword(String password) {
        return !ValidateUtils.isEmpty(password) && ValidateUtils.isValidPasswordFormat(password);
    }

    // 인증 번호 전송
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

    // 인증 번호 검사
//    public boolean verifyCode(String email, String code) {
//        String stored = verificationCodeStore.get(email);
//        LocalDateTime sentAt = codeTimestamps.get(email);
//        if (stored == null || sentAt == null) return false;
//        if (LocalDateTime.now().isAfter(sentAt.plusMinutes(3))) return false; // 3분 유효
//        return stored.equals(code);
//    }

    // 인증 번호 생성
//    private String generateCode() {
//        return String.valueOf((int) ((Math.random() * 900000) + 100000)); // 6자리 숫자
//    }

    // 이메일 회원가입
    @Transactional
    public MemberDTO registerByEmail(RegisterEmailDTO dto, HttpServletRequest req) {
        log.info("MemberService.registerByEmail() 실행 ======");
        log.info("email: {}", dto.getEmail());
        log.info("name: {}", dto.getName());
        log.info("password: {}", dto.getPassword1());
        String clientIp = ClientIpUtils.getClientIp(req);
        Member member = dto.toVO();
        member.setPwd(passwordEncoder.encode(member.getPwd())); // 암호화
        member.setRegIp(clientIp);
        member.setRecentIp(clientIp);
        memberMapper.insertMember(member);
        return findMemberByEmail(member.getEmail());
    }

    // 이메일 로그인
    @Transactional
    public MemberDTO loginByEmail(LoginDTO dto, HttpServletRequest req, HttpServletResponse resp) {
        log.info("MemberService.loginByEmail() 실행 ======");
        // 비밀번호 검증
        String email = dto.getEmail();
        Member member = memberMapper.findMemberByEmail(email);
        log.info("MemberService: email로 사용자 조회, memberVo: {}", member);
        boolean isValidPassword = passwordEncoder.matches(dto.getPassword(), member.getPwd());
        log.info("비밀번호 검사: {}, {}", dto.getPassword(), member.getPwd());
        log.info("비밀번호 일치 여부: {}", isValidPassword);
        if (!isValidPassword) {
            log.info("비밀번호 일치하지 않음");
            return null;
        } else {
            log.info("비밀번호 일치");
            // IP주소 업데이트
            String clientIp = ClientIpUtils.getClientIp(req);
            member.setRecentIp(clientIp);
            memberMapper.updateMember(member);
            // 토큰 발급 및 저장 (HttpOnly 쿠키, DB)
            int memberId = member.getId();
            log.info("Token sub로 사용할 memberId: {}", memberId);
            TokenDTO tokenDto = jwtCookieManager.setTokensToCookies(resp, memberId);
            tokenService.saveRefreshToken(memberId, tokenDto);
            return findMemberByEmail(member.getEmail());
        }
    }

    // 카카오 로그인 또는 회원가입
    @Transactional
    public MemberDTO loginOrRegisterByKakaoCode(LoginDTO dto, HttpServletRequest req, HttpServletResponse resp) {
        log.info("MemberService.loginOrRegisterByKakaoCode() 실행 ======");
        String clientIp = ClientIpUtils.getClientIp(req);
        String kakaoAccessToken = kakaoTokenClient.getKakaoAccessToken(dto.getCode());
        String kakaoUserId = kakaoOidcClient.getKakaoUserId(kakaoAccessToken);

        // 등록된 사용자 -> IP주소 업데이트 / 미등록 사용자 -> 회원가입
        Member member = memberMapper.findMemberByKakaoId(kakaoUserId);
        log.info("MemberService: kakaoUserId로 사용자 조회, memberVo: {}", member);
        if (member != null) {
            member.setRecentIp(clientIp);
            memberMapper.updateMember(member);
        } else {
            String kakaoNickname = kakaoUserClient.getKakaoNickname(kakaoAccessToken);
            member = dto.toVO();
            member.setName(kakaoNickname);
            member.setKakaoId(kakaoUserId);
            member.setRegIp(clientIp);
            member.setRecentIp(clientIp);
            memberMapper.insertMember(member);
        }

        // 토큰 발급 및 저장 (HttpOnly 쿠키, DB)
        int memberId = member.getId();
        TokenDTO tokenDto = jwtCookieManager.setTokensToCookies(resp, memberId);
        tokenService.saveRefreshToken(memberId, tokenDto);

        return findMemberByKakaoId(member.getKakaoId());
    }

    // 구글 로그인 또는 회원가입
    @Transactional
    public MemberDTO loginOrRegisterByGoogleCode(LoginDTO dto, HttpServletRequest req, HttpServletResponse resp) {
        log.info("MemberService.loginOrRegisterByGoogleCode() 실행 ======");
        String clientIp = ClientIpUtils.getClientIp(req);
        String googleAccessToken = googleTokenClient.getGoogleAccessToken(dto.getCode());
        Map googleUserInfoMap = googleUserClient.getGoogleUserInfo(googleAccessToken);
        String googleId = googleUserInfoMap.get("id").toString();

        // 등록된 사용자 -> IP주소 업데이트 / 미등록 사용자 -> 회원가입
        Member member = memberMapper.findMemberByGoogleId(googleId);
        log.info("MemberService: googleId로 사용자 조회, memberVo: {}", member);
        if (member != null) {
            member.setRecentIp(clientIp);
            memberMapper.updateMember(member);
        } else {
            String email = googleUserInfoMap.get("email").toString();
            String name = googleUserInfoMap.get("name").toString();
            member = dto.toVO();
            member.setName(name);
            member.setEmail(email);
            member.setGoogleId(googleId);
            member.setRegIp(clientIp);
            member.setRecentIp(clientIp);
            memberMapper.insertMember(member);
        }

        // 토큰 발급 및 저장 (HttpOnly 쿠키, DB)
        int memberId = member.getId();
        TokenDTO tokenDto = jwtCookieManager.setTokensToCookies(resp, memberId);
        tokenService.saveRefreshToken(memberId, tokenDto);

        return findMemberByGoogleId(member.getGoogleId());
    }

    // 로그아웃
    public boolean logout(HttpServletResponse resp) {
        try {
            jwtCookieManager.clearTokensFromCookies(resp);
            log.info("로그아웃 성공");
            return true;
        } catch (Exception e) {
            log.error("accessToken, refreshToken 제거 실패", e);
            return false;
        }
    }
}