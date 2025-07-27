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
    public void getBuildingRegisterCommon(String address, String type) {
        try {
            CodefUtil codef = new CodefUtil(id, password, publicKey);
            BuildingRequestDTO buildingRequestDTO;
            BuildingResponseDTO result;
            
            /** 요청 파라미터 설정 */
            buildingRequestDTO = BuildingRequestDTO.builder()
                    .address(address)
                    .userId(privateId)
                    .userPassword(EasyCodefUtil.encryptRSA(privatePassword,codef.getCodef().getPublicKey()))
                    .type(type)
                    .build();
            
            /** 코드에프 정보 조회 요청 */
            String productUrl = "/v1/kr/public/lt/eais/general-buildings";
            
            try {
                result = codef.request(productUrl, buildingRequestDTO);
            } catch (Exception e) {
                log.error("CODEF 요청 에러 (type={}): {}", type, e.getMessage());
                return; // 예외를 던지지 않고 조용히 리턴
            }
            
            // CODEF 응답 검증
            if(result == null || result.getBuildingRegisterVO() == null) {
                log.warn("CODEF API 응답이 비어있음 (type={}): {}", type, address);
                return;
            }
            
            // 필수 필드 검증
            if(result.getBuildingRegisterVO().getResDocNo() == null || 
               result.getBuildingRegisterVO().getResDocNo().trim().isEmpty()) {
                log.warn("CODEF API 응답에 필수 데이터가 없음 (type={}): {}", type, address);
                return;
            }
            
            // DB 저장 전에 위/경도 변환
            try {
                Map<String, Double> coords = addressGeocodeService.getCoordinates(address);
                result.getBuildingRegisterVO().setLatitude(coords.get("lat"));
                result.getBuildingRegisterVO().setLongitude(coords.get("lng"));
            } catch (Exception e) {
                log.warn("주소 좌표 변환 실패 (type={}): {}", type, address);
            }
            
            // DB 저장
            result.getBuildingRegisterVO().setType("일반");
            buildingRegisterPersistence.insertBuildingRegister(result);
            
        } catch (Exception e) {
            log.error("건축물 정보 처리 실패 (type={}): {}", type, e.getMessage());
            // 예외를 던지지 않고 조용히 리턴
        }
    }
    /** address = 정확한 도로명 주소, dong = (ex. 101동...) 동 이름이 존재한다면 넣고 없다면 null */
    public void getBuildingRegisterSet(String address, String dong) {
        CodefUtil codef = new CodefUtil(id, password, publicKey);
        BuildingRequestDTO buildingRequestDTO = null;
        BuildingResponseDTO result = null;
        try {
            /** 요청 파라미터 설정 - 각 상품별 파라미터를 설정(https://developer.codef.io/products) */
            buildingRequestDTO = BuildingRequestDTO.builder()
                    .address(address)
                    .userId(privateId)
                    .userPassword(EasyCodefUtil.encryptRSA(privatePassword,codef.getCodef().getPublicKey()))
                    .build();
            // 동명이 존재한다면 추가
            if(dong != null){
                buildingRequestDTO.setDong(dong);
            }
        } catch (Exception e) {
            log.error("RSA 암호화 에러",e);
            throw new RuntimeException("RSA 암호화 에러",e);
        }
        String productUrl = "/v1/kr/public/lt/eais/building-ledger-heading";
        try {
            result = codef.request(productUrl, buildingRequestDTO);
        } catch (Exception e) {
            log.error("CODEF 요청 에러",e);
            throw new RuntimeException("CODEF 요청 에러",e);
        }
        // DB 저장 전에 위/경도 변환
        if(result != null) {
            try {
                Map<String, Double> coords = addressGeocodeService.getCoordinates(address);
                result.getBuildingRegisterVO().setLatitude(coords.get("lat"));
                result.getBuildingRegisterVO().setLongitude(coords.get("lng"));
            } catch (Exception e) {
                log.warn("주소 좌표 변환 실패: {}", address);
            }
        }
        // DB 저장
        if(result == null) return;
        result.getBuildingRegisterVO().setType("집합");
        buildingRegisterPersistence.insertBuildingRegister(result);
    }
}
