package com.lighthouse.wishlist.service;

import com.lighthouse.common.external.naver.NaverMapClient;
import com.lighthouse.common.external.naver.NaverSearchClient;
import com.lighthouse.common.geocoding.service.GeoCodingService;
import com.lighthouse.estate.dto.EstateDTO;
import com.lighthouse.estate.service.EstateService;
import com.lighthouse.response.CustomException;
import com.lighthouse.response.ErrorCode;
import com.lighthouse.wishlist.dto.BuildingInfoDTO;
import com.lighthouse.wishlist.dto.EstateWishlistRequestDTO;
import com.lighthouse.wishlist.dto.EstateWishlistResponseDTO;
import com.lighthouse.wishlist.entity.LikeEstate;
import com.lighthouse.wishlist.mapper.EstateWishlistMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class EstateWishlistService {
    private final EstateWishlistMapper mapper;
    private final NaverMapClient naverMapClient;
    private final NaverSearchClient naverSearchClient;
    private final EstateService estateService;
    private final GeoCodingService geoCodingService;
    public void saveOrUpdateWishlist(Long memberId, EstateWishlistRequestDTO dto) {
        Map<String, Double> geoCode = geoCodingService.getCoordinateFromAddress(dto.getJibunAddr());
        double latitude = geoCode.get("lat");
        double longitude = geoCode.get("lng");
        LikeEstate existing = mapper.findByMemberIdAndJibunAddr(memberId, latitude, longitude, false);

        if (existing != null) {
            existing.setIsLike(1);
            int updated = mapper.updateLikeEstate(existing);
            if (updated != 1) {
                throw new CustomException(ErrorCode.WISHLIST_PROCESS_FAIL);
            }
            log.info("Wishlist updated for memberId={}, addr={}", memberId, dto.getJibunAddr());
        } else {
            LikeEstate newItem = new LikeEstate();
            newItem.setMemberId(memberId);
            newItem.setLatitude(latitude);
            newItem.setLongitude(longitude);
            EstateDTO estateDTO = estateService.getEstateByLatLngWithNull(newItem.getLatitude(), newItem.getLongitude());
            if(estateDTO != null) {
                //건물명 + 건물 타입 가져오기
                newItem.setEstateId(estateDTO.getId());
                BuildingInfoDTO buildingInfo = mapper.findByEstateId(estateDTO.getId());
                newItem.setBuildingName(buildingInfo.getBuildingName());
                newItem.setBuildingType(buildingInfo.getBuildingType());
            }
            else{
                String buildingName = null;
                //네이버 map API로 건물명 가져오기
                Map<String,Object>geocodeResult = naverMapClient.getInfoOfAddress(dto.getJibunAddr());
                if(!geocodeResult.isEmpty()){
                    List<Map<String, Object>> addressElements =
                            (List<Map<String, Object>>) geocodeResult.get("addressElements");

                    for (Map<String, Object> element : addressElements) {
                        List<String> types = (List<String>) element.get("types");
                        if (types != null && types.contains("BUILDING_NAME")) {
                            buildingName = (String) element.get("longName");
                            break;
                        }
                    }
                    log.info("건물명 = {}", buildingName);
                    newItem.setBuildingName(buildingName);
                    if(!Objects.requireNonNull(buildingName).isEmpty()){
                        //네이버 검색 API로 건물 타입 가져오기
                        String[] tokens = dto.getJibunAddr().split(" ");
                        // 시도 + 시군구 + 빌딩명이 가장 정확한 검색결과로 나타남
                        String query = tokens[0] + " " + tokens[1] + " " + tokens[2] + buildingName;
                        int category = naverSearchClient.getCategoryCodeByAddress(query);
                        log.info("category = {}", category);
                        newItem.setBuildingType(category);
                    }
                }
            }
            newItem.setJibunAddr(dto.getJibunAddr());
            newItem.setIsLike(1);
            int inserted = mapper.saveLikeEstate(newItem);
            if (inserted != 1) {
                throw new CustomException(ErrorCode.WISHLIST_PROCESS_FAIL);
            }
            log.info("Wishlist inserted for memberId={}, jibunAddr={}", memberId, dto.getJibunAddr());
        }
    }
    public void deleteWishlist(Long memberId, String jibunAddr) {
        Map<String, Double> geoCode = geoCodingService.getCoordinateFromAddress(jibunAddr);
        double latitude = geoCode.get("lat");
        double longitude = geoCode.get("lng");
        LikeEstate existing = mapper.findByMemberIdAndJibunAddr(memberId, latitude, longitude, false);
        if (existing != null) {
            existing.setIsLike(2);
            int updated = mapper.updateLikeEstate(existing);
            if (updated != 1) {
                throw new CustomException(ErrorCode.WISHLIST_PROCESS_FAIL);
            }
            log.info("Wishlist soft deleted for memberId={}, jibunAddr={}", memberId, jibunAddr);
        } else {
            throw new CustomException(ErrorCode.WISHLIST_NOT_FOUND);
        }
    }

    public boolean existByMemberIdAndJibunAddr(Long memberId, String jibunAddr) {
        Map<String, Double> geoCode = geoCodingService.getCoordinateFromAddress(jibunAddr);
        double latitude = geoCode.get("lat");
        double longitude = geoCode.get("lng");
        return mapper.findByMemberIdAndCoord(memberId, latitude, longitude, true) != null;
    }

    public List<EstateWishlistResponseDTO> getAllEstateByMemberId(Long memberId) {
        return mapper.findAllEstateByMemberId(memberId);
    }
}
