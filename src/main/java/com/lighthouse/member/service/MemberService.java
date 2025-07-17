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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {
    final MemberMapper mapper;
    final PasswordEncoder passwordEncoder;

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

    // 아이디 중복 확인
    public boolean checkDuplicateEmail(String email) {
        MemberVO memberVo = mapper.findByEmail(email);
        return memberVo != null;
    }

    // 회원가입
    @Transactional
    public MemberDTO registerMemberByEmail(EmailRegisterDTO dto, HttpServletRequest req) {
        String clientIp = ClientIpUtils.getClientIp(req);
        MemberVO memberVo = dto.toVO();
        memberVo.setPassword(passwordEncoder.encode(memberVo.getPassword())); // 비밀번호 암호화
        memberVo.setRegIp(clientIp);
        memberVo.setRecentIp(clientIp);
        mapper.insertUser(memberVo);
        return selectMemberById(memberVo.getId());
    }

    // 카카오 회원가입 (또는 로그인)
    public MemberDTO registerOrLoginMemberByKakaoCode(KakaoRegisterDTO dto, HttpServletRequest req) {
        String clientIp = ClientIpUtils.getClientIp(req);
        String accessToken = KakaoTokenClient.getAccessToken(dto.getCode());
        String kakaoUserId = KakaoOidcClient.getKakaoUserId(accessToken);
        String email = kakaoUserId + "@kakao.com";

        // 기존 사용자 확인
        MemberVO existingMember = mapper.findByEmail(email);
        if (existingMember != null) {
            return MemberDTO.of(existingMember);
        }

        // 회원가입
        MemberVO memberVo = dto.toVO();
        memberVo.setEmail(email);
        memberVo.setRegIp(clientIp);
        memberVo.setRecentIp(clientIp);
        mapper.insertUser(memberVo);
        return selectMemberById(memberVo.getId());
    }

    // 사용자 비밀번호 수정
    public void changePassword(ChangePasswordDTO dto) {
        MemberVO memberVo = mapper.selectMemberById(dto.getId());
        if (!passwordEncoder.matches(dto.getOldPassword(), memberVo.getPassword())) {
            throw new PasswordMismatchException();
        }
        dto.setNewPassword(passwordEncoder.encode(dto.getNewPassword()));
        mapper.updatePassword(dto);
    }
}