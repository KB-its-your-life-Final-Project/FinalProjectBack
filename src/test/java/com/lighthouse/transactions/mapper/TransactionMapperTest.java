package com.lighthouse.transactions.mapper;

import com.lighthouse.config.EnvLoader;
import com.lighthouse.config.RootConfig;
import com.lighthouse.security.config.SecurityConfig;
import com.lighthouse.transactions.entity.EstateApiIntegration;
import com.lighthouse.transactions.vo.ApartmentTradeVO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import static com.lighthouse.transactions.util.ParseUtil.getEstateParams;
import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = { RootConfig.class, SecurityConfig.class }, initializers = EnvLoader.class)
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

    @Test
    @DisplayName("findIdByUniqueCombination 테스트")
    void findIdByUniqueCombinationTest() {
        EstateApiIntegration entity = EstateApiIntegration.builder()
                .sggCd(48310)
                .sggNm("거제시")
                .umdNm("고현동")
                .jibun("159-1")
                .buildingName("휴엔하임오피스텔")
                .mhouseType("")
                .shouseType("")
                .buildYear(2015)
                .buildingType(2) // 건물 유형 (1: 아파트, 2: 오피스텔, 3: 연립, 4: 단독)
                .sourceApi(3) // 1: api_apartment_trade, 2: api_apartment_rental, 3: api_officetel_trade, 4: api_officetel_rental, 5: api_multihouse_trade, 6: api_multihouse_rental, 7: api_singlehouse_trade, 8: api_singlehouse_rental
                .jibunAddr("고현동 159-1")
                .latitude(37.131506)
                .longitude(127.0896495)
                .build();
        // 테스트 대상 메소드
        int estateId = mapper.findIdByUniqueCombination(getEstateParams(entity));
        int expected = 630;
        assertEquals(expected, estateId, String.format("반환된 estateId가 예상 값인 %d과 다름", expected));
        log.info("✅ 테스트 데이터 select 완료: {}", estateId);
    }
}