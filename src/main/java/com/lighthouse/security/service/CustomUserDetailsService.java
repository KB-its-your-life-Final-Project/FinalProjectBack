package com.lighthouse.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.lighthouse.security.vo.CustomUser;
import com.lighthouse.member.vo.MemberVO;
import com.lighthouse.security.mapper.UserDetailsMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserDetailsMapper mapper;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        MemberVO memberVo = mapper.get(email);
        if(memberVo == null){
            throw new UsernameNotFoundException(email + "은 없는 이메일입니다.");
        }

        log.info(">>> Email: {}", memberVo.getEmail());
        log.info(">>> Password: {}", memberVo.getPwd());
        return new CustomUser(memberVo);
    }
}
