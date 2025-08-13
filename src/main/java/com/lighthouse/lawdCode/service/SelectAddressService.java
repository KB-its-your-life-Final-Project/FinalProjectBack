package com.lighthouse.lawdCode.service;

import com.lighthouse.estate.dto.BuildingInfoDto;
import com.lighthouse.estate.service.EstateService;
import com.lighthouse.lawdCode.converter.AddressNameConverter;
import com.lighthouse.lawdCode.dto.*;
import com.lighthouse.lawdCode.mapper.AddressMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SelectAddressService {
    @Autowired
    private AddressMapper addressMapper;
    
    @Autowired
    private EstateService estateService;
    
    @Autowired
    private AddressNameConverter addressNameConverter;

    // 시/도 목록 조회
    public List<SidoDto> getSidoList() {
        try {
            log.info("시/도 목록 조회 시작");
            List<SidoDto> result = addressMapper.selectDistinctSidoWithName();
            
            // 시도명 변환
            result = result.stream()
                    .map(sido -> {
                        String convertedName = addressNameConverter.convertSidoName(sido.getSidoCd());
                        sido.setSidoNm(convertedName);
                        return sido;
                    })
                    .collect(Collectors.toList());
                    
            log.info("시/도 목록 조회 완료: {}개", result != null ? result.size() : 0);
            return result;
        } catch (Exception e) {
            log.error("시/도 목록 조회 중 에러 발생", e);
            return null;
        }
    }

    // 시/군/구 목록 조회 (시/도별)
    public List<SigugunDto> getSigugunList(String sidoCd) {
        try {
            log.info("시/군/구 목록 조회 시작 - sidoCd: {}", sidoCd);
            List<SigugunDto> result = addressMapper.selectDistinctSggWithNameBySidoCd(sidoCd);
            
            // 시군구명 변환 및 중복 제거
            result = result.stream()
                    .map(sigugun -> {
                        String convertedName = addressNameConverter.convertSggName(sidoCd, sigugun.getSggNm());
                        sigugun.setSggNm(convertedName);
                        return sigugun;
                    })
                    .collect(Collectors.groupingBy(SigugunDto::getSggNm))
                    .values()
                    .stream()
                    .map(list -> list.get(0)) // 각 그룹에서 첫 번째 항목만 선택
                    .collect(Collectors.toList());
                    
            log.info("시/군/구 목록 조회 완료: {}개", result != null ? result.size() : 0);
            return result;
        } catch (Exception e) {
            log.error("시/군/구 목록 조회 중 에러 발생 - sidoCd: {}", sidoCd, e);
            return null;
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
            return null;
        }
    }
    
    // 건물 정보 목록 조회 (지역코드와 읍면동명으로)
    public BuildingResponseDto getBuildingList(String regionCode, String dongName) {
        try {
            log.info("건물 정보 목록 조회 시작 - regionCode: {}, dongName: {}", regionCode, dongName);
            List<BuildingInfoDto> buildingInfos = estateService.getBuildingInfosByRegionCodeAndDongName(regionCode, dongName);
            log.info("건물 정보 목록 조회 완료: {}개", buildingInfos != null ? buildingInfos.size() : 0);
            
            return BuildingResponseDto.builder()
                    .regionCode(regionCode)
                    .dongName(dongName)
                    .buildingInfos(buildingInfos)
                    .build();
        } catch (Exception e) {
            log.error("건물 정보 목록 조회 중 에러 발생 - regionCode: {}, dongName: {}", regionCode, dongName, e);
            return null;
        }
    }
}
