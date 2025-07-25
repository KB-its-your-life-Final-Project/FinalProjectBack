package com.lighthouse.safereport.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormData {//사용자가 검색한 건물의 최근 거래가, 건축연도
    private int dealAmount;//거래가 ( 전세가율 점수 계산 목적 )
    private int buildYear;//건축연도 ( 연식 점수 계산 목적 )
    private int score; //최종 점수 = 전세가율점수 + 연식점수
}
