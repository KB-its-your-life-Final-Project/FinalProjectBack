package com.lighthouse.safereport.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormDataVO {//사용자가 검색한 건물의 최근 거래가, 건축연도
    private int dealAmount;//거래가
    private int buildYear;//건축연도
    private int score;
}
