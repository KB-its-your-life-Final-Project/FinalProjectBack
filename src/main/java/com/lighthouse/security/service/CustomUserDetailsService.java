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
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        MemberVO memberVo = mapper.get(username);
        if(memberVo == null){
            throw new UsernameNotFoundException(username + "은 없는 id입니다.");
        }

        log.info(">>> Username: {}", memberVo.getUsername());
        log.info(">>> Password: {}", memberVo.getPassword());
        log.info(">>> Authorities: {}", memberVo.getAuthList());
        return new CustomUser(memberVo);
    }
}
