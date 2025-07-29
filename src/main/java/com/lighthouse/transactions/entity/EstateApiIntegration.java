package com.lighthouse.transactions.entity;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 부동산 거래 내역 기반 주소 정보 통합 테이블
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstateApiIntegration {
    private int id;                 // 기본키
    private int sggCd;              // 지역코드
    private String sggNm;           // 시군구
    private String umdNm;           // 법정동
    private String jibun;           // 지번
    private String buildingName;    // 건물명
    private String mhouseType;      // 연립/다세대 유형
    private String shouseType;      // 다가구/단독 유형
    private int buildYear;          // 건축 년도
    private int buildingType;       // 건물 유형 (1: 아파트, 2: 오피스텔, 3: 연립, 4: 단독)
    private int sourceTable;        // 원본 API (1: apartment_trade, 2: apartment_rental, 3: officetel_trade, 4: officetel_rental, 5: multihouse_trade, 6: multihouse_rental, 7: singlehouse_trade, 8: singlehouse_rental)
    private int originalId;         // 없어도 됨 - 삭제 고려 (원래는 원본 테이블에서의 id)
    private String jibunAddr;      // 지번 주소
    private double latitude;        // 위도
    private double longitude;       // 경도
}
