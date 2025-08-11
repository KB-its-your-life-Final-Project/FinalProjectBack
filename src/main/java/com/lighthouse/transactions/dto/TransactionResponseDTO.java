package com.lighthouse.transactions.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransactionResponseDTO {
    private String date;
    private String buildingName;
    private Integer dealAmount;
    private Integer deposit;
    private Integer monthlyRent;
    private String type;
}
