package com.lighthouse.transactions.vo;

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
    private String sggCd;            // 지역코드
    private String umdNm;           // 법정동
    private String mhouseNm;        // 연립다세대명
    private String jibun;           // 지번
    private String buildYear;       // 건축년도
    private String excluUseAr;      // 전용면적
    private String houseType;       // (연립/다세대)

    private String dealYear;        // 계약년도
    private String dealMonth;       // 계약월
    private String dealDay;         // 계약일

    private String deposit;         // 보증금액(만원)
    private String monthlyRent;     // 월세금액(만원)

    private String floor;           // 층
    private String contractTerm;    // 계약기간
    private String contractType;    // 계약구분
    private String useRRRight;      // 갱신요구권사용

    private String preDeposit;      // 종전계약보증금
    private String preMonthlyRent;  // 종전계약월세
}
