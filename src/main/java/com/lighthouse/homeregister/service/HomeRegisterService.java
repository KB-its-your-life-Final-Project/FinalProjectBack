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

import com.lighthouse.estate.dto.EstateDTO;
import com.lighthouse.estate.service.EstateService;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class HomeRegisterService {
    
    private final HomeRegisterMapper homeRegisterMapper;
    private final EstateService estateService;

    // 집 정보 등록
    @Transactional
    public HomeRegisterResponseDTO registerHome(HomeRegisterRequestDTO requestDTO, Integer userId, HttpServletRequest request) {
        
        // 위도/경도로 estate_integration_tbl에서 부동산 정보 조회
        EstateDTO estateInfo = estateService.getEstateByLatLng(requestDTO.getLat(), requestDTO.getLng());
        
        if (estateInfo == null) {
            log.warn("해당 위도/경도에 대한 부동산 정보를 찾을 수 없습니다. lat: {}, lng: {}", requestDTO.getLat(), requestDTO.getLng());
            throw new RuntimeException("해당 위치의 부동산 정보를 찾을 수 없습니다.");
        }
        
        // 기존 집 정보 조회
        HomeRegister existingHome = homeRegisterMapper.selectHomeByUserId(userId);
        
        HomeRegister homeEntity = createHomeEntity(requestDTO, userId, estateInfo, request);
        
        if (existingHome != null) {
            // 기존 집 정보가 있으면 수정
            homeEntity.setId(existingHome.getId());
            homeRegisterMapper.updateHome(homeEntity);
            
            return HomeRegisterResponseDTO.builder()
                .estateId(estateInfo.getId())
                .buildingName(estateInfo.getBuildingName())
                    .buildingNumber(requestDTO.getBuildingNumber())
                .actionType("UPDATE")
                .contractStart(requestDTO.getContractStart())
                .contractEnd(requestDTO.getContractEnd())
                .rentType(requestDTO.getRentType())
                .jeonseAmount(requestDTO.getJeonseAmount())
                .monthlyDeposit(requestDTO.getMonthlyDeposit())
                .monthlyRent(requestDTO.getMonthlyRent())
                .regDate(homeEntity.getRegDate() != null ? homeEntity.getRegDate().toString() : null)
                .build();
        } else {
            // 기존 집 정보가 없으면 새로 등록
            homeRegisterMapper.insertHome(homeEntity);
            log.info("집 정보 등록 완료 - userId: {}, estateId: {}", userId, estateInfo.getId());
            
            return HomeRegisterResponseDTO.builder()
                .estateId(estateInfo.getId())
                    .buildingNumber(requestDTO.getBuildingNumber())
                .buildingName(estateInfo.getBuildingName())
                .actionType("NEW")
                .contractStart(requestDTO.getContractStart())
                .contractEnd(requestDTO.getContractEnd())
                .rentType(requestDTO.getRentType())
                .jeonseAmount(requestDTO.getJeonseAmount())
                .monthlyDeposit(requestDTO.getMonthlyDeposit())
                .monthlyRent(requestDTO.getMonthlyRent())
                .regDate(homeEntity.getRegDate() != null ? homeEntity.getRegDate().toString() : null)
                .build();
        }
    }
    
        private HomeRegister createHomeEntity(HomeRegisterRequestDTO requestDTO, Integer userId, 
                                               EstateDTO estateInfo, HttpServletRequest request) {
        try {
            HomeRegister homeEntity = new HomeRegister();
            
            // 기본 정보 설정
            homeEntity.setUserId(userId);
            homeEntity.setEstateId(estateInfo.getId());
            homeEntity.setUmdNm(estateInfo.getUmdNm());
            homeEntity.setSggCd(estateInfo.getSggCd());
            homeEntity.setBuildingName(estateInfo.getBuildingName());
            homeEntity.setBuildingNumber(requestDTO.getBuildingNumber());
            homeEntity.setJibun(estateInfo.getJibunAddr());
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
    public HomeRegister getHomeInfo(Integer userId) {
        log.info("집 정보 조회 - userId: {}", userId);
        return homeRegisterMapper.selectHomeByUserId(userId);
    }
}
