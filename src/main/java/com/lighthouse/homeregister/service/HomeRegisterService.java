package com.lighthouse.homeregister.service;

import com.lighthouse.estate.dto.EstateDTO;
import com.lighthouse.homeregister.dto.HomeRegisterRequestDTO;
import com.lighthouse.homeregister.dto.HomeRegisterResponseDTO;
import com.lighthouse.homeregister.entity.HomeRegister;
import com.lighthouse.homeregister.mapper.HomeRegisterMapper;
import com.lighthouse.member.util.ClientIpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lighthouse.estate.service.EstateService;
import com.lighthouse.common.geocoding.service.GeoCodingService;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class HomeRegisterService {
    
    private final HomeRegisterMapper homeRegisterMapper;
    private final EstateService estateService;
    private final GeoCodingService geoCodingService;

    // 집 정보 등록
    @Transactional
    public HomeRegisterResponseDTO registerHome(HomeRegisterRequestDTO requestDTO, Integer userId, HttpServletRequest request) {
        
        // 위도/경도 유효성 검사
        if (requestDTO.getLat() == 0.0 || requestDTO.getLng() == 0.0) {
            log.warn("유효하지 않은 위도/경도 값: lat={}, lng={}", requestDTO.getLat(), requestDTO.getLng());
            
            // 위도/경도가 유효하지 않은 경우에도 집 정보 등록을 허용
            // jibun 주소만으로 등록하도록 처리
            log.info("위도/경도가 유효하지 않아 jibun 주소만으로 집 정보 등록을 시도합니다.");
        }
        
        // 위도/경도로 estate_integration_tbl에서 부동산 정보 조회 (선택적)
        EstateDTO estateInfo = null;
        Map<String, String> addressInfo = null;
        
        if (requestDTO.getLat() != 0.0 && requestDTO.getLng() != 0.0) {
            // 위도/경도가 유효한 경우에만 estate 테이블 조회 및 네이버 API 호출
            estateInfo = estateService.getEstateByLatLng(requestDTO.getLat(), requestDTO.getLng());
            addressInfo = geoCodingService.getDetailedAddressFromCoordinates(
                requestDTO.getLat(), requestDTO.getLng()
            );
        } else {
            // 위도/경도가 유효하지 않은 경우 기본값 설정
            estateInfo = null;
            addressInfo = new HashMap<>();
            addressInfo.put("buildingName", "");
            addressInfo.put("umdNm", "");
            addressInfo.put("jibunAddress", "");
            addressInfo.put("sggCd", "");
        }
        
        // 기존 집 정보 조회
        HomeRegister existingHome = homeRegisterMapper.selectHomeByUserId(userId);
        
        HomeRegister homeEntity = createHomeEntity(requestDTO, userId, estateInfo, addressInfo, request);
        
        if (existingHome != null) {
            // 기존 집 정보가 있으면 수정
            homeEntity.setId(existingHome.getId());
            homeRegisterMapper.updateHome(homeEntity);
            
            // buildingName 우선순위: 1) requestDTO, 2) estateInfo, 3) addressInfo
            String finalBuildingName = "";
            if (requestDTO.getBuildingName() != null && !requestDTO.getBuildingName().isEmpty()) {
                finalBuildingName = requestDTO.getBuildingName();
            } else if (estateInfo != null && estateInfo.getBuildingName() != null && !estateInfo.getBuildingName().isEmpty()) {
                finalBuildingName = estateInfo.getBuildingName();
            } else if (addressInfo != null && addressInfo.get("buildingName") != null) {
                finalBuildingName = addressInfo.get("buildingName");
            }
            
            return HomeRegisterResponseDTO.builder()
                .estateId(estateInfo != null ? estateInfo.getId() : 0)
                .buildingName(finalBuildingName)
                    .buildingNumber(requestDTO.getBuildingNumber())
                .actionType("UPDATE")
                .contractStart(requestDTO.getContractStart())
                .contractEnd(requestDTO.getContractEnd())
                .rentType(requestDTO.getRentType())
                .jeonseAmount(requestDTO.getJeonseAmount())
                .monthlyDeposit(requestDTO.getMonthlyDeposit())
                .monthlyRent(requestDTO.getMonthlyRent())
                .regDate(homeEntity.getRegDate() != null ? homeEntity.getRegDate().toString() : null)
                .umdNm(addressInfo != null ? addressInfo.get("umdNm") : "")
                .jibunAddr(addressInfo != null ? addressInfo.get("jibunAddress") : "")
                .latitude(requestDTO.getLat() != 0.0 ? requestDTO.getLat() : null)
                .longitude(requestDTO.getLng() != 0.0 ? requestDTO.getLng() : null)
                .build();
        } else {
            // 기존 집 정보가 없으면 새로 등록
            homeRegisterMapper.insertHome(homeEntity);
            log.info("집 정보 등록 완료 - userId: {}, estateId: {}", userId, estateInfo != null ? estateInfo.getId() : "null");
            
            // buildingName 우선순위: 1) requestDTO, 2) estateInfo, 3) addressInfo
            String finalBuildingName = "";
            if (requestDTO.getBuildingName() != null && !requestDTO.getBuildingName().isEmpty()) {
                finalBuildingName = requestDTO.getBuildingName();
            } else if (estateInfo != null && estateInfo.getBuildingName() != null && !estateInfo.getBuildingName().isEmpty()) {
                finalBuildingName = estateInfo.getBuildingName();
            } else if (addressInfo != null && addressInfo.get("buildingName") != null) {
                finalBuildingName = addressInfo.get("buildingName");
            }
            
            return HomeRegisterResponseDTO.builder()
                .estateId(estateInfo != null ? estateInfo.getId() : 0)
                    .buildingNumber(requestDTO.getBuildingNumber())
                .buildingName(finalBuildingName)
                .actionType("NEW")
                .contractStart(requestDTO.getContractStart())
                .contractEnd(requestDTO.getContractEnd())
                .rentType(requestDTO.getRentType())
                .jeonseAmount(requestDTO.getJeonseAmount())
                .monthlyDeposit(requestDTO.getMonthlyDeposit())
                .monthlyRent(requestDTO.getMonthlyRent())
                .regDate(homeEntity.getRegDate() != null ? homeEntity.getRegDate().toString() : null)
                .umdNm(addressInfo != null ? addressInfo.get("umdNm") : "")
                .jibunAddr(addressInfo != null ? addressInfo.get("jibunAddress") : "")
                .latitude(requestDTO.getLat() != 0.0 ? requestDTO.getLat() : null)
                .longitude(requestDTO.getLng() != 0.0 ? requestDTO.getLng() : null)
                .build();
        }
    }
    
        private HomeRegister createHomeEntity(HomeRegisterRequestDTO requestDTO, Integer userId, 
                                               EstateDTO estateInfo, Map<String, String> addressInfo, HttpServletRequest request) {
        try {
            HomeRegister homeEntity = new HomeRegister();
            
            // 기본 정보 설정
            homeEntity.setUserId(userId);
            // estate_id는 estateInfo가 있으면 설정, 없으면 0 (NOT NULL 제약조건 해결)
            homeEntity.setEstateId(estateInfo != null ? estateInfo.getId() : 0);
            // 네이버 API에서 얻은 주소 정보 사용
            homeEntity.setUmdNm(addressInfo != null ? addressInfo.get("umdNm") : "");
            homeEntity.setSggCd(addressInfo != null && addressInfo.get("sggCd") != null && !addressInfo.get("sggCd").isEmpty() 
                ? Integer.parseInt(addressInfo.get("sggCd")) : null);
            // buildingName 우선순위: 1) requestDTO, 2) estateInfo, 3) addressInfo
            String finalBuildingName = "";
            if (requestDTO.getBuildingName() != null && !requestDTO.getBuildingName().isEmpty()) {
                finalBuildingName = requestDTO.getBuildingName();
            } else if (estateInfo != null && estateInfo.getBuildingName() != null && !estateInfo.getBuildingName().isEmpty()) {
                finalBuildingName = estateInfo.getBuildingName();
            } else if (addressInfo != null && addressInfo.get("buildingName") != null) {
                finalBuildingName = addressInfo.get("buildingName");
            }
            homeEntity.setBuildingName(finalBuildingName);
            homeEntity.setBuildingNumber(requestDTO.getBuildingNumber());
            homeEntity.setJibun(addressInfo != null ? addressInfo.get("jibunAddress") : "");
            homeEntity.setRegIp(ClientIpUtil.getClientIp(request));
            homeEntity.setIsDelete(1); // 1: 정상
            
            // 계약 정보 설정
            if (requestDTO.getContractStart() != null && !requestDTO.getContractStart().isEmpty()) {
                try {
                    // 여러 날짜 형식 시도
                    LocalDate contractStart = null;
                    String startDate = requestDTO.getContractStart();
                    
                    // "yyyy.MM.dd" 형식 시도
                    try {
                        contractStart = LocalDate.parse(startDate, DateTimeFormatter.ofPattern("yyyy.MM.dd"));
                    } catch (Exception e1) {
                        // "yyyy-MM-dd" 형식 시도
                        try {
                            contractStart = LocalDate.parse(startDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                        } catch (Exception e2) {
                            // "yyyy/MM/dd" 형식 시도
                            try {
                                contractStart = LocalDate.parse(startDate, DateTimeFormatter.ofPattern("yyyy/MM/dd"));
                            } catch (Exception e3) {
                                log.warn("계약 시작일 파싱 실패 - 지원되지 않는 형식: {}", startDate);
                            }
                        }
                    }
                    
                    if (contractStart != null) {
                        homeEntity.setContractStart(contractStart);
                    }
                } catch (Exception e) {
                    log.warn("계약 시작일 파싱 실패: {}, 원본값: {}", e.getMessage(), requestDTO.getContractStart());
                }
            }
            
            if (requestDTO.getContractEnd() != null && !requestDTO.getContractEnd().isEmpty()) {
                try {
                    // 여러 날짜 형식 시도
                    LocalDate contractEnd = null;
                    String endDate = requestDTO.getContractEnd();
                    
                    // "yyyy.MM.dd" 형식 시도
                    try {
                        contractEnd = LocalDate.parse(endDate, DateTimeFormatter.ofPattern("yyyy.MM.dd"));
                    } catch (Exception e1) {
                        // "yyyy-MM-dd" 형식 시도
                        try {
                            contractEnd = LocalDate.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                        } catch (Exception e2) {
                            // "yyyy/MM/dd" 형식 시도
                            try {
                                contractEnd = LocalDate.parse(endDate, DateTimeFormatter.ofPattern("yyyy/MM/dd"));
                            } catch (Exception e3) {
                                log.warn("계약 종료일 파싱 실패 - 지원되지 않는 형식: {}", endDate);
                            }
                        }
                    }
                    
                    if (contractEnd != null) {
                        homeEntity.setContractEnd(contractEnd);
                    }
                } catch (Exception e) {
                    log.warn("계약 종료일 파싱 실패: {}, 원본값: {}", e.getMessage(), requestDTO.getContractEnd());
                }
            }
            
            homeEntity.setRentType(requestDTO.getRentType());
            homeEntity.setJeonseAmount(requestDTO.getJeonseAmount());
            homeEntity.setMonthlyDeposit(requestDTO.getMonthlyDeposit());
            homeEntity.setMonthlyRent(requestDTO.getMonthlyRent());

            return homeEntity;
            
        } catch (Exception e) {
            log.error("createHomeEntity 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    // 집 정보 조회
    public HomeRegisterResponseDTO getHomeInfo(Integer userId) {
        HomeRegister homeInfo = homeRegisterMapper.selectHomeByUserId(userId);
        
        if (homeInfo == null) {
            return null;
        }
        
        // 부동산 정보에서 위도, 경도 가져오기
        EstateDTO estateInfo = null;
        Double latitude = null;
        Double longitude = null;
        
        if (homeInfo.getEstateId() != null && homeInfo.getEstateId() != 0) {
            // estate_id가 유효한 값인 경우에만 estate 테이블에서 조회
            estateInfo = estateService.getEstateById(homeInfo.getEstateId());
            if (estateInfo != null) {
                latitude = estateInfo.getLatitude();
                longitude = estateInfo.getLongitude();
            }
        } else if (homeInfo.getEstateId() == 0) {
            // estate_id가 0인 경우 jibun 주소로 네이버 API 호출하여 위도/경도 얻기
            if (homeInfo.getJibun() != null && !homeInfo.getJibun().isEmpty()) {
                try {
                    Map<String, Double> coordinates = geoCodingService.getCoordinateFromAddress(homeInfo.getJibun());
                    if (coordinates != null) {
                        latitude = coordinates.get("lat");
                        longitude = coordinates.get("lng");
                    }
                } catch (Exception e) {
                    log.warn("jibun 주소로 위도/경도 조회 실패: {}, 오류: {}", homeInfo.getJibun(), e.getMessage());
                }
            }
        }
        
        // 위도/경도가 있으면 네이버 API로 도로명 주소 조회
        String roadAddress = null;
        if (latitude != null && longitude != null) {
            try {
                roadAddress = geoCodingService.getRoadAddressFromCoordinates(latitude, longitude);
            } catch (Exception e) {
                log.warn("도로명 주소 조회 실패: {}", e.getMessage());
                // 도로명 주소 조회 실패는 전체 응답에 영향을 주지 않음
            }
        }
        
        return HomeRegisterResponseDTO.builder()
            .estateId(homeInfo.getEstateId())
            .buildingName(homeInfo.getBuildingName())
            .buildingNumber(homeInfo.getBuildingNumber())
            .actionType("EXIST")
            .contractStart(homeInfo.getContractStart() != null ? homeInfo.getContractStart().toString() : null)
            .contractEnd(homeInfo.getContractEnd() != null ? homeInfo.getContractEnd().toString() : null)
            .rentType(homeInfo.getRentType())
            .jeonseAmount(homeInfo.getJeonseAmount())
            .monthlyDeposit(homeInfo.getMonthlyDeposit())
            .monthlyRent(homeInfo.getMonthlyRent())
            .regDate(homeInfo.getRegDate() != null ? homeInfo.getRegDate().toString() : null)
            .umdNm(homeInfo.getUmdNm())
            .jibunAddr(homeInfo.getJibun())
            .latitude(latitude)
            .longitude(longitude)
            .roadAddress(roadAddress)
            .build();
    }
}
