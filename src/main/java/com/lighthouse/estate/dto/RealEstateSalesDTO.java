package com.lighthouse.estate.dto;

import lombok.Data;

/**
 * 부동산 매매 정보 DTO
 * all_real_estate_sales 테이블의 데이터를 담는 객체
 */
@Data
public class RealEstateSalesDTO {
    
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