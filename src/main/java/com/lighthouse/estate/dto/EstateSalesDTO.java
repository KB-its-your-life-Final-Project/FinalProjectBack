package com.lighthouse.estate.dto;

import lombok.Data;

/**
 * 부동산 매매 정보 DTO
 * estate_api_integration_sales_tbl 테이블의 데이터를 담는 객체
 */
@Data
public class EstateSalesDTO {
    
    private Integer id;
    private Integer estateId;
    private Integer dealYear;
    private Integer dealMonth;
    private Integer dealDay;
    private Integer dealAmount;
    private Integer deposit;
    private Integer monthlyRent;
    private Integer tradeType;
    
} 