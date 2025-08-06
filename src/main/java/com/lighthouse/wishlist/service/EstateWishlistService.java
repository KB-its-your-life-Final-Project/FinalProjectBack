package com.lighthouse.wishlist.service;

import com.lighthouse.common.external.naver.NaverMapClient;
import com.lighthouse.common.external.naver.NaverSearchClient;
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

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class EstateWishlistService {
    private final EstateWishlistMapper mapper;
    private final NaverMapClient naverMapClient;
    private final NaverSearchClient naverSearchClient;
    public void saveOrUpdateWishlist(Long memberId, EstateWishlistRequestDTO dto) {
        LikeEstate existing = mapper.findByMemberIdAndJibunAddr(memberId, dto.getJibunAddr(), false);

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
            if(dto.getEstateId() != null) {
                newItem.setEstateId(dto.getEstateId());
                //건물명 + 건물 타입 가져오기
                BuildingInfoDTO buildingInfo = mapper.findByEstateId(dto.getEstateId());
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
                    if(buildingName != null){
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
        LikeEstate existing = mapper.findByMemberIdAndJibunAddr(memberId, jibunAddr, false);
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
        return mapper.findByMemberIdAndJibunAddr(memberId, jibunAddr, true) != null;
    }

    public List<EstateWishlistResponseDTO> getAllEstateByMemberId(Long memberId) {
        return mapper.findAllEstateByMemberId(memberId);
    }
}
