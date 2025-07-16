package com.lighthouse.member.service;

import com.lighthouse.member.dto.ChangePasswordDTO;
import com.lighthouse.member.dto.MemberRegisterDTO;
import com.lighthouse.member.exception.PasswordMismatchException;
//import com.lighthouse.security.vo.AuthVO;
import lombok.RequiredArgsConstructor;
import com.lighthouse.member.dto.MemberDTO;
import com.lighthouse.member.mapper.MemberMapper;
import com.lighthouse.member.vo.MemberVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public MemberDTO selectMemberByUsername(String id) {
        MemberVO memberVo = Optional.ofNullable(mapper.selectMemberByUsername(id))
                .orElseThrow(NoSuchElementException::new);
        return MemberDTO.of(memberVo);
    }

    // 아이디 중복 확인
    public boolean checkDuplicateUsername(String username) {
        MemberVO memberVo = mapper.findByUsername(username);
        return memberVo != null;
    }

    // 회원가입
    @Transactional
    public MemberDTO registerMember(MemberRegisterDTO dto) {
        MemberVO memberVo = dto.toVO();
        memberVo.setPassword(passwordEncoder.encode(memberVo.getPassword())); // 비밀번호 암호화
        mapper.insertUser(memberVo);
//        AuthVO auth = new AuthVO();
//        auth.setUsername(memberVo.getUsername());
//        auth.setAuth("ROLE_MEMBER");
//        mapper.insertAuth(auth);
        return selectMemberByUsername(memberVo.getUsername());
    }

    // 카카오/구글 로그인 시 아이디 조회 또는 회원가입
    public MemberDTO findOrRegisterByUsername(String username) {
        MemberVO memberVo = mapper.selectMemberByUsername(username);
        if (memberVo != null) {
            return MemberDTO.of(memberVo);
        } else {
            MemberVO newMemberVo = new MemberVO();
            newMemberVo.setUsername(username);
            newMemberVo.setPassword(""); // 카카오/구글 - 없어도 됨
            mapper.insertUser(newMemberVo);
//            AuthVO auth = new AuthVO();
//            auth.setUsername(newMemberVo.getUsername());
//            auth.setAuth("ROLE_MEMBER");
//            mapper.insertAuth(auth);
            return selectMemberByUsername(memberVo.getUsername());
        }
    }

    // 사용자 비밀번호 수정
    public void changePassword(ChangePasswordDTO dto) {
        MemberVO memberVo = mapper.selectMemberByUsername(dto.getUsername());
        if (!passwordEncoder.matches(dto.getOldPassword(), memberVo.getPassword())) {
            throw new PasswordMismatchException();
        }
        dto.setNewPassword(passwordEncoder.encode(dto.getNewPassword()));
        mapper.updatePassword(dto);
    }
}