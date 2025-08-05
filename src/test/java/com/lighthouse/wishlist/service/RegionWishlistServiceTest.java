package com.lighthouse.wishlist.service;

import com.lighthouse.lawdCode.service.LawdCodeService;
import com.lighthouse.response.CustomException;
import com.lighthouse.response.ErrorCode;
import com.lighthouse.wishlist.dto.SeparatedRegionDTO;
import com.lighthouse.wishlist.entity.LikeRegion;
import com.lighthouse.wishlist.mapper.RegionWishlistMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RegionWishlistServiceTest {

    private RegionWishlistMapper mapper;
    private LawdCodeService lawdCodeService;
    private RegionWishlistService service;

    @BeforeEach
    void setup() {
        mapper = mock(RegionWishlistMapper.class);
        lawdCodeService = mock(LawdCodeService.class);
        service = new RegionWishlistService(mapper, lawdCodeService);
    }

    @Test
    void saveOrUpdateWishlist_updateSuccess() {
        LikeRegion existing = new LikeRegion();
        when(lawdCodeService.findRegionByRegionCd("1120010100").getLocallowNm()).thenReturn("강남동");
        when(mapper.findByMemberIdAndRegionCd(1L, "11", "200", "101")).thenReturn(existing);
        when(mapper.updateLikeRegion(existing)).thenReturn(1);

        assertDoesNotThrow(() -> service.saveOrUpdateWishlist(1L, "1120010100000"));
        verify(mapper).updateLikeRegion(existing);
    }

    @Test
    void saveOrUpdateWishlist_updateFail() {
        LikeRegion existing = new LikeRegion();
        when(lawdCodeService.findRegionByRegionCd("1120010100").getLocallowNm()).thenReturn("강남동");
        when(mapper.findByMemberIdAndRegionCd(anyLong(), anyString(), anyString(), anyString())).thenReturn(existing);
        when(mapper.updateLikeRegion(any())).thenReturn(0);

        CustomException ex = assertThrows(CustomException.class, () -> service.saveOrUpdateWishlist(1L, "1120010100000"));
        assertEquals(ErrorCode.WISHLIST_PROCESS_FAIL, ex.getErrorCode());
    }

    @Test
    void saveOrUpdateWishlist_insertSuccess() {
        when(lawdCodeService.findRegionByRegionCd("1120010100").getLocallowNm()).thenReturn("강남동");
        when(mapper.findByMemberIdAndRegionCd(anyLong(), anyString(), anyString(), anyString())).thenReturn(null);
        when(mapper.saveLikeRegion(any())).thenReturn(1);

        assertDoesNotThrow(() -> service.saveOrUpdateWishlist(1L, "1120010100000"));
        verify(mapper).saveLikeRegion(any());
    }

    @Test
    void saveOrUpdateWishlist_insertFail() {
        when(lawdCodeService.findRegionByRegionCd("1120010100").getLocallowNm()).thenReturn("강남동");
        when(mapper.findByMemberIdAndRegionCd(anyLong(), anyString(), anyString(), anyString())).thenReturn(null);
        when(mapper.saveLikeRegion(any())).thenReturn(0);

        CustomException ex = assertThrows(CustomException.class, () -> service.saveOrUpdateWishlist(1L, "1120010100000"));
        assertEquals(ErrorCode.WISHLIST_PROCESS_FAIL, ex.getErrorCode());
    }

    @Test
    void saveOrUpdateWishlist_invalidRegionCd() {
        when(lawdCodeService.findRegionByRegionCd("1120010100").getLocallowNm()).thenReturn(null);

        CustomException ex = assertThrows(CustomException.class, () -> service.saveOrUpdateWishlist(1L, "1120010100000"));
        assertEquals(ErrorCode.WISHLIST_BAD_REQUEST, ex.getErrorCode());
    }

    @Test
    void deleteWishlist_success() {
        LikeRegion existing = new LikeRegion();
        when(lawdCodeService.findRegionByRegionCd("1120010100").getLocallowNm()).thenReturn("강남동");
        when(mapper.findByMemberIdAndRegionCd(1L, "11", "200", "101")).thenReturn(existing);
        when(mapper.updateLikeRegion(existing)).thenReturn(1);

        assertDoesNotThrow(() -> service.deleteWishlist(1L, "1120010100000"));
        verify(mapper).updateLikeRegion(existing);
    }

    @Test
    void deleteWishlist_failUpdate() {
        LikeRegion existing = new LikeRegion();
        when(lawdCodeService.findRegionByRegionCd("1120010100").getLocallowNm()).thenReturn("강남동");
        when(mapper.findByMemberIdAndRegionCd(1L, "11", "200", "101")).thenReturn(existing);
        when(mapper.updateLikeRegion(existing)).thenReturn(0);

        CustomException ex = assertThrows(CustomException.class, () -> service.deleteWishlist(1L, "1120010100000"));
        assertEquals(ErrorCode.WISHLIST_PROCESS_FAIL, ex.getErrorCode());
    }

    @Test
    void deleteWishlist_notFound() {
        when(lawdCodeService.findRegionByRegionCd("1120010100").getLocallowNm()).thenReturn("강남동");
        when(mapper.findByMemberIdAndRegionCd(anyLong(), anyString(), anyString(), anyString())).thenReturn(null);

        CustomException ex = assertThrows(CustomException.class, () -> service.deleteWishlist(1L, "1120010100000"));
        assertEquals(ErrorCode.WISHLIST_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void getEstateIdsByMemberId_success() {
        SeparatedRegionDTO dto = new SeparatedRegionDTO();
        when(mapper.findRegionsByMemberId(1L)).thenReturn(Collections.singletonList(dto));

        List<SeparatedRegionDTO> result = service.getEstateIdsByMemberId(1L);
        assertEquals(1, result.size());
    }
}
