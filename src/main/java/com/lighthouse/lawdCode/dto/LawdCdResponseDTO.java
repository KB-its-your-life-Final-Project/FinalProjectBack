package com.lighthouse.lawdCode.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LawdCdResponseDTO {
    private String regionCd;        // 지역코드
    private String locataddNm;      // 소재지주소명
    private String locallowNm;      // 소재지하위명
    private Integer gridX;
    private Integer gridY;
}