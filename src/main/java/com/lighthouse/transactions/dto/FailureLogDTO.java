package com.lighthouse.transactions.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FailureLogDTO {
    private final int lawdCd;
    private final int dealYmd;
    private final String apiName;
}
