package com.lighthouse.transactions.vo;

import com.lighthouse.transactions.entity.EstateApiIntegration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 아파트 매매 거래 정보 VO
 */
@Getter
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

    public static EstateApiIntegration toEstateApiIntegration(ApartmentTradeVO entity) {
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
//                .sourceTable(entity.getSourceTable())
//                .originalId(entity.getOriginalId())
//                .jibunAddr(entity.getJibunAddr())
//                .latitude(entity.getLatitude())
//                .longitude(entity.getLongitude())
                .build();
    }
}
