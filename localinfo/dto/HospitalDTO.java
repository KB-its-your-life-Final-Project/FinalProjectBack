package com.lighthouse.localinfo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HospitalDTO {
    private String regionCd;       // 법정동 코드
    private String regionName;     // 법정동 이름
    private String locataddNm;     // 풀 주소명
    private Long totalCount;
}