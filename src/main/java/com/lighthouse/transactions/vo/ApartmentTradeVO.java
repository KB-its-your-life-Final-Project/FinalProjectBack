package com.lighthouse.transactions.vo;

import com.lighthouse.transactions.entity.EstateApiIntegration;
import com.lighthouse.transactions.entity.EstateApiIntegrationSales;
import com.lighthouse.transactions.util.AddressUtil;
import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static com.lighthouse.transactions.util.ParseUtil.safeParseInt;

/**
 * 아파트 매매 거래 정보 VO
 */
@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApartmentTradeVO {
    private int sggCd;                 // 지역코드
    private String umdNm;              // 법정동
    private String aptDong;            // 아파트동명
    private String aptNm;              // 단지명
    private int buildYear;             // 건축연도
    private String cdealDay;           // 해제사유발생일
    private String cdealType;          // 해제여부
    private String dealAmount;         // 거래금액 (만원)
    private int dealDay;               // 계약일
    private int dealMonth;             // 계약월
    private int dealYear;              // 계약년도
    private String dealingGbn;         // 거래유형 (중개 / 직거래)
    private String estateAgentSggNm;   // 중개사소재지 (시군구 단위)
    private double excluUseAr;         // 전용면적
    private int floor;                 // 층
    private String jibun;              // 지번
    private String landLeaseholdGbn;   // 토지임대부 아파트 여부
    private String rgstDate;           // 등기일자
    private String slerGbn;            // 거래주체정보 매도자 (개인/법인/공공기관/기타)
    private String buyerGbn;           // 거래주체정보 매수자 (개인/법인/공공기관/기타)

    public static EstateApiIntegration toEstateApiIntegration(ApartmentTradeVO entity, AddressUtil addrUtils) {
        String jibunAddr = AddressUtil.getJibunAddr(entity.getUmdNm(), entity.getJibun());
        Map<String, Double> latLngMap = addrUtils.getLatLng(jibunAddr);
        double lat = latLngMap.getOrDefault("lat", 0.0);
        double lng = latLngMap.getOrDefault("lng", 0.0);
        return EstateApiIntegration.builder()
                .sggCd(entity.getSggCd())
                .sggNm("")
                .umdNm(entity.getUmdNm())
                .jibun(entity.getJibun())
                .buildingName(entity.getAptNm())
                .mhouseType("")
                .shouseType("")
                .buildYear(entity.getBuildYear())
                .buildingType(1) // 건물 유형 (1: 아파트, 2: 오피스텔, 3: 연립, 4: 단독)
                .sourceApi(1) // 1: api_apartment_trade, 2: api_apartment_rental, 3: api_officetel_trade, 4: api_officetel_rental, 5: api_multihouse_trade, 6: api_multihouse_rental, 7: api_singlehouse_trade, 8: api_singlehouse_rental
                .jibunAddr(jibunAddr)
                .latitude(lat)
                .longitude(lng)
                .build();
    }

    public static EstateApiIntegrationSales toEstateApiIntegrationSales(ApartmentTradeVO entity) {
        return EstateApiIntegrationSales.builder()
//                .estateId()                       // Service 단에서 처리 (estate_api_integration_tbl의 id)
                .dealYear(entity.getDealYear())
                .dealMonth(entity.getDealMonth())
                .dealDay(entity.getDealDay())
                .dealAmount(safeParseInt(entity.getDealAmount()))
//                .deposit(safeParseInt(entity.getDeposit()))
//                .monthlyRent(safeParseInt(entity.getMonthlyRent()))
                .tradeType(1)                       // 거래 유형 (1: 매매, 2: 전월세)
                .build();
    }
}
