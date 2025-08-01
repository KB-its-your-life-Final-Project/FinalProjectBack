package com.lighthouse.buildingRegister.service;


import com.lighthouse.buildingRegister.dto.BuildingRequestDTO;
import com.lighthouse.buildingRegister.dto.BuildingResponseDTO;
import com.lighthouse.buildingRegister.util.CodefUtil;

import com.lighthouse.toCoord.service.AddressGeocodeService;
import io.codef.api.EasyCodefUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.HashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class BuildingRegisterService {
    @Value("${CODEF_DEMO_ID}") private String id;
    @Value("${CODEF_DEMO_PW}") private String password;
    @Value("${CODEF_PUBLIC_KEY}") private String publicKey;
    @Value("${PRIVATE_ID}") private String privateId;
    @Value("${PRIVATE_PW}") private String privatePassword;

    private final BuildingRegisterPersistence buildingRegisterPersistence;
    private final AddressGeocodeService addressGeocodeService;

    /** address = 정확한 도로명 주소, type = (0=지상/1=지하/2=공중) */
    public BuildingResponseDTO getBuildingRegisterCommon(String address, String type) {
        // 주소 정규화
        String normalizedAddress = normalizeAddress(address);
        
        try {
            CodefUtil codef = new CodefUtil(id, password, publicKey);
            BuildingRequestDTO buildingRequestDTO;
            BuildingResponseDTO result;
            
            /** 요청 파라미터 설정 */
            buildingRequestDTO = BuildingRequestDTO.builder()
                    .address(normalizedAddress)
                    .userId(privateId)
                    .userPassword(EasyCodefUtil.encryptRSA(privatePassword,codef.getCodef().getPublicKey()))
                    .type(type)
                    .build();
            
            /** 코드에프 정보 조회 요청 */
            String productUrl = "/v1/kr/public/lt/eais/general-buildings";
            
            try {
                log.info("CODEF API 호출 시작: {}", productUrl);
                log.info("요청 파라미터: address={}, userId={}, type={}", buildingRequestDTO.getAddress(), buildingRequestDTO.getUserId(), type);
                
                long startTime = System.currentTimeMillis();
                result = codef.request(productUrl, buildingRequestDTO);
                long endTime = System.currentTimeMillis();
                
                log.info("CODEF API 호출 완료 (소요시간: {}ms): {}", (endTime - startTime), productUrl);
                log.info("CODEF API 호출 성공 (type={}): {}", type, normalizedAddress);
            } catch (Exception e) {
                if (e.getMessage() != null && e.getMessage().contains("타임아웃")) {
                    log.warn("CODEF API 타임아웃 (type={}): {} - {}", type, normalizedAddress, e.getMessage());
                } else {
                    log.error("CODEF 요청 에러 (type={}): {} - {}", type, normalizedAddress, e.getMessage(), e);
                }
                return null; // 예외를 던지지 않고 null 반환
            }
            
            // CODEF 응답 검증
            if(result == null || result.getBuildingRegisterVO() == null) {
                log.warn("CODEF API 응답이 비어있음 (type={}): {}", type, normalizedAddress);
                return null;
            }
            
            // 필수 필드 검증
            if(result.getBuildingRegisterVO().getCommAddrRoadName() == null || 
               result.getBuildingRegisterVO().getCommAddrRoadName().trim().isEmpty()) {
                log.warn("CODEF API 응답에 필수 데이터가 없음 (type={}): {}", type, normalizedAddress);
                return null;
            }
            
            // jibun_addr 설정 (res_user_addr + commAddrLotNumber 조합)
            String resUserAddr = result.getBuildingRegisterVO().getResUserAddr();
            String commAddrLotNumber = result.getBuildingRegisterVO().getCommAddrLotNumber();
            if(resUserAddr != null && commAddrLotNumber != null) {
                String jibunAddr = resUserAddr + " " + commAddrLotNumber;
                result.getBuildingRegisterVO().setJibunAddr(jibunAddr);
                log.info("지번 주소 설정: {}", jibunAddr);
            }
            
            // DB 저장 전에 위/경도 변환 (normalizedAddress 사용)
            try {
                Map<String, Double> coords = addressGeocodeService.getCoordinates(normalizedAddress);
                result.getBuildingRegisterVO().setLatitude(coords.get("lat"));
                result.getBuildingRegisterVO().setLongitude(coords.get("lng"));
            } catch (Exception e) {
                log.warn("주소 좌표 변환 실패 (type={}): {}", type, normalizedAddress);
            }
            
            // 건물 유형 설정 (일반 주택)
            result.getBuildingRegisterVO().setType("일반");
            log.info("건물 유형 설정: 일반 (type={})", type);
            
            // DB 저장
            buildingRegisterPersistence.insertBuildingRegister(result);
            
            return result; // 성공 시 결과 반환
            
        } catch (Exception e) {
            log.error("건축물 정보 처리 실패 (type={}): {}", type, e.getMessage());
            return null; // 예외를 던지지 않고 null 반환
        }
    }
    /** address = 정확한 도로명 주소, dong = (ex. 101동...) 동 이름이 존재한다면 넣고 없다면 null */
    public BuildingResponseDTO getBuildingRegisterSet(String address, String dong) {
        CodefUtil codef = new CodefUtil(id, password, publicKey);
        BuildingRequestDTO buildingRequestDTO = null;
        BuildingResponseDTO result = null;
        String normalizedAddress = normalizeAddress(address);
        try {
            /** 요청 파라미터 설정 - 각 상품별 파라미터를 설정(https://developer.codef.io/products) */
            buildingRequestDTO = BuildingRequestDTO.builder()
                    .address(normalizedAddress)
                    .userId(privateId)
                    .userPassword(EasyCodefUtil.encryptRSA(privatePassword,codef.getCodef().getPublicKey()))
                    .build();
            // 동명이 존재한다면 추가
            if(dong != null){
                buildingRequestDTO.setDong(dong);
            }
        } catch (Exception e) {
            log.error("RSA 암호화 에러",e);
            return null; // 예외를 던지지 않고 null 반환
        }
        String productUrl = "/v1/kr/public/lt/eais/building-ledger-heading";
        try {
            log.info("CODEF API 호출 시작: {}", productUrl);
            log.info("요청 파라미터: address={}, userId={}", buildingRequestDTO.getAddress(), buildingRequestDTO.getUserId());
            
            long startTime = System.currentTimeMillis();
            result = codef.request(productUrl, buildingRequestDTO);
            long endTime = System.currentTimeMillis();
            
            log.info("CODEF API 호출 완료 (소요시간: {}ms): {}", (endTime - startTime), productUrl);
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("타임아웃")) {
                log.warn("CODEF API 타임아웃: {} - {}", normalizedAddress, e.getMessage());
            } else {
                log.error("CODEF 요청 에러: {} - {}", normalizedAddress, e.getMessage(), e);
            }
            return null; // 예외를 던지지 않고 null 반환
        }
        // jibun_addr 설정 및 위/경도 변환
        if(result != null && result.getBuildingRegisterVO() != null) {
            // jibun_addr 설정 (res_user_addr + commAddrLotNumber 조합)
            String resUserAddr = result.getBuildingRegisterVO().getResUserAddr();
            String commAddrLotNumber = result.getBuildingRegisterVO().getCommAddrLotNumber();
            if(resUserAddr != null && commAddrLotNumber != null) {
                String jibunAddr = resUserAddr + " " + commAddrLotNumber;
                result.getBuildingRegisterVO().setJibunAddr(jibunAddr);
                log.info("지번 주소 설정: {}", jibunAddr);
            }
            
            // DB 저장 전에 위/경도 변환 (normalizedAddress 사용)
            try {
                Map<String, Double> coords = addressGeocodeService.getCoordinates(normalizedAddress);
                result.getBuildingRegisterVO().setLatitude(coords.get("lat"));
                result.getBuildingRegisterVO().setLongitude(coords.get("lng"));
            } catch (Exception e) {
                log.warn("주소 좌표 변환 실패: {}", normalizedAddress);
            }
            
            // 건물 유형 설정 (집합 주택)
            result.getBuildingRegisterVO().setType("집합");
            log.info("건물 유형 설정: 집합 (address={})", normalizedAddress);
            
            buildingRegisterPersistence.insertBuildingRegister(result);
        } else {
            log.warn("집합건축물 대장 조회 결과가 null입니다: {}", normalizedAddress);
        }
        
        return result; // 결과 반환
    }

    // 토지 대장 요청할 때 지역명 안 맞는 문제로 호출 불가 -> 지역명 매칭
    private String normalizeAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            return address;
        }
        
        // 지역명 매핑 HashMap
        Map<String, String> regionMapping = new HashMap<>();
        regionMapping.put("경기", "경기도");
        regionMapping.put("경남", "경상남도");
        regionMapping.put("경북", "경상북도");
        regionMapping.put("전남", "전라남도");
        regionMapping.put("전북", "전라북도");
        regionMapping.put("충남", "충청남도");
        regionMapping.put("충북", "충청북도");
        regionMapping.put("강원", "강원도");
        regionMapping.put("제주", "제주특별자치도");
        regionMapping.put("부산", "부산광역시");
        regionMapping.put("대구", "대구광역시");
        regionMapping.put("인천", "인천광역시");
        regionMapping.put("광주", "광주광역시");
        regionMapping.put("대전", "대전광역시");
        regionMapping.put("울산", "울산광역시");
        regionMapping.put("세종", "세종특별자치시");
        
        String normalizedAddress = address;
        
        // 정규식을 사용한 더 정확한 매칭
        for (Map.Entry<String, String> entry : regionMapping.entrySet()) {
            String shortName = entry.getKey();
            String fullName = entry.getValue();
            
            // 단어 경계를 고려한 정확한 매칭
            if (normalizedAddress.matches(".*\\b" + shortName + "\\s.*")) {
                normalizedAddress = normalizedAddress.replaceAll("\\b" + shortName + "\\s", fullName + " ");
                break;
            }
        }
        
        return normalizedAddress;
    }
}
