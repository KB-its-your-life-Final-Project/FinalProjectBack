package com.lighthouse.aiRecommend.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.lighthouse.estate.dto.EstateDTO;
import com.lighthouse.estate.dto.EstateSalesDTO;
import com.lighthouse.estate.dto.EstateAndEstateSalesDTO;
import com.lighthouse.estate.entity.EstateSales;
import com.lighthouse.estate.service.EstateService;
import com.lighthouse.estate.converter.EstateDTOConverter;
import com.lighthouse.gemini.service.GeminiChatService;
import com.lighthouse.gemini.service.GeminiService;
import com.lighthouse.search.dto.SearchHistoryDTO;
import com.lighthouse.search.service.SearchHistoryService;
import com.lighthouse.wishlist.dto.EstateWishlistResponseDTO;
import com.lighthouse.wishlist.service.EstateWishlistService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiRecommendService {
    
    private final GeminiService geminiService;
    private final SearchHistoryService searchHistoryService;
    private final EstateWishlistService estateWishlistService;
    private final EstateService estateService;
    private final EstateDTOConverter estateDTOConverter;

    public String getAiRecommend(Long memberId) {

        List<EstateAndEstateSalesDTO> recentEstateList = new ArrayList<>();
        List<EstateAndEstateSalesDTO> wishEstateList = new ArrayList<>();
        List<EstateAndEstateSalesDTO> nearbyEstateList = new ArrayList<>();

        //최근 검색 목록
        SearchHistoryDTO dto = new SearchHistoryDTO();
        dto.setMemberId(memberId);
        List<SearchHistoryDTO> searchHistoryDTOs = searchHistoryService.findAllSearchHistoryByCondition(dto);

        for (SearchHistoryDTO searchHistory : searchHistoryDTOs) {
            try {
                EstateDTO estate = estateService.getEstateByAddress(searchHistory.getKeyword());
                if(estate != null) {
                    EstateSalesDTO estateSalesDTO = new EstateSalesDTO();
                    estateSalesDTO.setEstateId(estate.getId());
                    List<EstateSalesDTO> estateSalesList = estateService.getEstateSalesByElement(estateSalesDTO);
                    
                    if (!estateSalesList.isEmpty()) {
                        EstateSalesDTO estateSales = estateSalesList.get(0);
                        EstateAndEstateSalesDTO estateAndEstateSalesDTO = estateDTOConverter.toEstateAndEstateSalesDTO(estate, estateSales);
                        recentEstateList.add(estateAndEstateSalesDTO);
                        
                        // 해당 estate의 주변 건물들도 검색해서 추가
                        try {
                            List<EstateDTO> nearbyEstates = estateService.getNearyByLatLng(estate.getLatitude(), estate.getLongitude());
                            for (EstateDTO nearbyEstate : nearbyEstates) {
                                EstateSalesDTO nearbyEstateSalesDTO = new EstateSalesDTO();
                                nearbyEstateSalesDTO.setEstateId(nearbyEstate.getId());
                                List<EstateSalesDTO> nearbyEstateSalesList = estateService.getEstateSalesByElement(nearbyEstateSalesDTO);
                                
                                if (!nearbyEstateSalesList.isEmpty()) {
                                    EstateSalesDTO nearbyEstateSales = nearbyEstateSalesList.get(0);
                                    EstateAndEstateSalesDTO nearbyEstateAndEstateSalesDTO = estateDTOConverter.toEstateAndEstateSalesDTO(nearbyEstate, nearbyEstateSales);
                                    nearbyEstateList.add(nearbyEstateAndEstateSalesDTO);
                                }
                            }
                        } catch (Exception nearbyEx) {
                            log.warn("검색 히스토리 estate 주변 건물 조회 실패 - estateId: {}, error: {}", 
                                estate.getId(), nearbyEx.getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("검색 히스토리에서 부동산 정보 조회 실패 - keyword: {}, error: {}", 
                    searchHistory.getKeyword(), e.getMessage());
            }
        }


        //내 찜 건물 목록
        List<EstateWishlistResponseDTO> estateWishlistResponseDTO =  estateWishlistService.getAllEstateByMemberId(memberId);

        for(EstateWishlistResponseDTO estateWishList : estateWishlistResponseDTO) {
            try {
                EstateDTO estate = estateService.getEstateByAddress(estateWishList.getJibunAddr());
                if(estate != null) {
                    EstateSalesDTO estateSalesDTO = new EstateSalesDTO();
                    estateSalesDTO.setEstateId(estate.getId());
                    List<EstateSalesDTO> estateSalesList = estateService.getEstateSalesByElement(estateSalesDTO);
                    
                    if (!estateSalesList.isEmpty()) {
                        EstateSalesDTO estateSales = estateSalesList.get(0);
                        EstateAndEstateSalesDTO estateAndEstateSalesDTO = estateDTOConverter.toEstateAndEstateSalesDTO(estate, estateSales);
                        wishEstateList.add(estateAndEstateSalesDTO);
                        
                        // 해당 estate의 주변 건물들도 검색해서 추가
                        try {
                            List<EstateDTO> nearbyEstates = estateService.getNearyByLatLng(estate.getLatitude(), estate.getLongitude());
                            for (EstateDTO nearbyEstate : nearbyEstates) {
                                EstateSalesDTO nearbyEstateSalesDTO = new EstateSalesDTO();
                                nearbyEstateSalesDTO.setEstateId(nearbyEstate.getId());
                                List<EstateSalesDTO> nearbyEstateSalesList = estateService.getEstateSalesByElement(nearbyEstateSalesDTO);
                                
                                if (!nearbyEstateSalesList.isEmpty()) {
                                    EstateSalesDTO nearbyEstateSales = nearbyEstateSalesList.get(0);
                                    EstateAndEstateSalesDTO nearbyEstateAndEstateSalesDTO = estateDTOConverter.toEstateAndEstateSalesDTO(nearbyEstate, nearbyEstateSales);
                                    nearbyEstateList.add(nearbyEstateAndEstateSalesDTO);
                                }
                            }
                        } catch (Exception nearbyEx) {
                            log.warn("찜 목록 estate 주변 건물 조회 실패 - estateId: {}, error: {}", 
                                estate.getId(), nearbyEx.getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("찜 목록에서 부동산 정보 조회 실패 - jibunAddr: {}, error: {}", 
                    estateWishList.getJibunAddr(), e.getMessage());
            }
        }

        String prompt = "넌 한국의 안전자산 관리를 위한 부동산 서비스를 맡고 있어 너는 내가 제공하는 사용자의 정보로 집을 추천해야해 다른 집은 찾아보면 안돼. 투자보다는 실제로 거주 목적의 집들을 추천해야해 ";
        prompt += "내가 너에게 어떤 데이터를 줄껀데 그거 기반으로 json 타입으로만 리턴해줘 왜냐면 바로 사용할꺼니까. ";        
        prompt += "리턴할 json 타입은 { jibunAddress: 지번주소, positiveFactor: 장점 (한줄 - 설명하듯이), reason: 추천한 이유 (한줄 - 설명하듯이)} ";
    

        // 최근 검색 목록 데이터 추가
        prompt += "최근 검색 목록엔 현재 ";
        if (!recentEstateList.isEmpty()) {
            for (EstateAndEstateSalesDTO estate : recentEstateList) {
                String tradeType = estate.getTradeType() != null && estate.getTradeType() == 1 ? "매매" : "전월세";
                String priceInfo = "";
                
                if (estate.getTradeType() != null && estate.getTradeType() == 1) {
                    // 매매
                    priceInfo = String.format("매매가: %d만원", 
                        estate.getDealAmount() != null ? estate.getDealAmount() : 0);
                } else {
                    // 전월세
                    if (estate.getMonthlyRent() != null && estate.getMonthlyRent() == 0) {
                        // 전세
                        priceInfo = String.format("보증금: %d만원 (전세)", 
                            estate.getDeposit() != null ? estate.getDeposit() : 0);
                    } else {
                        // 월세
                        priceInfo = String.format("보증금: %d만원, 월세: %d만원", 
                            estate.getDeposit() != null ? estate.getDeposit() : 0,
                            estate.getMonthlyRent() != null ? estate.getMonthlyRent() : 0);
                    }
                }
                
                prompt += String.format("'%s (%s, %s)', ", 
                    estate.getJibunAddr(), tradeType, priceInfo);
            }
        } else {
            prompt += "검색 기록 없음, ";
        }
        
        // 찜 목록 데이터 추가
        prompt += "내 찜 목록엔 현재 ";
        if (!wishEstateList.isEmpty()) {
            for (EstateAndEstateSalesDTO estate : wishEstateList) {
                String tradeType = estate.getTradeType() != null && estate.getTradeType() == 1 ? "매매" : "전월세";
                String priceInfo = "";
                
                if (estate.getTradeType() != null && estate.getTradeType() == 1) {
                    // 매매
                    priceInfo = String.format("매매가: %d만원", 
                        estate.getDealAmount() != null ? estate.getDealAmount() : 0);
                } else {
                    // 전월세
                    if (estate.getMonthlyRent() != null && estate.getMonthlyRent() == 0) {
                        // 전세
                        priceInfo = String.format("보증금: %d만원 (전세)", 
                            estate.getDeposit() != null ? estate.getDeposit() : 0);
                    } else {
                        // 월세
                        priceInfo = String.format("보증금: %d만원, 월세: %d만원", 
                            estate.getDeposit() != null ? estate.getDeposit() : 0,
                            estate.getMonthlyRent() != null ? estate.getMonthlyRent() : 0);
                    }
                }
                
                prompt += String.format("'%s (%s, %s)', ", 
                    estate.getJibunAddr(), tradeType, priceInfo);
            }
        } else {
            prompt += "찜 목록 없음, ";
        }
        
        prompt += "주변 건물 목록엔 현재 ";
        if (!nearbyEstateList.isEmpty()) {
            for (EstateAndEstateSalesDTO estate : nearbyEstateList) {
                String tradeType = estate.getTradeType() != null && estate.getTradeType() == 1 ? "매매" : "전월세";
                String priceInfo = "";
                
                if (estate.getTradeType() != null && estate.getTradeType() == 1) {
                    // 매매
                    priceInfo = String.format("매매가: %d만원", 
                        estate.getDealAmount() != null ? estate.getDealAmount() : 0);
                } else {
                    // 전월세
                    if (estate.getMonthlyRent() != null && estate.getMonthlyRent() == 0) {
                        // 전세
                        priceInfo = String.format("보증금: %d만원 (전세)", 
                            estate.getDeposit() != null ? estate.getDeposit() : 0);
                    } else {
                        // 월세
                        priceInfo = String.format("보증금: %d만원, 월세: %d만원", 
                            estate.getDeposit() != null ? estate.getDeposit() : 0,
                            estate.getMonthlyRent() != null ? estate.getMonthlyRent() : 0);
                    }
                }
                
                prompt += String.format("'%s (%s, %s)', ", 
                    estate.getJibunAddr(), tradeType, priceInfo);
            }
        } else {
            prompt += "주변 건물 없음, ";
        }
        
        prompt += "이렇게 존재하고 꼭 위 항목들중 몇개를 골라서 추천해줘 ";
        prompt += "사용자의 최근 검색 기록이 아니더라도 다른 곳이 있으면 상관없어 ";
        prompt += "위 정보를 바탕으로 아까 말한 json 타입으로 반환해줘 ";
        
        
        // When
        try {
            String result = geminiService.sendPrompt(prompt);
            
            System.out.println("=== AI 응답 ===");
            System.out.println("프롬프트: " + prompt);
            System.out.println("AI 응답: " + result);
            System.out.println("===============");
            
            return result;
            
        } catch (Exception e) {
            System.out.println("API 호출 실패: " + e.getMessage());
            throw new RuntimeException("AI 추천 처리 중 오류 발생: " + e.getMessage());
        }

    }
}

