package com.lighthouse.transactions.dto;

import lombok.AllArgsConstructor;
import java.util.function.BiConsumer;

/**
 * API 명 + 실행 함수 매핑 클래스
 */
@AllArgsConstructor
public class ApiNameCallDTO {
    public String apiName;
    public BiConsumer<Integer, Integer> apiCall;
}
