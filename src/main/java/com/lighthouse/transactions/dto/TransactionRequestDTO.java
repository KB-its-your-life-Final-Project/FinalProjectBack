package com.lighthouse.transactions.dto;

import lombok.Data;

@Data
public class TransactionRequestDTO {

    private String buildingName;
    private Integer tradeType;
    private String startDate;
    private String endDate;


    private String jibunAddress;
    private Double lat;
    private Double lng;

}
