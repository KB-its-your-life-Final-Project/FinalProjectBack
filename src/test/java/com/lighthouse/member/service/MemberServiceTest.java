package com.lighthouse.member.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.ExtendWith;
import com.lighthouse.config.RootConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.jupiter.api.Assertions.assertNotNull;


@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes= {RootConfig.class})
@Slf4j
@ActiveProfiles("local")
class MemberServiceTest {
    @Autowired
    private MemberService service;

    // @Test
    // void getList() {
    //     for(MemberResponseDTO member: service.selectMembers()){
    //         log.info("{}",member);
    //     }
    // }

    // @Test
    // void get() {
    //     MemberResponseDTO dto = service.selectMemberByUsername("admin");
    //     assertNotNull(dto, "admin 회원은 반드시 존재해야 함");
    // }
}