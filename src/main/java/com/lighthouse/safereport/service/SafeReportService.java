package com.lighthouse.safereport.service;

import com.lighthouse.safereport.dto.SafeReportRequestDto;
import com.lighthouse.safereport.mapper.SafeReportMapper;
import com.lighthouse.estate.service.EstateService;
import com.lighthouse.estate.dto.EstateDTO;
import com.lighthouse.estate.entity.EstateSales;
import com.lighthouse.safereport.entity.RentalRatioAndBuildyear;
import com.lighthouse.safereport.entity.ViolationStatus;
import com.lighthouse.safereport.entity.FloorAndPurpose;
import com.lighthouse.buildingRegister.service.BuildingRegisterService;
import com.lighthouse.buildingRegister.mapper.BuildingRegisterMapper;
import com.lighthouse.buildingRegister.vo.BuildingRegisterVO;
import com.lighthouse.buildingRegister.vo.BuildingRegisterWithStatusVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.lighthouse.buildingRegister.dto.BuildingResponseDTO;
import com.lighthouse.common.geocoding.service.GeoCodingService;
import com.lighthouse.safereport.dto.SafeReportResponseDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class SafeReportService {
    
    // 상수 정의
    private static final int RATIO_SAFE_THRESHOLD = 80;      // 안전 기준 (80% 이하)
    private static final int RATIO_CAUTION_THRESHOLD = 90;   // 주의 기준 (90% 이하)
    private static final int SCORE_SAFE = 10;                // 안전 점수
    private static final int SCORE_CAUTION = 6;              // 주의 점수
    private static final int SCORE_DANGER = 3;               // 위험 점수
    private static final int AGE_THRESHOLD_1 = 10;           // 연식 기준 1
    private static final int AGE_THRESHOLD_2 = 20;           // 연식 기준 2
    private static final int AGE_THRESHOLD_3 = 30;           // 연식 기준 3
    
    private final SafeReportMapper safereportmapper;
    private final EstateService estateService;
    private final BuildingRegisterService buildingRegisterService;
    private final BuildingRegisterMapper buildingRegisterMapper;
    private final GeoCodingService geoCodingService;

    // 건물의 건축연도 점수, 깡통 전세 점수 계산
    public RentalRatioAndBuildyear generateSafeReport(SafeReportRequestDto dto) {
        // 1단계: 위도/경도로 건물 정보 조회
        EstateDTO realEstate = estateService.getEstateByLatLng(dto.getLat(), dto.getLng());
        log.info("realEstate: {}", realEstate);
        
        if(realEstate == null) {
            return null;
        }
        
        // 2단계: 매매 정보 조회
        Integer dealAmount = getDealAmount(realEstate.getId());
        
        // 3단계: 점수 계산 및 결과 생성
        return calculateRentalRatioAndBuildyear(realEstate, dealAmount, dto.getBudget());
    }
    
    // 매매 정보 조회
    private Integer getDealAmount(Integer estateId) {
        log.info("매매 정보 조회 시작 - estateId: {}", estateId);
        EstateSales latestSale = safereportmapper.getSalesByEstateIdWithTradeType(estateId);
        log.info("매매 정보 조회 결과 - latestSale: {}", latestSale);
        
        if(latestSale == null) {
            log.info("매매 정보 없음, dealAmount를 0으로 설정");
            return 0;
        } else {
            log.info("매매 정보 있음, dealAmount: {}", latestSale.getDealAmount());
            return latestSale.getDealAmount();
        }
    }
    
    // 점수 계산 및 결과 생성
    private RentalRatioAndBuildyear calculateRentalRatioAndBuildyear(EstateDTO realEstate, Integer dealAmount, int budget) {
        RentalRatioAndBuildyear result = new RentalRatioAndBuildyear();
        result.setDealAmount(dealAmount);
        result.setBuildYear(realEstate.getBuildYear());
        
        // 역전세율 계산
        double ratio = calculateRentalRatio(budget, dealAmount);
        int ratioScore = calculateRatioScore(ratio);
        
        // 건축연도 점수 계산
        int ageScore = calculateAgeScore(realEstate.getBuildYear());
        result.setBuildYearScore(ageScore);
        
        // 결과 설정
        result.setReverseRentalRatio(ratio);
        result.setScore(ratioScore);
        
        return result;
    }
    
    // 역전세율 계산
    private double calculateRentalRatio(int budget, Integer dealAmount) {
        if(dealAmount == 0) {
            return 0;
        }
        return ((double) budget / dealAmount) * 100;
    }
    
    // 역전세율 점수 계산
    private int calculateRatioScore(double ratio) {
        if(ratio <= RATIO_SAFE_THRESHOLD) {
            return SCORE_SAFE;
        } else if(ratio <= RATIO_CAUTION_THRESHOLD) {
            return SCORE_CAUTION;
        } else {
            return SCORE_DANGER;
        }
    }
    
    // 건축연도 점수 계산
    private int calculateAgeScore(Integer buildYear) {
        int currentYear = LocalDate.now().getYear();
        int age = currentYear - buildYear;
        
        if(age <= AGE_THRESHOLD_1) {
            return 1;
        } else if(age <= AGE_THRESHOLD_2) {
            return 2;
        } else if(age <= AGE_THRESHOLD_3) {
            return 3;
        } else {
            return 4;
        }
    }

    // 위반 여부와 층수/용도 정보 통합 조회
    public BuildingInfoResult getBuildingInfo(SafeReportRequestDto dto) {
        String fullAddress = buildFullAddress(dto.getRoadAddress(), dto.getDongName());
        double[] coordinates = getCoordinatesFromAddress(fullAddress);
        
        // 1단계: DB에서 토지대장 데이터 조회
        BuildingInfoResult dbResult = getBuildingInfoFromDB(coordinates[0], coordinates[1]);
        if(dbResult != null) {
            return dbResult;
        }
        
        // 2단계: DB에 데이터가 없으면 토지대장 API 호출
        if(fullAddress != null && !fullAddress.trim().isEmpty()) {
            BuildingResponseDTO apiResult = callBuildingRegisterAPI(fullAddress);
            if(apiResult != null) {
                // API 호출 성공 시 DB에 저장되었을 것이므로 DB에서 다시 조회
                BuildingInfoResult dbResultAfterApi = getBuildingInfoFromDB(dto.getLat(), dto.getLng());
                if(dbResultAfterApi != null) {
                    return dbResultAfterApi;
                }
            }
        }
        
        log.warn("건물 정보 조회 실패: {}", fullAddress);
        return new BuildingInfoResult(null, null);
    }
    
    // 전체 주소 구성
    private String buildFullAddress(String address, String dongName) {
        if(dongName != null && !dongName.trim().isEmpty()) {
            return address + " (" + dongName + ")";
        }
        return address;
    }
    
    // 주소를 좌표로 변환
    private double[] getCoordinatesFromAddress(String fullAddress) {
        try {
            Map<String, Double> coords = geoCodingService.getCoordinateFromAddress(fullAddress);
            return new double[]{coords.get("lat"), coords.get("lng")};
        } catch (Exception e) {
            log.warn("주소 좌표 변환 실패: {}", fullAddress);
            return new double[]{0.0, 0.0};
        }
    }
    
    // 토지대장 API 호출
    private BuildingResponseDTO callBuildingRegisterAPI(String fullAddress) {
        try {
            log.info("토지대장 API 호출 시작: {}", fullAddress);
            
            // 먼저 집합건축물 대장 조회 시도
            BuildingResponseDTO result = trySetBuildingRegisterAPI(fullAddress);
            
            // 집합건축물 대장 조회 실패하면 일반 건축물 대장 조회
            if(result == null) {
                result = tryCommonBuildingRegisterAPI(fullAddress);
            }
            
            if(result != null) {
                log.info("토지대장 API 호출 성공: {}", fullAddress);
            } else {
                log.warn("모든 토지대장 API 호출 실패: {}", fullAddress);
            }
            
            return result;
        } catch (Exception e) {
            log.error("토지대장 API 호출 중 예외 발생: {} - {}", fullAddress, e.getMessage());
            return null;
        }
    }
    
    // 집합건축물 대장 API 호출
    private BuildingResponseDTO trySetBuildingRegisterAPI(String fullAddress) {
        try {
            log.info("집합건축물 대장 조회 시도: {}", fullAddress);
            return buildingRegisterService.getBuildingRegisterSet(fullAddress, null);
        } catch (Exception e) {
            log.warn("집합건축물 대장 API 호출 실패: {} - {}", fullAddress, e.getMessage());
            return null;
        }
    }
    
    // 일반 건축물 대장 API 호출
    private BuildingResponseDTO tryCommonBuildingRegisterAPI(String fullAddress) {
        try {
            log.info("일반 건축물 대장 조회 시도: {}", fullAddress);
            return buildingRegisterService.getBuildingRegisterCommon(fullAddress, "0");
        } catch (Exception e) {
            log.warn("일반 건축물 대장 API 호출 실패: {} - {}", fullAddress, e.getMessage());
            return null;
        }
    }
    
    // DB에서 토지대장 정보 조회
    private BuildingInfoResult getBuildingInfoFromDB(double lat, double lng) {
        BuildingRegisterVO building = buildingRegisterMapper.getBuildingRegisterByLocation(lat, lng);
        List<BuildingRegisterWithStatusVO> buildingsWithStatus = buildingRegisterMapper.getBuildingRegisterWithStatusByLocation(lat, lng);
        
        ViolationStatus violationStatus = null;
        List<FloorAndPurpose> floorAndPurposeList = null;
        
        if(building != null) {
            violationStatus = new ViolationStatus();
            violationStatus.setViolationStatus(building.getResViolationStatus());
            log.info("BuildingRegisterMapper에서 위반여부 조회 성공: {}", violationStatus);
        }
        
        if(buildingsWithStatus != null && !buildingsWithStatus.isEmpty()) {
            floorAndPurposeList = buildingsWithStatus.stream()
                .map(buildingStatus -> {
                    FloorAndPurpose floorAndPurpose = new FloorAndPurpose();
                    floorAndPurpose.setResFloor(buildingStatus.getResFloor());
                    floorAndPurpose.setResUseType(buildingStatus.getResUseType());
                    floorAndPurpose.setResStructure(buildingStatus.getResStructure());
                    floorAndPurpose.setResArea(buildingStatus.getResArea());
                    return floorAndPurpose;
                })
                .toList();
            log.info("BuildingRegisterMapper에서 층수/용도 조회 성공: {}", floorAndPurposeList);
        }
        
        if(violationStatus != null || (floorAndPurposeList != null && !floorAndPurposeList.isEmpty())) {
            log.info("BuildingRegisterMapper에서 건물 정보 조회 성공 - 위반여부: {}, 층수/용도: {}", violationStatus, floorAndPurposeList);
            return new BuildingInfoResult(violationStatus, floorAndPurposeList);
        }
        
        return null;
    }
    
    // 건물 정보 결과를 담는 내부 클래스
    public static class BuildingInfoResult {
        private final ViolationStatus violationStatus;
        private final List<FloorAndPurpose> floorAndPurposeList;
        
        public BuildingInfoResult(ViolationStatus violationStatus, List<FloorAndPurpose> floorAndPurposeList) {
            this.violationStatus = violationStatus;
            this.floorAndPurposeList = floorAndPurposeList;
        }
        
        public ViolationStatus getViolationStatus() { return violationStatus; }
        public List<FloorAndPurpose> getFloorAndPurposeList() { return floorAndPurposeList; }
    }

    // 안심레포트 전체 생성
    public SafeReportResponseDto generateCompleteSafeReport(SafeReportRequestDto dto) {
        // 안심레포트 데이터 생성
        SafeReportResponseDto responseDto = generateSafeReportData(dto);
        
        // 데이터 유효성 검증
        if (!hasValidData(responseDto)) {
            log.warn("모든 정보 없음: lat={}, lng={}", dto.getLat(), dto.getLng());
            return null;
        }
        
        return responseDto;
    }
    
    // 안심레포트 데이터 생성
    private SafeReportResponseDto generateSafeReportData(SafeReportRequestDto dto) {
        // 건축년도, 거래 금액, 전세가율 얻기
        RentalRatioAndBuildyear rentalRatioAndBuildyear = generateSafeReport(dto);
        // 위반 여부와 층수/용도 정보 통합 조회 (건축물 대장 정보)
        BuildingInfoResult buildingInfo = getBuildingInfo(dto);

        return new SafeReportResponseDto(
            rentalRatioAndBuildyear, 
            buildingInfo != null ? buildingInfo.getViolationStatus() : null, 
            buildingInfo != null ? buildingInfo.getFloorAndPurposeList() : null
        );
    }
    
    // 데이터 유효성 검증
    private boolean hasValidData(SafeReportResponseDto responseDto) {
        boolean hasRentalInfo = responseDto.getRentalRatioAndBuildyear() != null;
        boolean hasBuildingInfo = responseDto.getViolationStatus() != null || 
                                 (responseDto.getFloorAndPurposeList() != null && !responseDto.getFloorAndPurposeList().isEmpty());
        
        return hasRentalInfo || hasBuildingInfo;
    }
    
    // 최근 본 안심레포트 저장 여부 판단(매매 목록이 있고 전세가율이 100% 미만인 경우)
    public boolean shouldSaveToRecentReports(SafeReportResponseDto responseDto) {
        return responseDto.getRentalRatioAndBuildyear() != null &&
               responseDto.getRentalRatioAndBuildyear().getDealAmount() != 0 &&
               responseDto.getRentalRatioAndBuildyear().getReverseRentalRatio() < 100;
    }
}
