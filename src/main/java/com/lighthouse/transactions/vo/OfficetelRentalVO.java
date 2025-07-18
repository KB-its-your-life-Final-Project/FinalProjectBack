package com.lighthouse.transactions.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 오피스텔 전월세 거래 정보 VO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfficetelRentalVO {
    private int sggCd;                // 지역코드
    private String sggNm;            // 시군구
    private String umdNm;            // 법정동
    private String jibun;            // 지번
    private String offiNm;           // 단지명
    private double excluUseAr;       // 전용면적
    private int dealYear;            // 계약년도
    private int dealMonth;           // 계약월
    private int dealDay;             // 계약일
    private String deposit;          // 보증금액(만원)
    private String monthlyRent;      // 월세금액(만원)
    private int floor;              // 층
    private int buildYear;           // 건축년도
    private String contractTerm;     // 계약기간
    private String contractType;     // 계약구분
    private String useRRRight;       // 갱신요구권사용
    private String preDeposit;       // 종전계약보증금
    private String preMonthlyRent;   // 종전계약월세
}
