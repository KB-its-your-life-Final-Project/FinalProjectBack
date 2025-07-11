package org.lighthouse.member.service;

import lombok.RequiredArgsConstructor;
import org.lighthouse.member.dto.MemberDTO;
import org.lighthouse.member.mapper.MemberMapper;
import org.lighthouse.member.vo.MemberVO;
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
    public List<MemberDTO> selectMembers(){
        return mapper.selectMembers().stream()
                .map(MemberDTO::of)
                .toList();
    }
}
