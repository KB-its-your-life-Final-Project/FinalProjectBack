package com.lighthouse.transactions.vo;

import com.lighthouse.transactions.entity.EstateApiIntegration;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 연립/다세대주택 전월세 거래 정보 VO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MultiHouseRentalVO {
    private int sggCd;              // 지역코드
    private String umdNm;           // 법정동
    private String mhouseNm;        // 연립다세대명
    private String jibun;           // 지번
    private int buildYear;          // 건축년도
    private double excluUseAr;      // 전용면적
    private String houseType;       // (연립/다세대)
    private int dealYear;           // 계약년도
    private int dealMonth;          // 계약월
    private int dealDay;            // 계약일
    private String deposit;         // 보증금액(만원)
    private String monthlyRent;     // 월세금액(만원)
    private int floor;              // 층
    private String contractTerm;    // 계약기간
    private String contractType;    // 계약구분
    private String useRRRight;      // 갱신요구권사용
    private String preDeposit;      // 종전계약보증금
    private String preMonthlyRent;  // 종전계약월세

    public static EstateApiIntegration toEstateApiIntegration(MultiHouseRentalVO entity) {
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
//                .sourceTable(entity.getSourceTable())
//                .originalId(entity.getOriginalId())
//                .jibunAddr(entity.getJibunAddr())
//                .latitude(entity.getLatitude())
//                .longitude(entity.getLongitude())
                .build();
    }
}
