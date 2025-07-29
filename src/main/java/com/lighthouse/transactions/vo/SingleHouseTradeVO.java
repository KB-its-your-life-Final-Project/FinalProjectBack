package com.lighthouse.transactions.vo;

import com.lighthouse.transactions.entity.EstateApiIntegration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 단독/다가구 매매 거래 정보 VO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SingleHouseTradeVO {
    private int sggCd;                 // 지역코드
    private String umdNm;              // 법정동
    private String houseType;          // 주택유형(단독/다가구)
    private String jibun;              // 지번
    private double totalFloorAr;       // 연면적
    private double plottageAr;         // 대지면적
    private int dealYear;              // 계약년도
    private int dealMonth;             // 계약월
    private int dealDay;               // 계약일
    private String dealAmount;         // 거래금액(만원)
    private int buildYear;             // 건축년도
    private String cdealType;          // 해제여부
    private String cdealDay;           // 해제사유발생일
    private String dealingGbn;         // 거래유형(중개 및 직거래 여부)
    private String estateAgentSggNm;   // 중개사소재지(시군구 단위)
    private String slerGbn;            // 매도자
    private String buyerGbn;           // 매수자

    public static EstateApiIntegration toEstateApiIntegration(SingleHouseTradeVO entity) {
        return EstateApiIntegration.builder()
                .sggCd(entity.getSggCd())
//                .sggNm(entity.getSggNm())
                .umdNm(entity.getUmdNm())
                .jibun(entity.getJibun())
//                .buildingName(entity.getBuildingName())
//                .mhouseType(entity.getMhouseType())
                .shouseType(entity.getHouseType())
                .buildYear(entity.getBuildYear())
                .buildingType(4)                        // 건물 유형 (1: 아파트, 2: 오피스텔, 3: 연립, 4: 단독)
//                .sourceTable(entity.getSourceTable())
//                .originalId(entity.getOriginalId())
//                .jibunAddr(entity.getJibunAddr())
//                .latitude(entity.getLatitude())
//                .longitude(entity.getLongitude())
                .build();
    }
}