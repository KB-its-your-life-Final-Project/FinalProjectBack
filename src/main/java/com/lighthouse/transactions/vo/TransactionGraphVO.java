package com.lighthouse.transactions.vo;

import lombok.Data;

@Data
public class TransactionGraphVO {
    private int dealYear;
    private int dealMonth;
    private int dealDay;
    private int dealAmount;
    private Integer deposit;
    private int tradeType;
    private String buildingName;
    private Long estateId;
}
