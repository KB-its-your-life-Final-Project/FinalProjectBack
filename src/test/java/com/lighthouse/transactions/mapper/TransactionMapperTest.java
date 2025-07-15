package com.lighthouse.transactions.mapper;

import com.lighthouse.config.RootConfig;
import com.lighthouse.security.config.SecurityConfig;
import com.lighthouse.transactions.vo.ApartmentTradeVO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;


@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = { RootConfig.class, SecurityConfig.class })
@Slf4j
@ActiveProfiles("local")
class TransactionMapperTest {
    @Autowired
    private TransactionMapper mapper;

    @Test
    @DisplayName("실거래 데이터 삽입 테스트")
    void insertApartmentTradeTest() {
        ApartmentTradeVO vo = ApartmentTradeVO.builder()
                .aptDong("10동")
                .aptNm("인왕산2차아이파크")
                .buildYear(2015)
                .buyerGbn("")
                .cdealDay("")
                .cdealType("")
                .dealAmount("63,400")
                .dealDay(22)
                .dealMonth(12)
                .dealYear(2015)
                .dealingGbn("")
                .estateAgentSggNm("")
                .excluUseAr(84.0284)
                .floor(10)
                .jibun("88")
                .landLeaseholdGbn("N")
                .rgstDate("")
                .sggCd(11110)
                .slerGbn("")
                .umdNm("무악동")
                .build();

        mapper.insertApartmentTrade(vo);
        log.info("✅ 테스트 데이터 insert 완료: {}", vo);
    }
}