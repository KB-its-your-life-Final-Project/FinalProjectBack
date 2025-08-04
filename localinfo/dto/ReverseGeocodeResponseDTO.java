package com.lighthouse.localinfo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class ReverseGeocodeResponseDTO {


        private String addressName;  // 전체 주소명
        private String legalDongName; // 법정동명 (예: 서초동)
        private String legalDongCode; // 법정동 코드 (예: 1165010800)
        private double latitude;     // 요청 위도
        private double longitude;    // 요청 경도
        // 필요한 다른 필드 (시군구명, 도로명 주소 등
}
