package com.lighthouse.safereport.service;

import com.lighthouse.buildingRegister.mapper.BuildingRegisterMapper;
import com.lighthouse.buildingRegister.service.BuildingRegisterService;
import com.lighthouse.common.geocoding.service.GeoCodingService;
import com.lighthouse.estate.dto.EstateDTO;
import com.lighthouse.estate.entity.EstateSales;
import com.lighthouse.estate.service.EstateService;
import com.lighthouse.safereport.dto.SafeReportRequestDto;
import com.lighthouse.safereport.dto.SafeReportResponseDto;
import com.lighthouse.safereport.entity.RentalRatioAndBuildyear;
import com.lighthouse.safereport.mapper.SafeReportMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class SafeReportServiceTest {

    @Mock
    private SafeReportMapper safeReportMapper;

    @Mock
    private EstateService estateService;

    @Mock
    private BuildingRegisterMapper buildingRegisterMapper;

    @Mock
    private GeoCodingService geocodingService;

    @InjectMocks
    private SafeReportService service;

    private SafeReportRequestDto requestDto;
    private EstateDTO estateDTO;
    private EstateSales estateSales;

    @BeforeEach
    void setup(){
        requestDto = new SafeReportRequestDto();
        requestDto.setLat(37.5665);
        requestDto.setLng(126.9780);
        requestDto.setBudget(20000);
        requestDto.setRoadAddress("서울특별시 강남구 도산대로 123");

        estateDTO = new EstateDTO();
        estateDTO.setId(1);
        estateDTO.setBuildYear(2015);
        estateDTO.setBuildingName("테스트 건물");

        estateSales = new EstateSales();
        estateSales.setId(1);
        estateSales.setEstateId(1);
        estateSales.setDealAmount(25000);
        estateSales.setTradeType(1);
    }

    @Test
    @DisplayName("안전 역전세율(80%이하) 일 때 안전 등급 반환")
    void generateSafeReport_returnSafeGrade_whenRentalRaioBelow80(){
        requestDto.setBudget(20000);
        when(estateService.getEstateByLatLng(anyDouble(), anyDouble())).thenReturn(estateDTO);
        when(safeReportMapper.getSalesByEstateIdWithTradeType(any())).thenReturn(estateSales);

        RentalRatioAndBuildyear result = service.generateSafeReport(requestDto);

        assertThat(result).isNotNull();
        assertThat(result.getDealAmount()).isEqualTo(25000);
        assertThat(result.getBuildYear()).isEqualTo(2015);
        assertThat(result.getReverseRentalRatio()).isEqualTo(80.0);
        assertThat(result.getScore()).isEqualTo(10);
    }

    @Test
    @DisplayName("주의 역전세율(80~90%) 일 때 주의 등급 반환")
    void generateSafeReport_returnCautionGrade_whenRentalRaioBetween80And90(){
        requestDto.setBudget(22500);
        when(estateService.getEstateByLatLng(anyDouble(), anyDouble())).thenReturn(estateDTO);
        when(safeReportMapper.getSalesByEstateIdWithTradeType(any())).thenReturn(estateSales);

        RentalRatioAndBuildyear result = service.generateSafeReport(requestDto);

        assertThat(result).isNotNull();
        assertThat(result.getReverseRentalRatio()).isEqualTo(90.0);
        assertThat(result.getScore()).isEqualTo(6);
    }

    @Test
    @DisplayName("위험 역전세율(90% 초과) 일 때 위험 등급 반환")
    void generateSafeReport_returnDangerGrade_whenRentalRaioOver90(){
        requestDto.setBudget(25000);
        when(estateService.getEstateByLatLng(anyDouble(), anyDouble())).thenReturn(estateDTO);
        when(safeReportMapper.getSalesByEstateIdWithTradeType(any())).thenReturn(estateSales);

        RentalRatioAndBuildyear result = service.generateSafeReport(requestDto);

        assertThat(result).isNotNull();
        assertThat(result.getReverseRentalRatio()).isEqualTo(100.0);
        assertThat(result.getScore()).isEqualTo(3);
    }

    @Test
    @DisplayName("매매 데이터 없으면 dealAmount 0으로 설정")
    void generateSafeReport_SetDealAmountZero_whenNoSalesData(){
        when(estateService.getEstateByLatLng(anyDouble(), anyDouble())).thenReturn(estateDTO);
        when(safeReportMapper.getSalesByEstateIdWithTradeType(any())).thenReturn(null);

        RentalRatioAndBuildyear result = service.generateSafeReport(requestDto);

        assertThat(result).isNotNull();
        assertThat(result.getDealAmount()).isEqualTo(0);
        assertThat(result.getReverseRentalRatio()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("estate 정보 없으면 null 반환")
    void generateSafeReport_returnNull_whenEstateNotFound(){
        when(estateService.getEstateByLatLng(anyDouble(), anyDouble())).thenReturn(null);

        RentalRatioAndBuildyear result = service.generateSafeReport(requestDto);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("안심 레포트 정상적으로 생성")
    void generateSafeReport_ReturnValidResponse(){
        when(estateService.getEstateByLatLng(anyDouble(), anyDouble())).thenReturn(estateDTO);
        when(safeReportMapper.getSalesByEstateIdWithTradeType(any())).thenReturn(estateSales);
        when(geocodingService.getCoordinateFromAddress(anyString())).thenReturn(Map.of("lat", 37.5665, "lng", 126.9780));
        when(buildingRegisterMapper.getBuildingRegisterByLocation(anyDouble(), anyDouble())).thenReturn(null);
        when(buildingRegisterMapper.getBuildingRegisterWithStatusByLocation(anyDouble(), anyDouble())).thenReturn(List.of());

        SafeReportResponseDto result = service.generateCompleteSafeReport(requestDto);

        assertThat(result).isNotNull();
        assertThat(result.getRentalRatioAndBuildyear()).isNotNull();
        assertThat(result.getRentalRatioAndBuildyear().getDealAmount()).isEqualTo(25000);
    }

    @Test
    @DisplayName("최근 본 안심 레포트 저장 유효성 판단")
    void shouldSaveRecentReports_whenValidData(){

        RentalRatioAndBuildyear rentalRatioAndBuildyear = new RentalRatioAndBuildyear();
        rentalRatioAndBuildyear.setDealAmount(25000);
        rentalRatioAndBuildyear.setReverseRentalRatio(80.0);

        SafeReportResponseDto responseDto = new SafeReportResponseDto(rentalRatioAndBuildyear, null, null);

        boolean result = service.shouldSaveToRecentReports(responseDto);
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("최근 본 안심 레포트 저장 유효성 거부 매매 데이터 없으면 저장 불가가")
    void shouldNotSaveRecentReports_whenInvalidData(){
        RentalRatioAndBuildyear rentalRatioAndBuildyear = new RentalRatioAndBuildyear();
        rentalRatioAndBuildyear.setDealAmount(0);
        rentalRatioAndBuildyear.setReverseRentalRatio(0.0);

        SafeReportResponseDto responseDto = new SafeReportResponseDto(rentalRatioAndBuildyear, null, null);

        boolean result = service.shouldSaveToRecentReports(responseDto);
        assertThat(result).isFalse();
    }
}
