package com.lighthouse.safereport.dto;

import com.lighthouse.safereport.vo.BuildingTypeAndPurpose;
import com.lighthouse.safereport.vo.RentalRatioAndBuildyear;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SafeReportResponseDto {
    private RentalRatioAndBuildyear rentalRatioAndBuildyear;
    private BuildingTypeAndPurpose buildingTypeAndPurpose;
}
