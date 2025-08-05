package com.lighthouse.lawdCode.entity;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class LawdCd {
    private String regionCd;    //지역코드
    private String sidoCd;  //시도 코드
    private String sggCd;   // 시군구 코드
    private String umdCd;   //읍면동 코드
    private String riCd;    //리 코드
    private String locatjuminCd;    //소재지 주민 코드
    private String locatjijukCd;    // 소재지 지적코드
    private String locataddNm;  //소재지 주소명
    private Integer locatOrder; //소재지 정렬 순서
    private String locatRm; //소재지 비고 (설명 등)
    private String locathighCd;    // 상위 행정구역 코드
    private String locallowNm;  //하위 행정구역 명칭
    private String adptDe;  // 적용 일자(YYYYMMDD)
    private Integer gridX;
    private Integer gridY;
    private LocalDateTime regDate;
    private String isDeleted;
}
