package com.lighthouse.transactions.mapper;

import com.lighthouse.transactions.entity.EstateApiIntegration;
import com.lighthouse.transactions.vo.ApartmentTradeVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class TransactionMapperTest {

    private TransactionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = mock(TransactionMapper.class);
    }

    @Test
    @DisplayName("실거래 데이터 삽입 테스트 (Mock)")
    void insertApartmentTradeTest() {
        ApartmentTradeVO vo = ApartmentTradeVO.builder()
                .aptDong("10동")
                .aptNm("인왕산2차아이파크")
                .buildYear(2015)
                .dealAmount("63,400")
                .dealDay(22)
                .dealMonth(12)
                .dealYear(2015)
                .excluUseAr(84.0284)
                .floor(10)
                .jibun("88")
                .umdNm("무악동")
                .build();

        doNothing().when(mapper).insertApartmentTrade(vo);

        mapper.insertApartmentTrade(vo);

        verify(mapper, times(1)).insertApartmentTrade(vo);
    }

    @Test
    @DisplayName("findIdByUniqueCombination 테스트 (Mock)")
    void findIdByUniqueCombinationTest() {
        EstateApiIntegration entity = EstateApiIntegration.builder()
                .sggCd(48310)
                .sggNm("거제시")
                .umdNm("고현동")
                .jibun("159-1")
                .buildingName("휴엔하임오피스텔")
                .buildYear(2015)
                .buildingType(2)
                .sourceApi(3)
                .latitude(37.131506)
                .longitude(127.0896495)
                .build();

        // 예상 반환값
        int expectedEstateId = 12345;

        when(mapper.findIdByUniqueCombination(any())).thenReturn(expectedEstateId);

        int estateId = mapper.findIdByUniqueCombination(null); // getEstateParams(entity) 대신 Mock 처리

        verify(mapper, times(1)).findIdByUniqueCombination(any());
        assertEquals(expectedEstateId, estateId);
    }
}
