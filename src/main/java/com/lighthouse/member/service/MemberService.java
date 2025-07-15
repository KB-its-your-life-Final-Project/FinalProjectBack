package com.lighthouse.member.service;

import com.lighthouse.member.dto.ChangePasswordDTO;
import com.lighthouse.member.dto.MemberRegisterDTO;
import com.lighthouse.member.dto.MemberUpdateDTO;
import com.lighthouse.member.exception.PasswordMissmatchException;
import com.lighthouse.security.vo.AuthVO;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import com.lighthouse.member.dto.MemberDTO;
import com.lighthouse.member.mapper.MemberMapper;
import com.lighthouse.member.vo.MemberVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {
    final MemberMapper mapper;
    final PasswordEncoder passwordEncoder;
    public MemberDTO selectMemberByUsername(String id){
        MemberVO vo = Optional.ofNullable(mapper.selectMemberByUsername(id))
                .orElseThrow(NoSuchElementException::new);
        return MemberDTO.of(vo);
    }
    public List<MemberDTO> selectMembers(){
        return mapper.selectMembers().stream()
                .map(MemberDTO::of)
                .toList();
    }
    public MemberDTO get(String username) {
        MemberVO member = Optional.ofNullable(mapper.get(username))
                .orElseThrow(NoSuchElementException::new);
        return MemberDTO.of(member);
    }
    public boolean checkDuplicate(String username) {
        MemberVO member = mapper.findByUsername(username);
        return member != null;
    }
    private void saveProfileImg(MultipartFile profileImg, String username) {
        //아바타 업로드
        if (profileImg != null && !profileImg.isEmpty()) {
            File dest = new File("c:/upload/profileImg", username + ".png");
            try {
                profileImg.transferTo(dest);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    @Transactional
    public MemberDTO join(MemberRegisterDTO dto) {
        MemberVO member = dto.toVO();

        member.setPassword(passwordEncoder.encode(member.getPassword())); // 비밀번호 암호화
        mapper.insert(member);

        AuthVO auth = new AuthVO();
        auth.setUsername(member.getUsername());
        auth.setAuth("ROLE_MEMBER");
        mapper.insertAuth(auth);

        saveProfileImg(dto.getProfileImg(), member.getUsername());

        return get(member.getUsername());
    }

    public MemberDTO update(MemberUpdateDTO member) {
        //1. 패스워드가 맞지 않으면 update처리하지 않음.
        //내가 입력한 pw는 member에 들어있고.
        //db에 pw를 검색해서 가지고 와서 가지고 와야함.
        MemberVO vo = mapper.get(member.getUsername());
        if (!passwordEncoder.matches(member.getPassword(), vo.getPassword())) {
            throw new PasswordMissmatchException();
        }
        //2. mybatis에 update()처리 요청
        mapper.update(member.toVO());

        //3. 아바타 저장
        saveProfileImg(member.getProfileImg(), member.getUsername());

        //4. 리턴은 검색해서 리턴
        return get(member.getUsername());
    }

    public void changePassword(ChangePasswordDTO changePassword) {
        MemberVO member = mapper.get(changePassword.getUsername());

        if (!passwordEncoder.matches(changePassword.getOldPassword(), member.getPassword())) {
            throw new PasswordMissmatchException();
        }

        changePassword.setNewPassword(passwordEncoder.encode(changePassword.getNewPassword()));

        mapper.updatePassword(changePassword);
    }
}
