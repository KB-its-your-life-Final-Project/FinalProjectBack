package com.lighthouse.transactions.vo;

import lombok.Data;

@Data
public class TransactionGraphVO {
    private int dealYear;
    private int dealMonth;
    private int dealDay;
    private int dealAmount;
    private int deposit;
    private int monthlyRent;
    private int tradeType;
    private String buildingName;
}
