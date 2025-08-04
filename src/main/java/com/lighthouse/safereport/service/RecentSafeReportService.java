package com.lighthouse.safereport.service;

import com.lighthouse.response.ErrorCode;
import com.lighthouse.safereport.dto.RecentSafeReportResponseDto;
import com.lighthouse.safereport.dto.SafeReportRequestDto;
import com.lighthouse.safereport.dto.SafeReportResponseDto;
import com.lighthouse.safereport.entity.RecentSafeReport;
import com.lighthouse.safereport.entity.RentalRatioAndBuildyear;
import com.lighthouse.safereport.mapper.RecentSafeReportMapper;
import com.lighthouse.estate.mapper.EstateMapper;
import com.lighthouse.safereport.converter.RecentSafeReportConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecentSafeReportService {
    
    private static final String RESULT_GRADE_SAFE = "안전";
    private static final String RESULT_GRADE_CAUTION = "주의";
    private static final String RESULT_GRADE_DANGER = "위험";
    private static final int SCORE_SAFE = 10;
    private static final int SCORE_CAUTION = 6;
    
    private final RecentSafeReportMapper recentSafeReportMapper;
    private final SafeReportService safeReportService; // estate 데이터 조회용
    private final EstateMapper estateMapper; // estate_id 조회용
    private final RecentSafeReportConverter converter;
    
    // estate_id와 도로명주소 매핑을 위한 Map (메모리 캐시)
    private final Map<Integer, String> estateRoadAddressMap = new ConcurrentHashMap<>();
    
    // resultGrade 계산 
    private String calculateResultGrade(RentalRatioAndBuildyear rentalRatioAndBuildyear) {
        if (rentalRatioAndBuildyear == null) {
            return "";
        }
        
        int score = rentalRatioAndBuildyear.getScore();
        if (score == SCORE_SAFE) {
            return RESULT_GRADE_SAFE;
        } else if (score == SCORE_CAUTION) {
            return RESULT_GRADE_CAUTION;
        } else {
            return RESULT_GRADE_DANGER;
        }
    }
    
    // 최근 본 안심 레포트 저장
    @Transactional
    public void saveRecentSafeReport(Integer userId, SafeReportRequestDto requestDto) {
        try {
            // estate_id 추출
            Integer estateId = getEstateIdFromLocation(requestDto.getLat(), requestDto.getLng());
            if (estateId == null) {
                log.warn("estate_id를 찾을 수 없어 최근 본 안심레포트 저장을 건너뜁니다: lat={}, lng={}", 
                    requestDto.getLat(), requestDto.getLng());
                return;
            }
            
            // 안심레포트 데이터 생성하여 저장 조건 확인
            SafeReportResponseDto responseDto = safeReportService.generateCompleteSafeReport(requestDto);
            if (responseDto == null || !safeReportService.shouldSaveToRecentReports(responseDto)) {
                log.info("저장 조건을 만족하지 않아 최근 본 안심레포트 저장을 건너뜁니다: estateId={}", estateId);
                return;
            }
            
            // 중복 체크 및 저장/업데이트
            RecentSafeReport existingReport = recentSafeReportMapper.findByUserIdAndEstateId(userId, estateId);
            if (existingReport != null) {
                updateExistingReport(existingReport, requestDto);
            } else {
                createNewReport(userId, estateId, requestDto);
            }
        } catch (Exception e) {
            log.error("최근 본 안심레포트 저장 실패: {}", e.getMessage(), e);
            throw new RuntimeException(ErrorCode.RECENT_SAFEREPORT_SAVE_FAIL.getMessage());
        }
    }
    
    // 기존 레포트 업데이트
    private void updateExistingReport(RecentSafeReport existingReport, SafeReportRequestDto requestDto) {
        // 안심레포트 데이터 생성하여 저장 조건 확인
        SafeReportResponseDto responseDto = safeReportService.generateCompleteSafeReport(requestDto);
        if (responseDto == null || !safeReportService.shouldSaveToRecentReports(responseDto)) {
            log.info("저장 조건을 만족하지 않아 기존 레포트를 논리적 삭제합니다: estateId={}", existingReport.getEstateId());
            existingReport.setIsDelete(1); // 논리적 삭제
            recentSafeReportMapper.updateRecentSafeReport(existingReport);
            return;
        }
        
        String resultGrade = calculateResultGrade(responseDto.getRentalRatioAndBuildyear());
        
        existingReport.setBudget(requestDto.getBudget());
        existingReport.setResultGrade(resultGrade);
        existingReport.setUpdatedAt(LocalDateTime.now());
        existingReport.setIsDelete(0); // 삭제된 경우 복구
        recentSafeReportMapper.updateRecentSafeReport(existingReport);
    }
    
    // 새 레포트 생성
    private void createNewReport(Integer userId, Integer estateId, SafeReportRequestDto requestDto) {
        // 안심레포트 데이터 생성하여 저장 조건 확인
        SafeReportResponseDto responseDto = safeReportService.generateCompleteSafeReport(requestDto);
        if (responseDto == null || !safeReportService.shouldSaveToRecentReports(responseDto)) {
            log.info("저장 조건을 만족하지 않아 새 레포트 생성을 건너뜁니다: estateId={}", estateId);
            return;
        }
        
        String resultGrade = calculateResultGrade(responseDto.getRentalRatioAndBuildyear());
        
        RecentSafeReport newReport = buildRecentSafeReport(userId, estateId, requestDto, resultGrade);
        
        // 도로명주소를 Map에 캐시
        estateRoadAddressMap.put(estateId, requestDto.getRoadAddress());
        
        recentSafeReportMapper.insertRecentSafeReport(newReport);
    }
    
    // RecentSafeReport 생성
    private RecentSafeReport buildRecentSafeReport(Integer userId, Integer estateId, 
                                                  SafeReportRequestDto requestDto, String resultGrade) {
        return RecentSafeReport.builder()
            .userId(userId)
            .estateId(estateId)
            .budget(requestDto.getBudget())
            .resultGrade(resultGrade)
            .isDelete(0)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
    }
    
    //최근 본 안심레포트 목록 모두 조회 최신순
    public List<RecentSafeReportResponseDto> getRecentReports(Integer userId) {
        List<RecentSafeReport> reports = recentSafeReportMapper.findByUserIdOrderByCreatedAtDesc(userId);
        return reports.stream()
            .map(this::convertToResponseDto)
            .filter(dto -> dto != null) // null인 경우 필터링
            .collect(Collectors.toList());
    }
    
    //최근 본 안심레포트 상세 조회 (estate_api_integration_tbl에서 실시간 데이터 조회)
    public SafeReportResponseDto getRecentReportDetail(Integer id, Integer userId) {
        RecentSafeReport report = recentSafeReportMapper.findByIdAndUserId(id, userId);
        if (report == null) {
            return null;
        }
        
        // estate 정보 조회
        var estate = getEstateById(report.getEstateId());
        if (estate == null) {
            return null;
        }
        
        // SafeReportRequestDto 생성
        SafeReportRequestDto requestDto = buildSafeReportRequestDto(report, estate);
        
        // 실시간으로 안심레포트 데이터 생성
        return generateSafeReportResponse(requestDto);
    }
    
    // estate 정보 조회
    private com.lighthouse.estate.entity.Estate getEstateById(Integer estateId) {
        com.lighthouse.estate.entity.Estate estate = estateMapper.getEstateById(estateId);
        if (estate == null) {
            log.warn("estate 정보를 찾을 수 없습니다: estateId={}", estateId);
        }
        return estate;
    }
    
    // SafeReportRequestDto 생성
    private SafeReportRequestDto buildSafeReportRequestDto(RecentSafeReport report, com.lighthouse.estate.entity.Estate estate) {
        String roadAddress = estateRoadAddressMap.getOrDefault(report.getEstateId(), estate.getJibunAddr());
        
        SafeReportRequestDto requestDto = new SafeReportRequestDto();
        requestDto.setBuildingName(estate.getBuildingName());
        requestDto.setRoadAddress(roadAddress);
        requestDto.setBudget(report.getBudget());
        requestDto.setLat(estate.getLatitude().doubleValue());
        requestDto.setLng(estate.getLongitude().doubleValue());
        
        return requestDto;
    }
    
    // SafeReportResponseDto 생성
    private SafeReportResponseDto generateSafeReportResponse(SafeReportRequestDto requestDto) {
        var rentalRatioAndBuildyear = safeReportService.generateSafeReport(requestDto);
        var buildingInfo = safeReportService.getBuildingInfo(requestDto);
        
        return new SafeReportResponseDto(
            rentalRatioAndBuildyear, 
            buildingInfo != null ? buildingInfo.getViolationStatus() : null, 
            buildingInfo != null ? buildingInfo.getFloorAndPurposeList() : null
        );
    }
    
     // 최근 본 안심레포트 삭제   
    @Transactional
    public void deleteRecentReport(Integer id, Integer userId) {
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
    
     //위도/경도로 estate_id 조회
    private Integer getEstateIdFromLocation(double lat, double lng) {
        try {
            // EstateMapper를 사용하여 위도/경도로 estate 정보 조회
            var estate = estateMapper.getEstateByLatLng(lat, lng);
            if (estate != null) {
                return estate.getId(); // estate_id 반환
            }
            log.warn("위도/경도에 해당하는 estate 정보를 찾을 수 없습니다: lat={}, lng={}", lat, lng);
            return null;
        } catch (Exception e) {
            log.error("estate_id 조회 실패: {}", e.getMessage(), e);
            return null;
        }
    }
  
    //RecentSafeReport를 RecentSafeReportResponseDto로 변환
    private RecentSafeReportResponseDto convertToResponseDto(RecentSafeReport report) {
        // estate_id로 estate_api_integration_tbl에서 buildingName 조회
        var estate = estateMapper.getEstateById(report.getEstateId());
        if (estate == null) {
            log.warn("estate 정보를 찾을 수 없습니다: estateId={}", report.getEstateId());
            return null;
        }     
        // Map에서 도로명주소 조회 (없으면 지번주소 사용)
        String roadAddress = estateRoadAddressMap.getOrDefault(report.getEstateId(), estate.getJibunAddr());
        
        return converter.toResponseDto(report, estate, roadAddress);
    }
} 