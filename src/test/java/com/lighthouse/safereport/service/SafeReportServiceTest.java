package com.lighthouse.safereport.service;

import com.lighthouse.safereport.mapper.SafeReportMapper;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class SafeReportServiceTest {

    @Mock
    private SafeReportMapper mockMapper;

    @InjectMocks
    private SafeReportService service;

//    @Test
//    void generateSafeReport_shouldCalculateScoreCorrectly() {
//        // given
//        double lat = 37.1234;
//        double lng = 127.5678;
//        int budget = 20000;
//
//        SafeReportRequestDto dto = new SafeReportRequestDto();
//        dto.setLat(lat);
//        dto.setLng(lng);
//        dto.setBudget(budget);
//
//        RentalRatioAndBuildyear dbData = new RentalRatioAndBuildyear();
//        dbData.setDealAmount(25000); // 예: 80% 비율 → ratioScore = 1
//        dbData.setBuildYear(2015);   // 예: 9년차 → ageScore = 1
//
//        when(mockMapper.selectByCoord(lat, lng)).thenReturn(dbData);
//
//        // when
//        RentalRatioAndBuildyear result = service.generateSafeReport(dto);
//
//        // then
//        assertThat(result).isNotNull();
//        assertThat(result.getDealAmount()).isEqualTo(25000);
//        assertThat(result.getBuildYear()).isEqualTo(2015);
//        assertThat(result.getScore()).isEqualTo(1 + 1); // 전세가율 + 연식률
//    }
//
//    @Test
//    void generateSafeReport_shouldReturnNullWhenMapperReturnsNull() {
//        // given
//        SafeReportRequestDto dto = new SafeReportRequestDto();
//        dto.setLat(37.0);
//        dto.setLng(127.0);
//        dto.setBudget(15000);
//
//        when(mockMapper.selectByCoord(37.0, 127.0)).thenReturn(null);
//
//        // when
//        RentalRatioAndBuildyear result = service.generateSafeReport(dto);
//
//        // then
//        assertThat(result).isNull();
//    }
}
