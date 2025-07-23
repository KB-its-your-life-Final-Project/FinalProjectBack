package com.lighthouse.transactions.vo;

import lombok.Data;

@Data
public class MonthlyTransactionVO {
    private String date;      // yyyy-MM 형식
    private String type;      // 매매 or 전월세
    private double avgAmount; // 평균 거래금액

    // Getter/Setter
}
