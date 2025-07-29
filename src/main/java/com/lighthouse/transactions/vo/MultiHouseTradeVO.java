package com.lighthouse.transactions.vo;

import com.lighthouse.transactions.entity.EstateApiIntegration;
import com.lighthouse.transactions.entity.EstateApiIntegrationSales;
import com.lighthouse.transactions.util.AddressUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

import static com.lighthouse.transactions.util.ParseUtil.safeParseInt;

/**
 * 연립/다세대주택 매매 거래 정보 VO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MultiHouseTradeVO {
    private int sggCd;                   // 지역코드
    private String umdNm;                // 법정동
    private String mhouseNm;             // 연립다세대명
    private String jibun;                // 지번
    private int buildYear;               // 건축년도
    private double excluUseAr;           // 전용면적
    private double landAr;               // 대지권면적
    private String houseType;            // 집 유형 (다세대/연립)
    private int dealYear;                // 계약년도
    private int dealMonth;               // 계약월
    private int dealDay;                 // 계약일
    private String dealAmount;           // 거래금액(만원)
    private int floor;                   // 층
    private String cdealType;            // 해제여부
    private String cdealDay;             // 해제사유발생일
    private String dealingGbn;           // 거래유형(중개 및 직거래 여부)
    private String estateAgentSggNm;     // 중개사소재지(시군구 단위)
    private String rgstDate;             // 등기일자
    private String slerGbn;              // 거래주체정보_매도자
    private String buyerGbn;             // 거래주체정보_매수자

    public static EstateApiIntegration toEstateApiIntegration(MultiHouseTradeVO entity, AddressUtil addrUtils) {
        String jibunAddr = AddressUtil.getJibunAddr(entity.getUmdNm(), entity.getJibun());
        Map<String, Double> latLngMap = addrUtils.getLatLng(jibunAddr);
        double lat = latLngMap.getOrDefault("lat", 0.0);
        double lng = latLngMap.getOrDefault("lng", 0.0);
        return EstateApiIntegration.builder()
                .sggCd(entity.getSggCd())
//                .sggNm(entity.getSggNm())
                .umdNm(entity.getUmdNm())
                .jibun(entity.getJibun())
                .buildingName(entity.getMhouseNm())
                .mhouseType(entity.getHouseType())
//                .shouseType(entity.getShouseType())
                .buildYear(entity.getBuildYear())
                .buildingType(3)                         // 건물 유형 (1: 아파트, 2: 오피스텔, 3: 연립, 4: 단독)
                .sourceApi(5) // 1: api_apartment_trade, 2: api_apartment_rental, 3: api_officetel_trade, 4: api_officetel_rental, 5: api_multihouse_trade, 6: api_multihouse_rental, 7: api_singlehouse_trade, 8: api_singlehouse_rental
                .jibunAddr(jibunAddr)
                .latitude(lat)
                .longitude(lng)
                .build();
    }

    public static EstateApiIntegrationSales toEstateApiIntegrationSales(MultiHouseTradeVO entity) {
        return EstateApiIntegrationSales.builder()
//                .estateId()                       // mapper.xml 단에서 처리 필요 (estate_api_integration_tbl의 id)
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
