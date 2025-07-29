package com.lighthouse.transactions.vo;

import com.lighthouse.transactions.entity.EstateApiIntegration;
import com.lighthouse.transactions.util.AddressUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 오피스텔 매매 거래 정보 VO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OfficetelTradeVO {
    private int sggCd;                 // 지역코드
    private String sggNm;              // 시군구
    private String umdNm;              // 법정동
    private String jibun;              // 지번
    private String offiNm;             // 단지명
    private double excluUseAr;         // 전용면적
    private int dealYear;              // 계약년도
    private int dealMonth;             // 계약월
    private int dealDay;               // 계약일
    private String dealAmount;         // 거래금액(만원)
    private int floor;                 // 층
    private int buildYear;             // 건축년도
    private String cdealType;          // 해제여부
    private String cdealDay;           // 해제사유발생일
    private String dealingGbn;         // 거래유형(중개 및 직거래 여부)
    private String estateAgentSggNm;   // 중개사소재지(시군구 단위)
    private String slerGbn;            // 거래주체정보_매도자
    private String buyerGbn;           // 거래주체정보_매수자

    public static EstateApiIntegration toEstateApiIntegration(OfficetelTradeVO entity) {
        String jibunAddr = AddressUtils.getJibunAddr(entity.getUmdNm(), entity.getJibun());
        return EstateApiIntegration.builder()
                .sggCd(entity.getSggCd())
                .sggNm(entity.getSggNm())
                .umdNm(entity.getUmdNm())
                .jibun(entity.getJibun())
                .buildingName(entity.getOffiNm())
//                .mhouseType(entity.getMhouseType())
//                .shouseType(entity.getShouseType())
                .buildYear(entity.getBuildYear())
                .buildingType(2)                         // 건물 유형 (1: 아파트, 2: 오피스텔, 3: 연립, 4: 단독)
                .sourceApi(3) // 1: api_apartment_trade, 2: api_apartment_rental, 3: api_officetel_trade, 4: api_officetel_rental, 5: api_multihouse_trade, 6: api_multihouse_rental, 7: api_singlehouse_trade, 8: api_singlehouse_rental
                .jibunAddr(jibunAddr)
//                .latitude(entity.getLatitude())
//                .longitude(entity.getLongitude())
                .build();
    }
}
