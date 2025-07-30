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
 * 단독/다가구 전월세 거래 정보 VO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SingleHouseRentalVO {
    private int sggCd;               // 지역코드
    private String umdNm;            // 법정동
    private String houseType;        // 주택유형(단독/다가구)
    private double totalFloorAr;     // 연면적
    private int dealYear;            // 계약년도
    private int dealMonth;           // 계약월
    private int dealDay;             // 계약일
    private String deposit;          // 보증금액(만원)
    private String monthlyRent;      // 월세금액(만원)
    private int buildYear;           // 건축년도
    private String contractTerm;     // 계약기간
    private String contractType;     // 계약구분
    private String useRRRight;       // 갱신요구권사용
    private String preDeposit;       // 종전계약보증금
    private String preMonthlyRent;   // 종전계약월세

    public static EstateApiIntegration toEstateApiIntegration(SingleHouseRentalVO entity, AddressUtil addrUtils) {
        String jibunAddr = AddressUtil.getJibunAddr(entity.getUmdNm(), "");
        Map<String, Double> latLngMap = addrUtils.getLatLng(jibunAddr);
        double lat = latLngMap.getOrDefault("lat", 0.0);
        double lng = latLngMap.getOrDefault("lng", 0.0);
        return EstateApiIntegration.builder()
                .sggCd(entity.getSggCd())
                .sggNm("")
                .umdNm(entity.getUmdNm())
                .jibun("")
                .buildingName("")
                .mhouseType("")
                .shouseType(entity.getHouseType())
                .buildYear(entity.getBuildYear())
                .buildingType(4)                        // 건물 유형 (1: 아파트, 2: 오피스텔, 3: 연립, 4: 단독)
                .sourceApi(8) // 1: api_apartment_trade, 2: api_apartment_rental, 3: api_officetel_trade, 4: api_officetel_rental, 5: api_multihouse_trade, 6: api_multihouse_rental, 7: api_singlehouse_trade, 8: api_singlehouse_rental
                .jibunAddr(jibunAddr)
                .latitude(lat)
                .longitude(lng)
                .build();
    }

    public static EstateApiIntegrationSales toEstateApiIntegrationSales(SingleHouseRentalVO entity) {
        return EstateApiIntegrationSales.builder()
//                .estateId()                       // Service 단에서 처리 (estate_api_integration_tbl의 id)
                .dealYear(entity.getDealYear())
                .dealMonth(entity.getDealMonth())
                .dealDay(entity.getDealDay())
//                .dealAmount(safeParseInt(entity.getDealAmount()))
                .deposit(safeParseInt(entity.getDeposit()))
                .monthlyRent(safeParseInt(entity.getMonthlyRent()))
                .tradeType(2)                       // 거래 유형 (1: 매매, 2: 전월세)
                .build();
    }
}
