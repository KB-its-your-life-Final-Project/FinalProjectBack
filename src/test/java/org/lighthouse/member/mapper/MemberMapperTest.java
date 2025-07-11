package org.lighthouse.member.mapper;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lighthouse.config.RootConfig;
import org.lighthouse.member.vo.MemberVO;
import org.lighthouse.security.config.SecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = { RootConfig.class, SecurityConfig.class })
@Slf4j
@ActiveProfiles("local")
class MemberMapperTest {
    @Autowired
    private MemberMapper mapper;
    @Test
    @DisplayName("기존 회원 정보 조회 테스트")
    public void testGetExistingUser() {
        String existUser = "admin";
        MemberVO member = mapper.selectMemberByUsername(existUser);

        assertNotNull(member, "회원 정보가 존재해야 합니다.");
        log.info("Exist user : {}", member);

    }
}