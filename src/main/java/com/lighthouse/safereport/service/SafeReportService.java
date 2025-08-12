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
import java.util.NoSuchElementException;

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
    //private static final int AGE_THRESHOLD_1 = 10;           // 연식 기준 1
    //private static final int AGE_THRESHOLD_2 = 20;           // 연식 기준 2
    //private static final int AGE_THRESHOLD_3 = 30;           // 연식 기준 3
    
    private final SafeReportMapper safereportmapper;
    private final EstateService estateService;
    private final BuildingRegisterService buildingRegisterService;
    private final BuildingRegisterMapper buildingRegisterMapper;
    private final GeoCodingService geoCodingService;

    // 건물의 깡통 전세 점수 계산
    public RentalRatioAndBuildyear generateSafeReport(SafeReportRequestDto dto) {
        // 1단계: 위도/경도로 건물 정보 조회
        EstateDTO realEstate = null;
        try {
            realEstate = estateService.getEstateByLatLng(dto.getLat(), dto.getLng());
        } catch (NoSuchElementException e) {
            log.info("위경도로 건물 정보를 찾을 수 없음: lat={}, lng={}", dto.getLat(), dto.getLng());
            realEstate = null;
        }
        
        if(realEstate == null) {
            // 건물 정보가 없어도 기본값으로 dealAmount=0인 객체 반환
            log.info("건물 정보 없음, dealAmount=0으로 기본값 설정");
            RentalRatioAndBuildyear defaultResult = new RentalRatioAndBuildyear();
            defaultResult.setDealAmount(0);
            defaultResult.setBuildYear(0);
            defaultResult.setReverseRentalRatio(0.0);
            defaultResult.setScore(0);
            return defaultResult;
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
        
        // 건축연도 점수 계산 (현재는 사용하지 않음)
        // int ageScore = calculateAgeScore(realEstate.getBuildYear());
        // result.setBuildYearScore(ageScore);
        
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
        // dealAmount가 0이거나 ratio가 0인 경우 0점 반환
        if(ratio == 0) {
            return 0;
        }
        
        if(ratio <= RATIO_SAFE_THRESHOLD) {
            return SCORE_SAFE;
        } else if(ratio <= RATIO_CAUTION_THRESHOLD) {
            return SCORE_CAUTION;
        } else {
            return SCORE_DANGER;
        }
    }
    
    // 건축연도 점수 계산
    // private int calculateAgeScore(Integer buildYear) {
    //     int currentYear = LocalDate.now().getYear();
    //     int age = currentYear - buildYear;
        
    //     if(age <= AGE_THRESHOLD_1) {
    //         return 1;
    //     } else if(age <= AGE_THRESHOLD_2) {
    //         return 2;
    //     } else if(age <= AGE_THRESHOLD_3) {
    //         return 3;
    //     } else {
    //         return 4;
    //     }
    // }
    
    // 위반 점수 계산
    private int calculateViolationScore(String violationStatus) {
        if (violationStatus == null) {
            return 0; // 정보가 없으면 0점
        }
        
        String trimmedStatus = violationStatus.trim();
        
        if (trimmedStatus.isEmpty()) {
            return 10; // 공백이면 정상 건축물이므로 10점
        }
        
        // "위반건축물"인 경우 위험 점수 부여
        if ("위반건축물".equals(trimmedStatus)) {
            return 3; // 위반 건축물 점수
        }
        
        return 10; // 그 외의 경우 정상 건축물로 간주하여 10점
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
            log.info("DB에 토지대장 데이터가 없어 API 호출을 시도합니다: {}", fullAddress);
            BuildingResponseDTO apiResult = callBuildingRegisterAPI(fullAddress);
            if(apiResult != null) {
                // API 호출 성공 시 DB에 저장되었을 것이므로 DB에서 다시 조회
                BuildingInfoResult dbResultAfterApi = getBuildingInfoFromDB(dto.getLat(), dto.getLng());
                if(dbResultAfterApi != null) {
                    log.info("API 호출 후 DB에서 건물 정보 조회 성공: {}", fullAddress);
                    return dbResultAfterApi;
                }
            } else {
                log.warn("토지대장 API 호출 실패 또는 타임아웃으로 인해 건물 정보 없음: {}", fullAddress);
            }
        }
        
        log.warn("건물 정보 조회 실패 - 토지대장 데이터 없음: {}", fullAddress);
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
    
    // 토지대장 API 호출 (타임아웃 포함)
    private BuildingResponseDTO callBuildingRegisterAPI(String fullAddress) {
        try {
            log.info("토지대장 API 호출 시작: {} (타임아웃: 10초)", fullAddress);
            
            // 먼저 집합건축물 대장 조회 시도
            BuildingResponseDTO result = trySetBuildingRegisterAPI(fullAddress);
            
            // 집합건축물 대장 조회 실패하면 일반 건축물 대장 조회
            if(result == null) {
                result = tryCommonBuildingRegisterAPI(fullAddress);
            }
            
            if(result != null) {
                log.info("토지대장 API 호출 성공: {}", fullAddress);
            } else {
                log.warn("모든 토지대장 API 호출 실패 (타임아웃, API 오류 또는 데이터 없음): {}", fullAddress);
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
            // 위반 점수 계산
            int violationScore = calculateViolationScore(building.getResViolationStatus());
            violationStatus.setScore(violationScore);
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
        
        // 데이터가 없어도 기본값으로 응답 반환 (프론트에서 dealAmount=0으로 처리 가능)
        log.info("안심레포트 생성 완료: lat={}, lng={}", dto.getLat(), dto.getLng());
        
        return responseDto;
    }
    
    // 안심레포트 데이터 생성
    private SafeReportResponseDto generateSafeReportData(SafeReportRequestDto dto) {
        // 건축년도, 거래 금액, 전세가율 얻기
        RentalRatioAndBuildyear rentalRatioAndBuildyear = generateSafeReport(dto);
        // 위반 여부와 층수/용도 정보 통합 조회 (건축물 대장 정보)
        BuildingInfoResult buildingInfo = getBuildingInfo(dto);

        // 최종 점수 계산
        Integer totalScore = calculateTotalScore(rentalRatioAndBuildyear, buildingInfo != null ? buildingInfo.getViolationStatus() : null);

        SafeReportResponseDto responseDto = new SafeReportResponseDto();
        responseDto.setRentalRatioAndBuildyear(rentalRatioAndBuildyear);
        responseDto.setViolationStatus(buildingInfo != null ? buildingInfo.getViolationStatus() : null);
        responseDto.setFloorAndPurposeList(buildingInfo != null ? buildingInfo.getFloorAndPurposeList() : null);
        responseDto.setTotalScore(totalScore);
        
        return responseDto;
    }
    
    // 최종 점수 계산 (역전세율 점수 70% + 위반 점수 30%)
    private Integer calculateTotalScore(RentalRatioAndBuildyear rentalRatioAndBuildyear, ViolationStatus violationStatus) {
        // 특별 케이스: 역전세율 점수가 0이고 위반 점수가 10점인 경우 최종 점수를 8점으로 설정
        if (rentalRatioAndBuildyear != null && rentalRatioAndBuildyear.getScore() == 0 && 
            violationStatus != null && violationStatus.getScore() == 10) {
            return 8;
        }
        
        double totalScore = 0.0;
        int validFactors = 0;
        
        // 역전세율 점수 (70% 비중) - 점수가 0이 아닌 경우만 포함
        if (rentalRatioAndBuildyear != null && rentalRatioAndBuildyear.getScore() > 0) {
            totalScore += rentalRatioAndBuildyear.getScore() * 0.7;
            validFactors++;
        }
        
        // 위반 점수 (30% 비중) - 점수가 0이 아닌 경우만 포함
        if (violationStatus != null && violationStatus.getScore() > 0) {
            totalScore += violationStatus.getScore() * 0.3;
            validFactors++;
        }
        
        // 둘 다 점수가 없는 경우
        if (validFactors == 0) {
            return 0;
        }
        
        // 소수점 반올림하여 정수로 반환
        return (int) Math.round(totalScore);
    }
    
    // 데이터 유효성 검증
    private boolean hasValidData(SafeReportResponseDto responseDto) {
        boolean hasRentalInfo = responseDto.getRentalRatioAndBuildyear() != null;
        boolean hasBuildingInfo = responseDto.getViolationStatus() != null || 
                                 (responseDto.getFloorAndPurposeList() != null && !responseDto.getFloorAndPurposeList().isEmpty());
        
        return hasRentalInfo || hasBuildingInfo;
    }
    
    // 최근 본 안심레포트 저장 여부 판단(전세가율이 100% 미만인 경우)
    public boolean shouldSaveToRecentReports(SafeReportResponseDto responseDto) {
        if (responseDto == null || responseDto.getRentalRatioAndBuildyear() == null) {
            return false;
        }
        return responseDto.getRentalRatioAndBuildyear().getReverseRentalRatio() < 100;
    }
}
