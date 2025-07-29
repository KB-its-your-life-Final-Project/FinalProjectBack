package com.lighthouse.transactions.dto;

import lombok.Data;

//프론트 → 백엔드 요청 DTO
@Data
public class TransactionRequestDTO {

    private String buildingName;
    private String tradeType;
    private String startDate;
    private String endDate;
}
