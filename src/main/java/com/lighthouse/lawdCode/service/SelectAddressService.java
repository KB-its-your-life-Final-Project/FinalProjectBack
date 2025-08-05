package com.lighthouse.lawdCode.service;

import com.lighthouse.lawdCode.dto.*;
import com.lighthouse.lawdCode.mapper.AddressMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Service
@Slf4j
public class SelectAddressService {
    @Autowired
    private AddressMapper addressMapper;

    // 시/도 목록 조회
    public List<SidoDto> getSidoList() {
        try {
            log.info("시/도 목록 조회 시작");
            List<SidoDto> result = addressMapper.selectDistinctSidoWithName();
            log.info("시/도 목록 조회 완료: {}개", result != null ? result.size() : 0);
            return result;
        } catch (Exception e) {
            log.error("시/도 목록 조회 중 에러 발생", e);
            throw e;
        }
    }

    // 시/군/구 목록 조회 (시/도별)
    public List<SigugunDto> getSigugunList(String sidoCd) {
        try {
            log.info("시/군/구 목록 조회 시작 - sidoCd: {}", sidoCd);
            List<SigugunDto> result = addressMapper.selectDistinctSggWithNameBySidoCd(sidoCd);
            log.info("시/군/구 목록 조회 완료: {}개", result != null ? result.size() : 0);
            return result;
        } catch (Exception e) {
            log.error("시/군/구 목록 조회 중 에러 발생 - sidoCd: {}", sidoCd, e);
            throw e;
        }
    }

    // 읍/면/동 목록 조회 (시/군/구별)
    public List<DongDto> getDongList(String sidoCd, String sggCd) {
        try {
            log.info("읍/면/동 목록 조회 시작 - sidoCd: {}, sggCd: {}", sidoCd, sggCd);
            List<DongDto> result = addressMapper.selectDistinctDongBySidoCdAndSggCd(sidoCd, sggCd);
            log.info("읍/면/동 목록 조회 완료: {}개", result != null ? result.size() : 0);
            return result;
        } catch (Exception e) {
            log.error("읍/면/동 목록 조회 중 에러 발생 - sidoCd: {}, sggCd: {}", sidoCd, sggCd, e);
            throw e;
        }
    }
    
    // 건물명 목록 조회 (지역코드와 읍면동명으로)
    public BuildingResponseDto getBuildingList(String regionCode, String dongName) {
        try {
            log.info("건물명 목록 조회 시작 - regionCode: {}, dongName: {}", regionCode, dongName);
            List<String> buildingNames = addressMapper.selectBuildingNamesByRegionCodeAndDongName(regionCode, dongName);
            log.info("건물명 목록 조회 완료: {}개", buildingNames != null ? buildingNames.size() : 0);
            
            return BuildingResponseDto.builder()
                    .regionCode(regionCode)
                    .dongName(dongName)
                    .buildingNames(buildingNames)
                    .build();
        } catch (Exception e) {
            log.error("건물명 목록 조회 중 에러 발생 - regionCode: {}, dongName: {}", regionCode, dongName, e);
            throw e;
        }
    }
}
