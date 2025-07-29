package com.lighthouse.buildingRegister.service;

import com.lighthouse.config.EnvLoader;
import com.lighthouse.config.RootConfig;
import com.lighthouse.security.config.SecurityConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = { RootConfig.class, SecurityConfig.class }, initializers = EnvLoader.class)
@Slf4j
@ActiveProfiles("local")
class BuildingRegisterServiceTest {

    @Autowired
    BuildingRegisterService service;

    @Test
    @DisplayName("건축물대장 일반 조회 테스트")
    void getBuildingRegisterCommon() {
        service.getBuildingRegisterCommon("경기도 광명시 소하동 1276-4","0");
    }

    @Test
    @DisplayName("집합건축물 대장 조회 테스트")
    void getBuildingRegisterSet(){
        service.getBuildingRegisterSet("경기도 시흥시 하중로209번길 9 (하중동, 참이슬아파트)", "201동");
    }


}