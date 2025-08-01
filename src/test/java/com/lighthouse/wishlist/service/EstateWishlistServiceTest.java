package com.lighthouse.wishlist.service;


import com.lighthouse.response.CustomException;
import com.lighthouse.response.ErrorCode;
import com.lighthouse.wishlist.entity.LikeEstate;
import com.lighthouse.wishlist.mapper.EstateWishlistMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;




class EstateWishlistServiceTest {
    private EstateWishlistMapper mapper;
    private EstateWishlistService service;

    @BeforeEach
    void setup() {
        mapper = Mockito.mock(EstateWishlistMapper.class);
        service = new EstateWishlistService(mapper);
    }

    @Test
    void saveOrUpdateWishlist_existingUpdateSuccess() {
        LikeEstate existing = new LikeEstate();
        existing.setMemberId(1L);
        existing.setEstateId(2L);

        Mockito.when(mapper.findByMemberIdAndEstateId(1L, 2L)).thenReturn(existing);
        Mockito.when(mapper.updateLikeEstate(any())).thenReturn(1);

        assertDoesNotThrow(() -> service.saveOrUpdateWishlist(1L, 2L));
        Mockito.verify(mapper).updateLikeEstate(existing);
    }

    @Test
    void saveOrUpdateWishlist_existingUpdateFail() {
        LikeEstate existing = new LikeEstate();
        existing.setMemberId(1L);
        existing.setEstateId(2L);

        Mockito.when(mapper.findByMemberIdAndEstateId(1L, 2L)).thenReturn(existing);
        Mockito.when(mapper.updateLikeEstate(any())).thenReturn(0);

        CustomException ex = assertThrows(CustomException.class, () -> service.saveOrUpdateWishlist(1L, 2L));
        assertEquals(ErrorCode.WISHLIST_PROCESS_FAIL, ex.getErrorCode());
    }

    @Test
    void saveOrUpdateWishlist_newInsertSuccess() {
        Mockito.when(mapper.findByMemberIdAndEstateId(1L, 2L)).thenReturn(null);
        Mockito.when(mapper.saveLikeEstate(any())).thenReturn(1);

        assertDoesNotThrow(() -> service.saveOrUpdateWishlist(1L, 2L));
        Mockito.verify(mapper).saveLikeEstate(any());
    }

    @Test
    void saveOrUpdateWishlist_newInsertFail() {
        Mockito.when(mapper.findByMemberIdAndEstateId(1L, 2L)).thenReturn(null);
        Mockito.when(mapper.saveLikeEstate(any())).thenReturn(0);

        CustomException ex = assertThrows(CustomException.class, () -> service.saveOrUpdateWishlist(1L, 2L));
        assertEquals(ErrorCode.WISHLIST_PROCESS_FAIL, ex.getErrorCode());
    }

    @Test
    void deleteWishlist_existingDeleteSuccess() {
        LikeEstate existing = new LikeEstate();
        existing.setMemberId(1L);
        existing.setEstateId(2L);

        Mockito.when(mapper.findByMemberIdAndEstateId(1L, 2L)).thenReturn(existing);
        Mockito.when(mapper.updateLikeEstate(any())).thenReturn(1);

        assertDoesNotThrow(() -> service.deleteWishlist(1L, 2L));
        Mockito.verify(mapper).updateLikeEstate(existing);
    }

    @Test
    void deleteWishlist_existingDeleteFail() {
        LikeEstate existing = new LikeEstate();
        existing.setMemberId(1L);
        existing.setEstateId(2L);

        Mockito.when(mapper.findByMemberIdAndEstateId(1L, 2L)).thenReturn(existing);
        Mockito.when(mapper.updateLikeEstate(any())).thenReturn(0);

        CustomException ex = assertThrows(CustomException.class, () -> service.deleteWishlist(1L, 2L));
        assertEquals(ErrorCode.WISHLIST_PROCESS_FAIL, ex.getErrorCode());
    }

    @Test
    void deleteWishlist_notFound() {
        Mockito.when(mapper.findByMemberIdAndEstateId(1L, 2L)).thenReturn(null);

        CustomException ex = assertThrows(CustomException.class, () -> service.deleteWishlist(1L, 2L));
        assertEquals(ErrorCode.WISHLIST_NOT_FOUND, ex.getErrorCode());
    }
}
