package com.lighthouse.member.service;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import com.lighthouse.member.dto.MemberDTO;
import com.lighthouse.member.mapper.MemberMapper;
import com.lighthouse.member.vo.MemberVO;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class MemberService {
    final MemberMapper memberMapper;
    public MemberDTO findById(Number id){
        // 기본값 설정
        MemberVO vo = Optional.ofNullable(memberMapper.findById(id))
                .orElseThrow(NoSuchElementException::new);

        //Sping Security 권한 별 전달 데이터 변경
        //내용 미작성

        return MemberDTO.toUser(vo);
    }
    public List<MemberDTO> findAll(){
        
        return memberMapper.findAll().stream()
                .map(MemberDTO::toUser)
                .toList();
    }
}
