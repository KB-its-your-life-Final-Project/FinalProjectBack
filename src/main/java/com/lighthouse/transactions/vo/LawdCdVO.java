package com.lighthouse.transactions.vo;

import lombok.Getter;
import lombok.Setter;

// 법정동코드 데이터 객체
@Getter
@Setter
public class LawdCdVO {
    private String regionCd;        // 지역코드
    private String sidoCd;          // 시도코드
    private String sggCd;           // 시군구코드
    private String umdCd;           // 읍면동코드
    private String riCd;            // 리코드
    private String locatjuminCd;    // 소재지주민코드
    private String locatjijukCd;    // 소재지지적코드
    private String locataddNm;      // 소재지주소명
    private Integer locatOrder;     // 소재지순서
    private String locatRm;         // 소재지비고
    private String locathighCd;     // 소재지상위코드
    private String locallowNm;      // 소재지하위명
    private String adptDe;          // 적용일자
}