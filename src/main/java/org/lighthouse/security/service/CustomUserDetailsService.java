package org.lighthouse.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lighthouse.security.vo.CustomUser;
import org.lighthouse.member.vo.MemberVO;
import org.lighthouse.security.mapper.UserDetailsMapper;
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
        MemberVO vo = mapper.get(username);
        if(vo == null){
            throw new UsernameNotFoundException(username + "은 없는 id입니다.");
        }
        return new CustomUser(vo);
    }
}
