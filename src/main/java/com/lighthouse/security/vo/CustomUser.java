package com.lighthouse.security.vo;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import com.lighthouse.member.entity.Member;
import java.util.Collections;

@Getter
@Setter
public class CustomUser extends User {
    private Member user;

    public CustomUser(Member vo){
        super(vo.getEmail(),
              vo.getPwd(),
              Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        this.user = vo;
    }
}
