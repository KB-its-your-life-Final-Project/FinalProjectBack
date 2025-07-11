package org.lighthouse.security.vo;

import lombok.Getter;
import lombok.Setter;
import org.lighthouse.member.vo.MemberVO;
import org.springframework.security.core.userdetails.User;

@Getter
@Setter
public class CustomUser extends User {
    private MemberVO member;

    public CustomUser(MemberVO vo){
        super(vo.getUsername(), vo.getPassword(), vo.getAuthList());
        this.member = vo;
    }
}
