// src/test/java/com/lighthouse/common/external/naver/NaverMapClientTest.java
package com.lighthouse.common.external.naver;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import com.lighthouse.config.ServletConfig;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ServletConfig.class})
@TestPropertySource(locations = "classpath:application-test.properties")
@ActiveProfiles("test")
@WebAppConfiguration
public class NaverMapClientTest {

    @Autowired
    private NaverMapClient naverMapClient;

    //네이버 지도로 위도경도 가져오기
    @Test
    void testGetNaverClient_ValidAddress() {
        // Given
        String address = "서울특별시 강남구 테헤란로 152";

        // When
        Map<String, Object> result = naverMapClient.getInfoOfAddress(address);

        // Then
        System.out.println("테스트 결과: " + result);
        System.out.println("위도: " + result.get("y"));
        System.out.println("경도: " + result.get("x"));

        assertNotNull(result);
        assertNotNull(result.get("y")); // 위도
        assertNotNull(result.get("x")); // 경도
        assertTrue(Double.parseDouble((String) result.get("y")) > 0);
        assertTrue(Double.parseDouble((String) result.get("x")) > 0);

    }
}