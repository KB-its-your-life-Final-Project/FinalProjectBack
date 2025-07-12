package com.lighthouse.security.vo;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

@Data
public class AuthVO implements GrantedAuthority {
    private String auth;
    private String username;

    @Override
    public String getAuthority() {
        return auth;
    }
}
