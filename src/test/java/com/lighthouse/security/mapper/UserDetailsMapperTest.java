package com.lighthouse.security.mapper;

import com.lighthouse.security.mapper.UserDetailsMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import com.lighthouse.config.RootConfig;
import com.lighthouse.security.config.SecurityConfig;
import com.lighthouse.security.vo.AuthVO;
import com.lighthouse.member.vo.MemberVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;


@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = { RootConfig.class, SecurityConfig.class })
@Slf4j
@ActiveProfiles("local")
class UserDetailsMapperTest {
    @Autowired
    private UserDetailsMapper mapper;
    @Test
    public void get() {
        MemberVO member = mapper.get("admin");
        log.info("username: {}", member.getUsername());
        log.info("email: {}", member.getEmail());
        for(AuthVO auth : member.getAuthList()) {
            log.info(auth.getAuth());
        }
    }
}