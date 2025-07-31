package com.lighthouse.safereport.service;

import com.lighthouse.safereport.dto.SafeReportRequestDto;
import com.lighthouse.safereport.mapper.SafeReportMapper;
import com.lighthouse.estate.mapper.EstateMapper;
import com.lighthouse.estate.dto.RealEstateDTO;
import com.lighthouse.estate.dto.RealEstateSalesDTO;
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
import com.lighthouse.buildingRegister.dto.BuildingResponseDTO;

@Slf4j
@Service
@RequiredArgsConstructor
public class SafeReportService {
    private final SafeReportMapper safereportmapper;
    private final EstateMapper estateMapper;
    private final BuildingRegisterService buildingRegisterService;
    private final BuildingRegisterMapper buildingRegisterMapper;

    // 건물의 건축연도 점수, 깡통 전세 점수 계산
    public RentalRatioAndBuildyear generateSafeReport(SafeReportRequestDto dto) {
        // 1단계: 위도/경도로 건물 정보 조회
        RealEstateDTO realEstate = estateMapper.getRealEstateByLocation(dto.getLat(), dto.getLng());
        log.info("realEstate: {}", realEstate);
        
        if(realEstate == null) {
            return null;
        }
        // 건축 년도
        Integer buildYear = realEstate.getBuildYear();
        // 2단계: 건물 ID 추출
        Integer estateId = realEstate.getId();
        
        // 3단계: 해당 건물의 최근 매매 정보 1개 조회 (trade_type=1만)
        log.info("매매 정보 조회 시작 - estateId: {}", estateId);
        RealEstateSalesDTO latestSale = safereportmapper.getSalesByEstateIdWithTradeType(estateId);
        log.info("매매 정보 조회 결과 - latestSale: {}", latestSale);
        
        // 4단계: 거래 금액 추출 (latestSale이 null이면 0으로 설정)
        Integer dealAmount;
        //해당 건물의 매매 기록이 없는 경우
        if(latestSale == null) {
            dealAmount = 0;
            log.info("매매 정보 없음, dealAmount를 0으로 설정");
        } else {
            dealAmount = latestSale.getDealAmount();
            log.info("매매 정보 있음, dealAmount: {}", dealAmount);
        }
        
        RentalRatioAndBuildyear result = new RentalRatioAndBuildyear();
        result.setDealAmount(dealAmount);
        result.setBuildYear(buildYear);
        
        // 역전세율 계산
        int budget = dto.getBudget();
        double ratio;
        int ratioScore;
        
        if(dealAmount == 0) {
            ratio = 0;
            ratioScore = 0;
        } else {
            ratio = (budget / (double)dealAmount) * 100;
            ratioScore = (ratio <= 70) ? 0 : (ratio <= 80) ? 1 : (ratio <= 90) ? 2 : 3;
        }

        int currentYear = LocalDate.now().getYear();
        int age = currentYear - buildYear;
        int ageScore = (age <= 10) ? 1 : (age <= 20) ? 2 : (age <= 30) ? 3 : 4;
        result.setBuildyear_score(ageScore); //연식률 점수 저장

        //역전세율만 고려하는 것으로 수정
        result.setReverse_rental_ratio(ratio);
        result.setScore(ratioScore);
        return result;
    }

   

    // 위반 여부와 층수/용도 정보 통합 조회
    public BuildingInfoResult getBuildingInfo(SafeReportRequestDto dto) {
        String address = dto.getRoadAddress();
        String dongName = dto.getDongName();
        
        // 동 정보가 있으면 주소에 포함
        String fullAddress = address;
        if(dongName != null && !dongName.trim().isEmpty()) {
            fullAddress = address + " (" + dongName + ")";
        }
        
        // 1단계: DB에서 토지대장 데이터터 조회
        BuildingInfoResult dbResult = getBuildingInfoFromDB(dto.getLat(), dto.getLng());
        if(dbResult != null) {
            return dbResult;
        }
        
        // 2단계: DB에 데이터가 없으면 토지대장 API 호출
        if(fullAddress != null && !fullAddress.trim().isEmpty()) {
            try {
                log.info("토지대장 API 호출 시작: {}", fullAddress);
                
                // 먼저 집합건축물 대장 조회 시도
                BuildingResponseDTO result = null;
                try {
                    log.info("집합건축물 대장 조회 시도: {}", fullAddress);
                    result = buildingRegisterService.getBuildingRegisterSet(fullAddress, null);
                    if(result != null) {
                        log.info("집합건축물 대장 API 호출 성공: {}", fullAddress);
                    }
                } catch (Exception e) {
                    log.warn("집합건축물 대장 API 호출 실패: {} - {}", fullAddress, e.getMessage());
                }
                
                // 집합건축물 대장 조회 실패하면 일반 건축물 대장 조회
                if(result == null) {
                    try {
                        log.info("일반 건축물 대장 조회 시도: {}", fullAddress);
                        result = buildingRegisterService.getBuildingRegisterCommon(fullAddress, "0");
                        if(result != null) {
                            log.info("일반 건축물 대장 API 호출 성공: {}", fullAddress);
                        }
                    } catch (Exception e) {
                        log.warn("일반 건축물 대장 API 호출 실패: {} - {}", fullAddress, e.getMessage());
                    }
                }
                
                // API 호출 성공 시 DB에 저장되었을 것이므로 DB에서 다시 조회
                if(result != null) {
                    BuildingInfoResult dbResultAfterApi = getBuildingInfoFromDB(dto.getLat(), dto.getLng());
                    if(dbResultAfterApi != null) {
                        return dbResultAfterApi;
                    } else {
                        log.warn("토지대장에 건물 정보 없음: {}", fullAddress);
                    }
                } else {
                    log.warn("모든 토지대장 API 호출 실패: {}", fullAddress);
                }
            } catch (Exception e) {
                log.error("토지대장 API 호출 중 예외 발생: {} - {}", fullAddress, e.getMessage());
            }
        }
        
        log.warn("건물 정보 조회 실패: {}", fullAddress);
        return new BuildingInfoResult(null, null);
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
}
