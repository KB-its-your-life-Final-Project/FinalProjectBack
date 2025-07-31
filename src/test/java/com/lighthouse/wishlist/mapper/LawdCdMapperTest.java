package com.lighthouse.wishlist.mapper;

import com.lighthouse.config.EnvLoader;
import com.lighthouse.config.RootConfig;
import com.lighthouse.security.config.SecurityConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = { RootConfig.class, SecurityConfig.class }, initializers = EnvLoader.class)
@Slf4j
@ActiveProfiles("local")
class LawdCdMapperTest {
    @Autowired
    private LawdCdMapper mapper;

    @Test
    void testFindUmdNmByRegionCd() {
        String testRegionCd = "1168010300";

        // when
        String umdNm = mapper.findUmdNmByRegionCd(testRegionCd);

        // then
        assertThat(umdNm)
                .isNotNull()
                .isEqualTo("개포동"); // 예상 값, DB에 맞춰 조정 필요
    }
}