package com.lighthouse.safereport.service;

import com.lighthouse.response.ErrorCode;
import com.lighthouse.safereport.dto.RecentSafeReportResponseDto;
import com.lighthouse.safereport.dto.RecentSafeReportDetailResponseDto;
import com.lighthouse.safereport.dto.SafeReportRequestDto;
import com.lighthouse.safereport.dto.SafeReportResponseDto;
import com.lighthouse.safereport.entity.RecentSafeReport;
import com.lighthouse.safereport.mapper.RecentSafeReportMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecentSafeReportService {
    
    private final RecentSafeReportMapper recentSafeReportMapper;
    private final SafeReportService safeReportService; // estate 데이터 조회용
    
    /**
     * 최근 본 안심레포트 저장
     */
    @Transactional
    public void saveRecentSafeReport(Long userId, SafeReportRequestDto requestDto, SafeReportResponseDto responseDto) {
        try {
            // estate_id 추출 (위도/경도로 estate_api_integration_tbl에서 조회)
            Long estateId = getEstateIdFromLocation(requestDto.getLat(), requestDto.getLng());
            
            // 중복 체크 (같은 위치의 안심레포트가 이미 있는지)
            RecentSafeReport existingReport = recentSafeReportMapper.findByUserIdAndLocation(
                userId, BigDecimal.valueOf(requestDto.getLat()), BigDecimal.valueOf(requestDto.getLng()));
            
            if (existingReport != null) {
                // 기존 데이터 업데이트
                existingReport.setBudget(requestDto.getBudget());
                existingReport.setCreatedAt(LocalDateTime.now());
                recentSafeReportMapper.updateRecentSafeReport(existingReport);
            } else {
                // 새 데이터 저장
                RecentSafeReport newReport = RecentSafeReport.builder()
                    .userId(userId)
                    .estateId(estateId)
                    .buildingName(requestDto.getBuildingName())
                    .roadAddress(requestDto.getRoadAddress())
                    .latitude(requestDto.getLat())
                    .longitude(requestDto.getLng())
                    .budget(requestDto.getBudget())
                    .resultGrade("완료")
                    .isDelete(0)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
                
                recentSafeReportMapper.insertRecentSafeReport(newReport);
            }
        } catch (Exception e) {
            log.error("최근 본 안심레포트 저장 실패: {}", e.getMessage(), e);
            throw new RuntimeException(ErrorCode.RECENT_SAFEREPORT_SAVE_FAIL.getMessage());
        }
    }
    
    /**
     * 최근 본 안심레포트 목록 조회
     */
    public List<RecentSafeReportResponseDto> getRecentReports(Long userId) {
        List<RecentSafeReport> reports = recentSafeReportMapper.findByUserIdOrderByCreatedAtDesc(userId);
        return reports.stream()
            .map(this::convertToResponseDto)
            .collect(Collectors.toList());
    }
    
    /**
     * 최근 본 안심레포트 상세 조회 (estate_api_integration_tbl에서 실시간 데이터 조회)
     */
    public RecentSafeReportDetailResponseDto getRecentReportDetail(Long id, Long userId) {
        RecentSafeReport report = recentSafeReportMapper.findByIdAndUserId(id, userId);
        if (report == null) {
            return null;
        }
        
        // estate_api_integration_tbl에서 최신 데이터 조회
        SafeReportRequestDto requestDto = new SafeReportRequestDto();
        requestDto.setLat(report.getLatitude().doubleValue());
        requestDto.setLng(report.getLongitude().doubleValue());
        requestDto.setBudget(report.getBudget());
        requestDto.setBuildingName(report.getBuildingName());
        requestDto.setRoadAddress(report.getRoadAddress());
        
        // 실시간으로 안심레포트 데이터 생성
        SafeReportResponseDto safeReportData = generateSafeReportResponse(requestDto);
        
        return convertToDetailResponseDto(report, safeReportData);
    }
    
    /**
     * 최근 본 안심레포트 삭제
     */
    @Transactional
    public void deleteRecentReport(Long id, Long userId) {
        try {
            // 삭제 전 존재 여부 확인
            RecentSafeReport report = recentSafeReportMapper.findByIdAndUserId(id, userId);
            if (report == null) {
                throw new RuntimeException(ErrorCode.RECENT_SAFEREPORT_NOT_FOUND.getMessage());
            }
            
            recentSafeReportMapper.deleteByIdAndUserId(id, userId);
        } catch (Exception e) {
            log.error("최근 본 안심레포트 삭제 실패: {}", e.getMessage(), e);
            throw new RuntimeException(ErrorCode.RECENT_SAFEREPORT_DELETE_FAIL.getMessage());
        }
    }
    
    /**
     * 위도/경도로 estate_id 조회
     */
    private Long getEstateIdFromLocation(double lat, double lng) {
        // estate_api_integration_tbl에서 위도/경도로 estate_id 조회
        // 실제 구현에서는 해당 매퍼 메서드 호출
        return 1L; // 임시 반환값
    }
    
    /**
     * SafeReportRequestDto로부터 SafeReportResponseDto 생성
     */
    private SafeReportResponseDto generateSafeReportResponse(SafeReportRequestDto requestDto) {
        // SafeReportService의 메서드들을 사용하여 실시간 데이터 생성
        var rentalRatioAndBuildyear = safeReportService.generateSafeReport(requestDto);
        var buildingInfo = safeReportService.getBuildingInfo(requestDto);
        
        return new SafeReportResponseDto(
            rentalRatioAndBuildyear, 
            buildingInfo != null ? buildingInfo.getViolationStatus() : null, 
            buildingInfo != null ? buildingInfo.getFloorAndPurposeList() : null
        );
    }
    
    /**
     * RecentSafeReport를 RecentSafeReportResponseDto로 변환
     */
    private RecentSafeReportResponseDto convertToResponseDto(RecentSafeReport report) {
        return RecentSafeReportResponseDto.builder()
            .id(report.getId())
            .buildingName(report.getBuildingName())
            .jibunAddress(report.getJibunAddress())
            .roadAddress(report.getRoadAddress())
            .diagnosisStatus(report.getDiagnosisStatus())
            .diagnosisDate(report.getDiagnosisDate())
            .createdAt(report.getCreatedAt())
            .build();
    }
    
    /**
     * RecentSafeReport를 RecentSafeReportDetailResponseDto로 변환 (실시간 데이터 포함)
     */
    private RecentSafeReportDetailResponseDto convertToDetailResponseDto(RecentSafeReport report, SafeReportResponseDto safeReportData) {
        return RecentSafeReportDetailResponseDto.builder()
            .id(report.getId())
            .buildingName(report.getBuildingName())
            .dongName(report.getDongName())
            .jibunAddress(report.getJibunAddress())
            .roadAddress(report.getRoadAddress())
            .latitude(report.getLatitude())
            .longitude(report.getLongitude())
            .budget(report.getBudget())
            .reportData(convertToJson(safeReportData)) // 실시간 조회한 데이터
            .diagnosisStatus(report.getDiagnosisStatus())
            .diagnosisDate(report.getDiagnosisDate())
            .createdAt(report.getCreatedAt())
            .build();
    }
    
    /**
     * SafeReportResponseDto를 JSON 문자열로 변환
     */
    private String convertToJson(SafeReportResponseDto responseDto) {
        // 실제로는 ObjectMapper를 사용하여 JSON으로 변환
        // 임시로 간단한 문자열 반환
        return "report_data_json";
    }
} 