package com.lighthouse.safereport.dto;

import com.lighthouse.safereport.vo.RentalRatioAndBuildyear;
import com.lighthouse.safereport.vo.ViolationStatusVO;
import com.lighthouse.safereport.vo.FloorAndPurpose;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SafeReportResponseDto {
    private RentalRatioAndBuildyear rentalRatioAndBuildyear; // 건축년도, 거래금액, 역전세율
    private ViolationStatusVO violationStatus;              // 위반 여부
    private List<FloorAndPurpose> floorAndPurposeList;      // 층수와 용도 목록
}
