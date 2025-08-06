package com.lighthouse.wishlist.service;


import com.lighthouse.common.external.naver.NaverMapClient;
import com.lighthouse.common.external.naver.NaverSearchClient;
import com.lighthouse.config.EnvLoader;
import com.lighthouse.config.RootConfig;
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
    @Autowired
    private NaverSearchClient naverSearchClient;

    @BeforeEach
    void setup() {
        mapper = Mockito.mock(EstateWishlistMapper.class);
        naverMapClient = Mockito.mock(NaverMapClient.class);
        naverSearchClient = Mockito.mock(NaverSearchClient.class);
        service = new EstateWishlistService(mapper, naverMapClient, naverSearchClient);
    }

    @Test
    void saveOrUpdateWishlist_existingUpdateSuccess() {
        LikeEstate existing = new LikeEstate();
        existing.setMemberId(1L);
        existing.setEstateId(null);

        Mockito.when(mapper.findByMemberIdAndJibunAddr(1L, "아주동 1575")).thenReturn(existing);
        Mockito.when(mapper.updateLikeEstate(any())).thenReturn(1);

        assertDoesNotThrow(() -> service.saveOrUpdateWishlist(1L, new EstateWishlistRequestDTO()));
        Mockito.verify(mapper).updateLikeEstate(existing);
    }

    @Test
    void saveOrUpdateWishlist_existingUpdateFail() {
        LikeEstate existing = new LikeEstate();
        existing.setMemberId(1L);
        existing.setEstateId(2L);

        Mockito.when(mapper.findByMemberIdAndJibunAddr(1L, "아주동 1575")).thenReturn(existing);
        Mockito.when(mapper.updateLikeEstate(any())).thenReturn(0);

        CustomException ex = assertThrows(CustomException.class, () -> service.saveOrUpdateWishlist(1L, new EstateWishlistRequestDTO()));
        assertEquals(ErrorCode.WISHLIST_PROCESS_FAIL, ex.getErrorCode());
    }

    @Test
    void saveOrUpdateWishlist_newInsertSuccess() {
        Mockito.when(mapper.findByMemberIdAndJibunAddr(1L, "도마교동 458")).thenReturn(null);
        Mockito.when(mapper.saveLikeEstate(any())).thenReturn(1);

        assertDoesNotThrow(() -> service.saveOrUpdateWishlist(1L, new  EstateWishlistRequestDTO(null,"도마교동 458" )));
        Mockito.verify(mapper).saveLikeEstate(any());
    }

    @Test
    void saveOrUpdateWishlist_newInsertFail() {
        Mockito.when(mapper.findByMemberIdAndJibunAddr(1L, "아주동 1575")).thenReturn(null);
        Mockito.when(mapper.saveLikeEstate(any())).thenReturn(0);

        CustomException ex = assertThrows(CustomException.class, () -> service.saveOrUpdateWishlist(1L, new EstateWishlistRequestDTO()));
        assertEquals(ErrorCode.WISHLIST_PROCESS_FAIL, ex.getErrorCode());
    }

    @Test
    void deleteWishlist_existingDeleteSuccess() {
        LikeEstate existing = new LikeEstate();
        existing.setMemberId(1L);
        existing.setEstateId(2L);

        Mockito.when(mapper.findByMemberIdAndJibunAddr(1L, "아주동 1575")).thenReturn(existing);
        Mockito.when(mapper.updateLikeEstate(any())).thenReturn(1);

        assertDoesNotThrow(() -> service.deleteWishlist(1L, "아주동 1575"));
        Mockito.verify(mapper).updateLikeEstate(existing);
    }

    @Test
    void deleteWishlist_existingDeleteFail() {
        LikeEstate existing = new LikeEstate();
        existing.setMemberId(1L);
        existing.setEstateId(2L);

        Mockito.when(mapper.findByMemberIdAndJibunAddr(1L, "아주동 1575")).thenReturn(existing);
        Mockito.when(mapper.updateLikeEstate(any())).thenReturn(0);

        CustomException ex = assertThrows(CustomException.class, () -> service.deleteWishlist(1L, "아주동 1575"));
        assertEquals(ErrorCode.WISHLIST_PROCESS_FAIL, ex.getErrorCode());
    }

    @Test
    void deleteWishlist_notFound() {
        Mockito.when(mapper.findByMemberIdAndJibunAddr(1L, "아주동 1575")).thenReturn(null);

        CustomException ex = assertThrows(CustomException.class, () -> service.deleteWishlist(1L, "아주동 1575"));
        assertEquals(ErrorCode.WISHLIST_NOT_FOUND, ex.getErrorCode());
    }
}
