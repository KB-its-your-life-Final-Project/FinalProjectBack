package com.lighthouse.transactions.dto;


import lombok.Builder;
import lombok.Data;
//백엔드에서 프론트로의 응답

@Data
@Builder
public class TransactionResponseDTO {
    private String date;
    private Long estateId;
    private int price;
    private String type;

}
