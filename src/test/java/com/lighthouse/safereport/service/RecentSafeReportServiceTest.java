package com.lighthouse.safereport.service;

import com.lighthouse.estate.entity.Estate;
import com.lighthouse.estate.mapper.EstateMapper;
import com.lighthouse.safereport.converter.RecentSafeReportConverter;
import com.lighthouse.safereport.dto.RecentSafeReportResponseDto;
import com.lighthouse.safereport.dto.SafeReportRequestDto;
import com.lighthouse.safereport.dto.SafeReportResponseDto;
import com.lighthouse.safereport.entity.RecentSafeReport;
import com.lighthouse.safereport.entity.RentalRatioAndBuildyear;
import com.lighthouse.safereport.mapper.RecentSafeReportMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecentSafeReportServiceTest {

    @Mock
    private RecentSafeReportMapper recentSafeReportMapper;

    @Mock
    private SafeReportService safeReportService;

    @Mock
    private EstateMapper estateMapper;

    @Mock
    private RecentSafeReportConverter converter;

    @InjectMocks
    private RecentSafeReportService service;

    private SafeReportRequestDto requestDto;
    private RecentSafeReport recentSafeReport;
    private Estate estate;

    @BeforeEach
    void setup() {
        requestDto = new SafeReportRequestDto();
        requestDto.setLat(37.5665);
        requestDto.setLng(126.9780);
        requestDto.setBudget(20000);
        requestDto.setRoadAddress("서울특별시 강남구 도산대로 123");

        recentSafeReport = new RecentSafeReport();
        recentSafeReport.setId(1);
        recentSafeReport.setMemberId(1);
        recentSafeReport.setEstateId(1);
        recentSafeReport.setBudget(20000);
        recentSafeReport.setResultGrade("안전");
        recentSafeReport.setIsDelete(0);
        recentSafeReport.setCreatedAt(LocalDateTime.now());
        recentSafeReport.setUpdatedAt(LocalDateTime.now());

        estate = new Estate();
        estate.setId(1);
        estate.setBuildingName("테스트 건물");
        estate.setLatitude(37.5665);
        estate.setLongitude(126.9780);
        estate.setJibunAddr("서울특별시 강남구 도산대로 123");

        RentalRatioAndBuildyear rentalRatioAndBuildyear = new RentalRatioAndBuildyear();
        rentalRatioAndBuildyear.setDealAmount(25000);
        rentalRatioAndBuildyear.setReverseRentalRatio(80.0);
        rentalRatioAndBuildyear.setScore(10);
    }

    @Test
    @DisplayName("새로운 최근 본 안심레포트 저장")
    void saveRecentSafeReport_shouldCreateNewReport_whenNoExistingReport() {
        // given
        when(estateMapper.getEstateByLatLng(anyDouble(), anyDouble())).thenReturn(estate);
        when(recentSafeReportMapper.findByUserIdAndEstateId(anyInt(), anyInt())).thenReturn(null);
        RentalRatioAndBuildyear mockRentalRatio = new RentalRatioAndBuildyear();
        mockRentalRatio.setDealAmount(25000);
        mockRentalRatio.setBuildYear(2015);
        mockRentalRatio.setReverseRentalRatio(80.0);
        mockRentalRatio.setScore(10);
        when(safeReportService.generateSafeReport(any())).thenReturn(mockRentalRatio);

        // when
        service.saveRecentSafeReport(1, requestDto);

        // then
        verify(recentSafeReportMapper).insertRecentSafeReport(any(RecentSafeReport.class));
        verify(recentSafeReportMapper, never()).updateRecentSafeReport(any(RecentSafeReport.class));
    }

    @Test
    @DisplayName("기존 최근 본 안심레포트를 업데이트")
    void saveRecentSafeReport_shouldUpdateExistingReport_whenReportExists() {
        // given
        when(estateMapper.getEstateByLatLng(anyDouble(), anyDouble())).thenReturn(estate);
        when(recentSafeReportMapper.findByUserIdAndEstateId(anyInt(), anyInt())).thenReturn(recentSafeReport);
        RentalRatioAndBuildyear mockRentalRatio = new RentalRatioAndBuildyear();
        mockRentalRatio.setDealAmount(25000);
        mockRentalRatio.setBuildYear(2015);
        mockRentalRatio.setReverseRentalRatio(80.0);
        mockRentalRatio.setScore(10);
        when(safeReportService.generateSafeReport(any())).thenReturn(mockRentalRatio);

        // when
        service.saveRecentSafeReport(1, requestDto);

        // then
        verify(recentSafeReportMapper).updateRecentSafeReport(any(RecentSafeReport.class));
        verify(recentSafeReportMapper, never()).insertRecentSafeReport(any(RecentSafeReport.class));
        assertThat(recentSafeReport.getIsDelete()).isEqualTo(0);
    }

    @Test
    @DisplayName("삭제된 최근 본 안심레포트를 복구한다")
    void saveRecentSafeReport_shouldRestoreDeletedReport_whenReportWasDeleted() {
        // given
        recentSafeReport.setIsDelete(1); // 삭제된 상태
        when(estateMapper.getEstateByLatLng(anyDouble(), anyDouble())).thenReturn(estate);
        when(recentSafeReportMapper.findByUserIdAndEstateId(anyInt(), anyInt())).thenReturn(recentSafeReport);
        when(safeReportService.generateSafeReport(any())).thenReturn(
                RentalRatioAndBuildyear.builder()
                        .dealAmount(25000)
                        .buildYear(2015)
                        .reverseRentalRatio(80.0)
                        .buildYearScore(1)
                        .score(10)
                        .build()
        );

        // when
        service.saveRecentSafeReport(1, requestDto);

        // then
        verify(recentSafeReportMapper).updateRecentSafeReport(any(RecentSafeReport.class));
        assertThat(recentSafeReport.getIsDelete()).isEqualTo(0);
    }

    @Test
    @DisplayName("estate_id를 찾을 수 없으면 저장하지 않는다")
    void saveRecentSafeReport_shouldNotSave_whenEstateIdNotFound() {
        // given
        when(estateMapper.getEstateByLatLng(anyDouble(), anyDouble())).thenReturn(null);

        // when
        service.saveRecentSafeReport(1, requestDto);

        // then
        verify(recentSafeReportMapper, never()).insertRecentSafeReport(any(RecentSafeReport.class));
        verify(recentSafeReportMapper, never()).updateRecentSafeReport(any(RecentSafeReport.class));
    }

    @Test
    @DisplayName("사용자의 최근 본 안심레포트 목록 조회")
    void getRecentReports_shouldReturnUserReports() {
        // given
        List<RecentSafeReport> reports = Arrays.asList(recentSafeReport);
        when(recentSafeReportMapper.findByUserIdOrderByCreatedAtDesc(anyInt())).thenReturn(reports);
        when(estateMapper.getEstateById(anyInt())).thenReturn(estate);
        when(converter.toResponseDto(any(RecentSafeReport.class), any(Estate.class), anyString()))
                .thenReturn(new RecentSafeReportResponseDto());

        // when
        List<RecentSafeReportResponseDto> result = service.getRecentReports(1);

        // then
        assertThat(result).isNotNull();
        verify(recentSafeReportMapper).findByUserIdOrderByCreatedAtDesc(1);
    }

    @Test
    @DisplayName("최근 본 안심레포트 상세 정보 조회")
    void getRecentReportDetail_shouldReturnDetailedReport() {
        // given
        when(recentSafeReportMapper.findByIdAndUserId(anyInt(), anyInt())).thenReturn(recentSafeReport);
        when(estateMapper.getEstateById(anyInt())).thenReturn(estate);
        when(safeReportService.generateSafeReport(any())).thenReturn(
                RentalRatioAndBuildyear.builder()
                        .dealAmount(25000)
                        .buildYear(2015)
                        .reverseRentalRatio(80.0)
                        .buildYearScore(1)
                        .score(10)
                        .build()
        );
        when(safeReportService.getBuildingInfo(any())).thenReturn(null);

        // when
        SafeReportResponseDto result = service.getRecentReportDetail(1, 1);

        // then
        assertThat(result).isNotNull();
        verify(safeReportService).generateSafeReport(any());
        verify(safeReportService).getBuildingInfo(any());
    }

    @Test
    @DisplayName("최근 본 안심레포트 삭제")
    void deleteRecentReport_shouldSoftDeleteReport() {
        // given
        when(recentSafeReportMapper.findByIdAndUserId(anyInt(), anyInt())).thenReturn(recentSafeReport);

        // when
        service.deleteRecentReport(1, 1);

        // then
        verify(recentSafeReportMapper).deleteByIdAndUserId(1, 1);
    }


}