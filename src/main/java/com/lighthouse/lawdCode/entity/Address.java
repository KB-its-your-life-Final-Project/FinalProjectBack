package com.lighthouse.lawdCode.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Address {
    private String regionCd; // 지역코드 (법정동코드, 유일값)
    private String sidoCd; // 시도 코드 (예: 서울=11)
    private String sggCd; // 시군구 코드
    private String umdCd; // 읍면동 코드
    private String riCd; // 리코드
    private String locatjuminCd; // 소재지 주민 코드
    private String locatjijukCd; // 소재지 지적 코드
    private String locataddNm; // 소재지 주소명 (전체 주소 형태)
    private Integer locatOrder; // 소재지 정렬 순서
    private String locatRm; // 소재지 비고 (설명 등)
    private String locathighCd; // 상위 행정구역 코드
    private String locallowNm; // 하위 행정구역 명칭 (동 이름)
}
