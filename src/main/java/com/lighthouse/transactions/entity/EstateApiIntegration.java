package com.lighthouse.transactions.entity;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Objects;

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
    private int sourceApi;          // 원본 API (1: apartment_trade, 2: apartment_rental, 3: officetel_trade, 4: officetel_rental, 5: multihouse_trade, 6: multihouse_rental, 7: singlehouse_trade, 8: singlehouse_rental)
    private String jibunAddr;       // 지번 주소
    private double latitude;        // 위도
    private double longitude;       // 경도

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EstateApiIntegration that = (EstateApiIntegration) o;
        return sggCd == that.sggCd &&
                buildYear == that.buildYear &&
                buildingType == that.buildingType &&
                sourceApi == that.sourceApi &&
                Double.compare(that.latitude, latitude) == 0 &&
                Double.compare(that.longitude, longitude) == 0 &&
                Objects.equals(sggNm, that.sggNm) &&
                Objects.equals(umdNm, that.umdNm) &&
                Objects.equals(jibun, that.jibun) &&
                Objects.equals(buildingName, that.buildingName) &&
                Objects.equals(mhouseType, that.mhouseType) &&
                Objects.equals(shouseType, that.shouseType) &&
                Objects.equals(jibunAddr, that.jibunAddr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sggCd, sggNm, umdNm, jibun, buildingName, mhouseType, shouseType, buildYear, buildingType, sourceApi, jibunAddr, latitude, longitude);
    }
}
