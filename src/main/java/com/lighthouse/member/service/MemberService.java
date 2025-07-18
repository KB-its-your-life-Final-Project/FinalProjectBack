package com.lighthouse.member.service;

import com.lighthouse.member.dto.ChangePasswordDTO;
import com.lighthouse.member.dto.EmailRegisterDTO;
import com.lighthouse.member.dto.KakaoRegisterDTO;
import com.lighthouse.member.exception.PasswordMismatchException;
import com.lighthouse.member.util.ClientIpUtils;
import com.lighthouse.member.service.external.KakaoOidcClient;
import com.lighthouse.member.service.external.KakaoTokenClient;
import com.lighthouse.member.dto.MemberDTO;
import com.lighthouse.member.mapper.MemberMapper;
import com.lighthouse.member.vo.MemberVO;
import com.lighthouse.security.util.JwtCookieManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    final MemberMapper mapper;
    final PasswordEncoder passwordEncoder;
    private final KakaoTokenClient kakaoTokenClient;
    private final KakaoOidcClient kakaoOidcClient;
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

    // 아이디 중복 확인
    public boolean checkDuplicateEmail(String email) {
        MemberVO memberVo = mapper.selectMemberByEmail(email);
        return memberVo != null;
    }

    // 이메일 회원가입
    @Transactional
    public MemberDTO registerMemberByEmail(EmailRegisterDTO dto, HttpServletRequest req) {
        log.info("email: {}", dto.getEmail());
        String clientIp = ClientIpUtils.getClientIp(req);
        MemberVO memberVo = dto.toVO();
        memberVo.setPwd(passwordEncoder.encode(memberVo.getPwd())); // 비밀번호 암호화
        memberVo.setKakaoUserId("");
        memberVo.setRegIp(clientIp);
        memberVo.setRecentIp(clientIp);
        mapper.insertUser(memberVo);
        return selectMemberByEmail(memberVo.getEmail());
    }

    // 카카오 회원가입 (또는 로그인)
    public MemberDTO registerOrLoginMemberByKakaoCode(KakaoRegisterDTO dto, HttpServletRequest req, HttpServletResponse resp) {
        String clientIp = ClientIpUtils.getClientIp(req);
        String accessTokenKakao = kakaoTokenClient.getAccessToken(dto.getCode());
        String kakaoUserId = kakaoOidcClient.getKakaoUserId(accessTokenKakao);

        // 사용자 조회
        MemberVO memberVo = mapper.selectMemberByKakaoUserId(kakaoUserId);

        // 회원가입 (미등록 사용자)
        if (memberVo == null) {
            memberVo = dto.toVO();
            memberVo.setEmail("");
            memberVo.setKakaoUserId(kakaoUserId);
            memberVo.setRegIp(clientIp);
            memberVo.setRecentIp(clientIp);
            mapper.insertUser(memberVo);
        }

        // Access Token 및 Refresh Token 쿠키에 저장
        jwtCookieManager.setTokensToCookies(resp, kakaoUserId);

        return selectMemberByKakaoUserId(memberVo.getKakaoUserId());
    }

    // 사용자 비밀번호 수정
    public void changePassword(ChangePasswordDTO dto) {
        MemberVO memberVo = mapper.selectMemberByEmail(dto.getEmail());
        if (!passwordEncoder.matches(dto.getOldPassword(), memberVo.getPwd())) {
            throw new PasswordMismatchException();
        }
        dto.setNewPassword(passwordEncoder.encode(dto.getNewPassword()));
        mapper.updatePassword(dto);
    }
}