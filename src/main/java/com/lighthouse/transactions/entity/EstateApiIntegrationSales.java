package com.lighthouse.transactions.entity;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * 부동산 거래 내역 통합 테이블
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstateApiIntegrationSales {
    private int id;             // 기본키
    private int estateId;       // estate_api_integration_tbl의 id
    private int dealYear;       // 계약 년도
    private int dealMonth;      // 계약 월
    private int dealDay;        // 계약 일
    private int dealAmount;     // 거래 금액 (만원)
    private int deposit;        // 보증 금액 (만원)
    private int monthlyRent;    // 월세 금액 (만원)
    private int tradeType;      // 거래 유형 (1: 매매, 2: 전월세)

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EstateApiIntegrationSales that = (EstateApiIntegrationSales) o;
        return estateId == that.estateId &&
                dealYear == that.dealYear &&
                dealMonth == that.dealMonth &&
                dealDay == that.dealDay &&
                dealAmount == that.dealAmount &&
                deposit == that.deposit &&
                monthlyRent == that.monthlyRent &&
                tradeType == that.tradeType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(estateId, dealYear, dealMonth, dealDay, dealAmount, deposit, monthlyRent, tradeType);
    }
}
