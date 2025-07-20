package com.lighthouse.member.service;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import com.lighthouse.member.dto.MemberDTO;
import com.lighthouse.member.mapper.MemberMapper;
import com.lighthouse.member.vo.MemberVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class MemberService {
    final MemberMapper mapper;
    public MemberDTO selectMemberByUsername(String id){
        MemberVO vo = Optional.ofNullable(mapper.selectMemberByUsername(id))
                .orElseThrow(NoSuchElementException::new);
        return MemberDTO.of(vo);
    }
    public List<MemberDTO> findAll(){
        return mapper.findAll().stream()
                .map(MemberDTO::of)
                .toList();
    }
}
