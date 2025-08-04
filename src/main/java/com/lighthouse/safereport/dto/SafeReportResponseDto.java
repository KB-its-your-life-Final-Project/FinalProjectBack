package com.lighthouse.safereport.dto;

import com.lighthouse.safereport.entity.RentalRatioAndBuildyear;
import com.lighthouse.safereport.entity.ViolationStatus;
import com.lighthouse.safereport.entity.FloorAndPurpose;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "안전 리포트 응답 데이터")
public class SafeReportResponseDto {
    @ApiModelProperty(value = "건축년도, 거래금액, 역전세율 정보")
    private RentalRatioAndBuildyear rentalRatioAndBuildyear; // 건축년도, 거래금액, 역전세율
    
    @ApiModelProperty(value = "위반 여부 정보")
    private ViolationStatus violationStatus;              // 위반 여부
    
    @ApiModelProperty(value = "층수와 용도 목록")
    private List<FloorAndPurpose> floorAndPurposeList;      // 층수와 용도 목록
}
