package com.lighthouse.member.service;

import com.lighthouse.member.dto.EmailRegisterDTO;
import com.lighthouse.member.dto.KakaoRegisterDTO;
import com.lighthouse.member.service.external.KakaoUserClient;
import com.lighthouse.member.util.ClientIpUtils;
import com.lighthouse.member.service.external.KakaoOidcClient;
import com.lighthouse.member.service.external.KakaoTokenClient;
import com.lighthouse.member.dto.MemberDTO;
import com.lighthouse.member.mapper.MemberMapper;
import com.lighthouse.member.util.ValidateUtils;
import com.lighthouse.member.vo.MemberVO;
import com.lighthouse.security.util.JwtCookieManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import javax.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
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
    private final MemberMapper mapper;
    @Autowired
    private final JavaMailSender mailSender;
    private final ConcurrentHashMap<String, String> verificationCodeStore = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, LocalDateTime> codeTimestamps = new ConcurrentHashMap<>();
    private final PasswordEncoder passwordEncoder;
    private final KakaoOidcClient kakaoOidcClient;
    private final KakaoTokenClient kakaoTokenClient;
    private final KakaoUserClient kakaoUserClient;
    private final JwtCookieManager jwtCookieManager;

    // 모든 사용자 조회
    public List<MemberDTO> selectMembers() {
        return mapper.selectMembers().stream()
                .map(MemberDTO::of)
                .toList();
    }

    // 아이디로 사용자 조회
    public MemberDTO selectMemberById(int id) {
        MemberVO memberVo = Optional.ofNullable(mapper.selectMemberById(id))
                .orElseThrow(NoSuchElementException::new);
        return MemberDTO.of(memberVo);
    }

    // 이메일로 사용자 조회
    public MemberDTO selectMemberByEmail(String email) {
        MemberVO memberVo = Optional.ofNullable(mapper.selectMemberByEmail(email))
                .orElseThrow(NoSuchElementException::new);
        return MemberDTO.of(memberVo);
    }

    // 카카오 회원ID로 사용자 조회
    public MemberDTO selectMemberByKakaoUserId(String kakaoUserId) {
        MemberVO memberVo = Optional.ofNullable(mapper.selectMemberByKakaoUserId(kakaoUserId))
                .orElseThrow(NoSuchElementException::new);
        return MemberDTO.of(memberVo);
    }

    // 이메일 중복 확인
    public boolean checkDuplicateEmail(String email) {
        MemberVO memberVo = mapper.selectMemberByEmail(email);
        return memberVo != null;
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
    public void sendVerificationCode(String email) throws Exception {
        String code = generateCode();
        verificationCodeStore.put(email, code);
        codeTimestamps.put(email, LocalDateTime.now());
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(email);
        helper.setSubject("[Lighthouse] 이메일 인증 코드입니다.");
        helper.setText("Verification Code: " + code, true);
        mailSender.send(message);
    }

    // 인증 번호 검사
    public boolean verifyCode(String email, String code) {
        String stored = verificationCodeStore.get(email);
        LocalDateTime sentAt = codeTimestamps.get(email);
        if (stored == null || sentAt == null) return false;
        if (LocalDateTime.now().isAfter(sentAt.plusMinutes(3))) return false; // 3분 유효
        return stored.equals(code);
    }

    // 인증 번호 생성
    private String generateCode() {
        return String.valueOf((int) ((Math.random() * 900000) + 100000)); // 6자리 숫자
    }

    // 이메일 회원가입
    @Transactional
    public MemberDTO registerMemberByEmail(EmailRegisterDTO dto, HttpServletRequest req) {
        log.info("MemberService.registerMemberByEmail() 실행 ======");
        log.info("email: {}", dto.getEmail());
        log.info("name: {}", dto.getName());
        log.info("password: {}", dto.getPassword());
        String clientIp = ClientIpUtils.getClientIp(req);
        MemberVO memberVo = dto.toVO();
        memberVo.setPwd(passwordEncoder.encode(memberVo.getPwd())); // 암호화
        memberVo.setRegIp(clientIp);
        memberVo.setRecentIp(clientIp);
        mapper.insertUser(memberVo);
        return selectMemberByEmail(memberVo.getEmail());
    }

    // 카카오 회원가입 또는 로그인
    @Transactional
    public MemberDTO registerOrLoginMemberByKakaoCode(KakaoRegisterDTO dto, HttpServletRequest req, HttpServletResponse resp) {
        log.info("MemberService.registerOrLoginMemberByKakaoCode() 실행 ======");
        String clientIp = ClientIpUtils.getClientIp(req);
        String kakaoAccessToken = kakaoTokenClient.getKakaoAccessToken(dto.getCode());
        String kakaoUserId = kakaoOidcClient.getKakaoUserId(kakaoAccessToken);

        // 미등록 사용자 -> 회원가입 / 등록된 사용자 -> IP주소 업데이트
        MemberVO memberVo = mapper.selectMemberByKakaoUserId(kakaoUserId);
        log.info("MemberService: kakaoUserId로 사용자 조회, memberVo: {}", memberVo);
        if (memberVo == null) {
            String kakaoNickname = kakaoUserClient.getKakaoNickname(kakaoAccessToken);
            memberVo = dto.toVO();
            memberVo.setName(kakaoNickname);
            memberVo.setKakaoUserId(kakaoUserId);
            memberVo.setRegIp(clientIp);
            memberVo.setRecentIp(clientIp);
            mapper.insertUser(memberVo);
        } else {
            memberVo.setRecentIp(clientIp);
            mapper.updateUser(memberVo);
        }

        // Access Token, Refresh Token 쿠키에 저장
        jwtCookieManager.setTokensToCookies(resp, kakaoUserId);

        return selectMemberByKakaoUserId(memberVo.getKakaoUserId());
    }

    // 로그아웃
    public boolean logoutUser(HttpServletResponse resp) {
        jwtCookieManager.clearTokensFromCookies(resp);
        return true;
    }
}