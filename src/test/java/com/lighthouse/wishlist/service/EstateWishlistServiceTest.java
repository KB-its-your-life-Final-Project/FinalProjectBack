package com.lighthouse.wishlist.service;


import com.lighthouse.common.external.naver.NaverMapClient;
import com.lighthouse.common.external.naver.NaverSearchClient;
import com.lighthouse.common.geocoding.service.GeoCodingService;
import com.lighthouse.config.EnvLoader;
import com.lighthouse.config.RootConfig;
import com.lighthouse.estate.service.EstateService;
import com.lighthouse.response.CustomException;
import com.lighthouse.response.ErrorCode;
import com.lighthouse.security.config.SecurityConfig;
import com.lighthouse.wishlist.dto.EstateWishlistRequestDTO;
import com.lighthouse.wishlist.entity.LikeEstate;
import com.lighthouse.wishlist.mapper.EstateWishlistMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;



@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = { RootConfig.class, SecurityConfig.class }, initializers = EnvLoader.class)
@Slf4j
@ActiveProfiles("local")
class EstateWishlistServiceTest {
    private EstateWishlistMapper mapper;
    private EstateWishlistService service;
    private NaverMapClient naverMapClient;
    private NaverSearchClient naverSearchClient;
    private EstateService estateService;
    private GeoCodingService geoCodingService;

    @BeforeEach
    void setup() {
        mapper = Mockito.mock(EstateWishlistMapper.class);
        naverMapClient = Mockito.mock(NaverMapClient.class);
        naverSearchClient = Mockito.mock(NaverSearchClient.class);
        estateService = Mockito.mock(EstateService.class);
        geoCodingService =  Mockito.mock(GeoCodingService.class);
        service = new EstateWishlistService(mapper, naverMapClient, naverSearchClient, estateService, geoCodingService);
    }

    @Test
    void saveOrUpdateWishlist_existingUpdateSuccess() {
        // DTO 준비
        EstateWishlistRequestDTO dto = new EstateWishlistRequestDTO();
        dto.setJibunAddr("아주동 1575");

        // geoCodingService 목킹
        Mockito.when(geoCodingService.getCoordinateFromAddress("아주동 1575"))
                .thenReturn(Map.of(
                        "lng", 127.0,  // longitude
                        "lat", 37.0    // latitude
                ));

        // 기존 찜 데이터
        LikeEstate existing = new LikeEstate();
        existing.setMemberId(1L);
        existing.setEstateId(null);
        existing.setJibunAddr("아주동 1575");
        existing.setLatitude(37.0);
        existing.setLongitude(127.0);

        // mapper 목킹
        Mockito.when(mapper.findByMemberIdAndCoord(1L, 37.0, 127.0, false)).thenReturn(existing);
        Mockito.when(mapper.updateLikeEstate(any())).thenReturn(1);

        // 테스트 실행
        assertDoesNotThrow(() -> service.saveOrUpdateWishlist(1L, dto));
        Mockito.verify(mapper).updateLikeEstate(existing);
    }
    @Test
    void saveOrUpdateWishlist_existingUpdateFail() {
        EstateWishlistRequestDTO dto = new EstateWishlistRequestDTO("아주동 1575");
        Mockito.when(geoCodingService.getCoordinateFromAddress("아주동 1575"))
                .thenReturn(Map.of("lat", 37.0, "lng", 127.0));

        LikeEstate existing = new LikeEstate();
        existing.setMemberId(1L);
        existing.setLatitude(37.0);
        existing.setLongitude(127.0);

        Mockito.when(mapper.findByMemberIdAndCoord(1L, 37.0, 127.0, false)).thenReturn(existing);
        Mockito.when(mapper.updateLikeEstate(any())).thenReturn(0);

        CustomException ex = assertThrows(CustomException.class, () -> service.saveOrUpdateWishlist(1L, dto));
        assertEquals(ErrorCode.WISHLIST_PROCESS_FAIL, ex.getErrorCode());
    }

    @Test
    void saveOrUpdateWishlist_newInsertSuccess() {
        EstateWishlistRequestDTO dto = new EstateWishlistRequestDTO("도마교동 458");
        Mockito.when(geoCodingService.getCoordinateFromAddress("도마교동 458"))
                .thenReturn(Map.of("lat", 36.5, "lng", 127.2));

        Mockito.when(mapper.findByMemberIdAndCoord(1L, 36.5, 127.2, false)).thenReturn(null);
        Mockito.when(mapper.saveLikeEstate(any())).thenReturn(1);

        assertDoesNotThrow(() -> service.saveOrUpdateWishlist(1L, dto));
        Mockito.verify(mapper).saveLikeEstate(any());
    }

    @Test
    void saveOrUpdateWishlist_newInsertFail() {
        EstateWishlistRequestDTO dto = new EstateWishlistRequestDTO("도마교동 458");
        Mockito.when(geoCodingService.getCoordinateFromAddress("도마교동 458"))
                .thenReturn(Map.of("lat", 36.5, "lng", 127.2));

        Mockito.when(mapper.findByMemberIdAndCoord(1L, 36.5, 127.2, false)).thenReturn(null);
        Mockito.when(mapper.saveLikeEstate(any())).thenReturn(0);

        CustomException ex = assertThrows(CustomException.class, () -> service.saveOrUpdateWishlist(1L, dto));
        assertEquals(ErrorCode.WISHLIST_PROCESS_FAIL, ex.getErrorCode());
    }

    @Test
    void deleteWishlist_existingDeleteSuccess() {
        String addr = "아주동 1575";
        Mockito.when(geoCodingService.getCoordinateFromAddress(addr))
                .thenReturn(Map.of("lat", 37.0, "lng", 127.0));

        LikeEstate existing = new LikeEstate();
        existing.setMemberId(1L);
        existing.setLatitude(37.0);
        existing.setLongitude(127.0);

        Mockito.when(mapper.findByMemberIdAndCoord(1L, 37.0, 127.0, false)).thenReturn(existing);
        Mockito.when(mapper.updateLikeEstate(any())).thenReturn(1);

        assertDoesNotThrow(() -> service.deleteWishlist(1L, addr));
        Mockito.verify(mapper).updateLikeEstate(existing);
    }

    @Test
    void deleteWishlist_existingDeleteFail() {
        String addr = "아주동 1575";
        Mockito.when(geoCodingService.getCoordinateFromAddress(addr))
                .thenReturn(Map.of("lat", 37.0, "lng", 127.0));

        LikeEstate existing = new LikeEstate();
        existing.setMemberId(1L);
        existing.setLatitude(37.0);
        existing.setLongitude(127.0);

        Mockito.when(mapper.findByMemberIdAndCoord(1L, 37.0, 127.0, false)).thenReturn(existing);
        Mockito.when(mapper.updateLikeEstate(any())).thenReturn(0);

        CustomException ex = assertThrows(CustomException.class, () -> service.deleteWishlist(1L, addr));
        assertEquals(ErrorCode.WISHLIST_PROCESS_FAIL, ex.getErrorCode());
    }

    @Test
    void deleteWishlist_notFound() {
        String addr = "아주동 1575";
        Mockito.when(geoCodingService.getCoordinateFromAddress(addr))
                .thenReturn(Map.of("lat", 37.0, "lng", 127.0));

        Mockito.when(mapper.findByMemberIdAndCoord(1L, 37.0, 127.0, false)).thenReturn(null);

        CustomException ex = assertThrows(CustomException.class, () -> service.deleteWishlist(1L, addr));
        assertEquals(ErrorCode.WISHLIST_NOT_FOUND, ex.getErrorCode());
    }

}
