package com.lighthouse.transactions.vo;

import com.lighthouse.transactions.entity.EstateApiIntegration;
import com.lighthouse.transactions.util.AddressUtils;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 아파트 전월세 거래 정보 VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApartmentRentalVO {
    private int sggCd;                // 지역코드
    private String umdNm;             // 법정동
    private String aptNm;             // 단지명
    private String jibun;             // 지번
    private double excluUseAr;        // 전용면적 (제곱미터)
    private int dealYear;             // 계약년도
    private int dealMonth;            // 계약월
    private int dealDay;              // 계약일
    private String deposit;           // 보증금액 (만원)
    private String monthlyRent;       // 월세금액 (만원)
    private int floor;                // 층
    private int buildYear;            // 건축년도
    private String contractTerm;      // 계약기간
    private String contractType;      // 계약구분 (전세/월세)
    private String useRRRight;        // 갱신요구권 사용 여부 ("사용"/"미사용")
    private String preDeposit;        // 종전계약 보증금
    private String preMonthlyRent;    // 종전계약 월세

    public static EstateApiIntegration toEstateApiIntegration(ApartmentRentalVO entity, AddressUtils addrUtils) {
        String jibunAddr = AddressUtils.getJibunAddr(entity.getUmdNm(), entity.getJibun());
        Map<String, Double> latLongMap = addrUtils.getLatLong(jibunAddr);
        return EstateApiIntegration.builder()
                .sggCd(entity.getSggCd())
//                .sggNm(entity.getSggNm())
                .umdNm(entity.getUmdNm())
                .jibun(entity.getJibun())
                .buildingName(entity.getAptNm())
//                .mhouseType(entity.getMhouseType())
//                .shouseType(entity.getShouseType())
                .buildYear(entity.getBuildYear())
                .buildingType(1) // 건물 유형 (1: 아파트, 2: 오피스텔, 3: 연립, 4: 단독)
                .sourceApi(2) // 1: api_apartment_trade, 2: api_apartment_rental, 3: api_officetel_trade, 4: api_officetel_rental, 5: api_multihouse_trade, 6: api_multihouse_rental, 7: api_singlehouse_trade, 8: api_singlehouse_rental
                .jibunAddr(jibunAddr)
//                .latitude(entity.getLatitude())
//                .longitude(entity.getLongitude())
                .build();
    }
}