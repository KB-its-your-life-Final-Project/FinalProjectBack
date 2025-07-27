package com.lighthouse.security.vo;

import lombok.Getter;
import lombok.Setter;
import com.lighthouse.member.vo.MemberVO;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collections;

@Getter
@Setter
public class CustomUser extends User {
    private MemberVO user;

    public CustomUser(MemberVO vo){
        super(vo.getEmail(),
              vo.getPwd(),
              Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        this.user = vo;
    }
}
