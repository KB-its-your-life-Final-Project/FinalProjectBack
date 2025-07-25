package com.lighthouse.transactions.vo;

import lombok.Data;

@Data
public class TransactionGraphVO {
    private int id;
    private int estateId;
    private int dealYear;
    private int dealMonth;
    private int dealDay;
    private int dealAmount;
    private Integer deposit;
    private Integer monthlyRent;
    private int tradeType;
    private String buildingName;
    // Getter/Setter
}
