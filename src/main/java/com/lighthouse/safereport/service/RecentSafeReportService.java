package com.lighthouse.safereport.service;

import com.lighthouse.response.ErrorCode;
import com.lighthouse.safereport.dto.RecentSafeReportResponseDto;
import com.lighthouse.safereport.dto.RecentSafeReportDetailResponseDto;
import com.lighthouse.safereport.dto.SafeReportRequestDto;
import com.lighthouse.safereport.dto.SafeReportResponseDto;
import com.lighthouse.safereport.entity.RecentSafeReport;
import com.lighthouse.safereport.mapper.RecentSafeReportMapper;
import com.lighthouse.estate.mapper.EstateMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecentSafeReportService {
    
    private final RecentSafeReportMapper recentSafeReportMapper;
    private final SafeReportService safeReportService; // estate 데이터 조회용
    private final EstateMapper estateMapper; // estate_id 조회용
    
    // estate_id와 도로명주소 매핑을 위한 Map (메모리 캐시)
    private final Map<Integer, String> estateRoadAddressMap = new ConcurrentHashMap<>();
    
    // 최근 본 안심 레포트 저장
    @Transactional
    public void saveRecentSafeReport(Integer userId, SafeReportRequestDto requestDto) {
        try {
            // estate_id 추출 (위도/경도로 estate_api_integration_tbl에서 조회)
            Integer estateId = getEstateIdFromLocation(requestDto.getLat(), requestDto.getLng());
            
            if (estateId == null) {
                log.warn("estate_id를 찾을 수 없어 최근 본 안심레포트 저장을 건너뜁니다: lat={}, lng={}", 
                    requestDto.getLat(), requestDto.getLng());
                return;
            }
            
            // 중복 체크 (같은 estateId의 안심레포트가 이미 있는지- 이전에 열람한 적 있는 건물인지)
            RecentSafeReport existingReport = recentSafeReportMapper.findByUserIdAndEstateId(userId, estateId);
            
                         if (existingReport != null) {
                 // 기존 데이터 업데이트 (삭제된 것도 복구)
                 existingReport.setBudget(requestDto.getBudget());
                 existingReport.setUpdatedAt(LocalDateTime.now());
                 existingReport.setIsDelete(0); // 삭제된 경우 복구
                 recentSafeReportMapper.updateRecentSafeReport(existingReport);
                          } else {
                 // 새 데이터 저장 (estate_id만 저장, 도로명주소는 Map에 캐시)
                 RecentSafeReport newReport = RecentSafeReport.builder()
                     .userId(userId)
                     .estateId(estateId)
                     .budget(requestDto.getBudget())
                     .resultGrade("완료")
                     .isDelete(0)
                     .createdAt(LocalDateTime.now())
                     .updatedAt(LocalDateTime.now())
                     .build();
                 
                 // 도로명주소를 Map에 캐시
                 estateRoadAddressMap.put(estateId, requestDto.getRoadAddress());
                 
                 recentSafeReportMapper.insertRecentSafeReport(newReport);
             }
        } catch (Exception e) {
            log.error("최근 본 안심레포트 저장 실패: {}", e.getMessage(), e);
            throw new RuntimeException(ErrorCode.RECENT_SAFEREPORT_SAVE_FAIL.getMessage());
        }
    }
    
    /**
     * 최근 본 안심레포트 목록 모두 조회 최신순
     */
    public List<RecentSafeReportResponseDto> getRecentReports(Integer userId) {
        List<RecentSafeReport> reports = recentSafeReportMapper.findByUserIdOrderByCreatedAtDesc(userId);
        return reports.stream()
            .map(this::convertToResponseDto)
            .filter(dto -> dto != null) // null인 경우 필터링
            .collect(Collectors.toList());
    }
    
    /**
     * 최근 본 안심레포트 상세 조회 (estate_api_integration_tbl에서 실시간 데이터 조회)
     */
    public SafeReportResponseDto getRecentReportDetail(Integer id, Integer userId) {
        RecentSafeReport report = recentSafeReportMapper.findByIdAndUserId(id, userId);
        if (report == null) {
            return null;
        }
        
        // estate_id로 estate_api_integration_tbl에서 데이터 조회
        var estate = estateMapper.getEstateById(report.getEstateId());
        if (estate == null) {
            log.warn("estate 정보를 찾을 수 없습니다: estateId={}", report.getEstateId());
            return null;
        }
        
        // Map에서 도로명주소 조회 (없으면 지번주소 사용)
        String roadAddress = estateRoadAddressMap.getOrDefault(report.getEstateId(), estate.getJibunAddr());
        
        SafeReportRequestDto requestDto = new SafeReportRequestDto();
        requestDto.setBuildingName(estate.getBuildingName());
        requestDto.setRoadAddress(roadAddress);
        requestDto.setBudget(report.getBudget());
        requestDto.setLat(estate.getLatitude().doubleValue()); // 위도 설정
        requestDto.setLng(estate.getLongitude().doubleValue()); // 경도 설정
        
        // 실시간으로 안심레포트 데이터 생성 (receiveForm과 동일한 로직)
        var rentalRatioAndBuildyear = safeReportService.generateSafeReport(requestDto);
        var buildingInfo = safeReportService.getBuildingInfo(requestDto);
        
        SafeReportResponseDto safeReportData = new SafeReportResponseDto(
            rentalRatioAndBuildyear, 
            buildingInfo != null ? buildingInfo.getViolationStatus() : null, 
            buildingInfo != null ? buildingInfo.getFloorAndPurposeList() : null
        );
        
        return safeReportData;
    }
    
    /**
     * 최근 본 안심레포트 삭제
     */
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
    
    /**
     * 위도/경도로 estate_id 조회
     */
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
        // estate_id로 estate_api_integration_tbl에서 buildingName 조회
        var estate = estateMapper.getEstateById(report.getEstateId());
        if (estate == null) {
            log.warn("estate 정보를 찾을 수 없습니다: estateId={}", report.getEstateId());
            return null;
        }
        
        String formattedDate = report.getUpdatedAt() != null ? 
            report.getUpdatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : 
            null;
            
        // Map에서 도로명주소 조회 (없으면 지번주소 사용)
        String roadAddress = estateRoadAddressMap.getOrDefault(report.getEstateId(), estate.getJibunAddr());
        
        return RecentSafeReportResponseDto.builder()
            .id(report.getId())
            .buildingName(estate.getBuildingName())
            .roadAddress(roadAddress)
            .resultGrade(report.getResultGrade())
            .updatedAt(formattedDate)
            .build();
    }
    
    /**
     * RecentSafeReport를 RecentSafeReportDetailResponseDto로 변환 (실시간 데이터 포함)
     */
    private RecentSafeReportDetailResponseDto convertToDetailResponseDto(RecentSafeReport report, SafeReportResponseDto safeReportData) {
        // estate_id로 estate_api_integration_tbl에서 buildingName 조회
        var estate = estateMapper.getEstateById(report.getEstateId());
        if (estate == null) {
            log.warn("estate 정보를 찾을 수 없습니다: estateId={}", report.getEstateId());
            return null;
        }
        
        // Map에서 도로명주소 조회 (없으면 지번주소 사용)
        String roadAddress = estateRoadAddressMap.getOrDefault(report.getEstateId(), estate.getJibunAddr());
        
        return RecentSafeReportDetailResponseDto.builder()
            .id(report.getId())
            .buildingName(estate.getBuildingName())
            .roadAddress(roadAddress)
            .budget(report.getBudget())
            .reportData(convertToJson(safeReportData)) // 실시간 조회한 데이터
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