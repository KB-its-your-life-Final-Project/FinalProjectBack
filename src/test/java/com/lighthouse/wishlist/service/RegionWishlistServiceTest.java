package com.lighthouse.wishlist.service;

import com.lighthouse.lawdCode.dto.LawdCdResponseDTO;
import com.lighthouse.lawdCode.service.LawdCodeService;
import com.lighthouse.response.CustomException;
import com.lighthouse.response.ErrorCode;
import com.lighthouse.wishlist.dto.RegionWishlistRequestDTO;
import com.lighthouse.wishlist.dto.SeparatedRegionDTO;
import com.lighthouse.wishlist.entity.LikeRegion;
import com.lighthouse.wishlist.mapper.RegionWishlistMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

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
        // DTO 준비
        RegionWishlistRequestDTO dto = new RegionWishlistRequestDTO();
        dto.setRegionCd("1112345600");

        // lawdCodeService 목킹
        LawdCdResponseDTO lawdDto = new LawdCdResponseDTO();
        lawdDto.setLocallowNm("서울특별시");  // 실제 서비스에서 사용하는 값
        Mockito.when(lawdCodeService.findRegionByRegionCd("1112345600")).thenReturn(lawdDto);

        // 기존 찜 데이터 준비
        LikeRegion existing = new LikeRegion();
        existing.setMemberId(1L);
        existing.setSidoCd("11");
        existing.setSsgCd("123");
        existing.setUmdCd("456");

        // mapper 목킹
        Mockito.when(mapper.findByMemberIdAndRegionCd(1L, "11","123","456",false)).thenReturn(existing);
        Mockito.when(mapper.updateLikeRegion(any())).thenReturn(1);

        // 테스트 실행
        assertDoesNotThrow(() -> service.saveOrUpdateWishlist(1L, dto.getRegionCd()));
        Mockito.verify(mapper).updateLikeRegion(existing);
    }

    @Test
    void saveOrUpdateWishlist_updateFail() {
        LikeRegion existing = new LikeRegion();

        // LawdCdResponseDTO 객체 반환
        LawdCdResponseDTO lawdDto = new LawdCdResponseDTO();
        lawdDto.setLocallowNm("강남동");
        when(lawdCodeService.findRegionByRegionCd("1120010100")).thenReturn(lawdDto);

        when(mapper.findByMemberIdAndRegionCd(anyLong(), anyString(), anyString(), anyString(), anyBoolean()))
                .thenReturn(existing);
        when(mapper.updateLikeRegion(any())).thenReturn(0);

        CustomException ex = assertThrows(CustomException.class, () -> service.saveOrUpdateWishlist(1L, "1120010100000"));
        assertEquals(ErrorCode.WISHLIST_PROCESS_FAIL, ex.getErrorCode());
    }
    @Test
    void saveOrUpdateWishlist_insertSuccess() {
        LawdCdResponseDTO lawdDto = new LawdCdResponseDTO();
        lawdDto.setLocallowNm("강남동");
        when(lawdCodeService.findRegionByRegionCd("1120010100")).thenReturn(lawdDto);

        when(mapper.findByMemberIdAndRegionCd(anyLong(), anyString(), anyString(), anyString(), anyBoolean()))
                .thenReturn(null);
        when(mapper.saveLikeRegion(any())).thenReturn(1);

        assertDoesNotThrow(() -> service.saveOrUpdateWishlist(1L, "1120010100000"));
        verify(mapper).saveLikeRegion(any());
    }

    @Test
    void saveOrUpdateWishlist_insertFail() {
        LawdCdResponseDTO lawdDto = new LawdCdResponseDTO();
        lawdDto.setLocallowNm("강남동");
        when(lawdCodeService.findRegionByRegionCd("1120010100")).thenReturn(lawdDto);

        when(mapper.findByMemberIdAndRegionCd(anyLong(), anyString(), anyString(), anyString(), anyBoolean()))
                .thenReturn(null);
        when(mapper.saveLikeRegion(any())).thenReturn(0);

        CustomException ex = assertThrows(CustomException.class, () -> service.saveOrUpdateWishlist(1L, "1120010100000"));
        assertEquals(ErrorCode.WISHLIST_PROCESS_FAIL, ex.getErrorCode());
    }

    @Test
    void saveOrUpdateWishlist_invalidRegionCd() {
        LawdCdResponseDTO lawdDto = new LawdCdResponseDTO();
        lawdDto.setLocallowNm(null); // 잘못된 지역코드
        when(lawdCodeService.findRegionByRegionCd("1120010100")).thenReturn(lawdDto);

        CustomException ex = assertThrows(CustomException.class, () -> service.saveOrUpdateWishlist(1L, "1120010100000"));
        assertEquals(ErrorCode.WISHLIST_PROCESS_FAIL, ex.getErrorCode());
    }

    @Test
    void deleteWishlist_success() {
        LawdCdResponseDTO lawdDto = new LawdCdResponseDTO();
        lawdDto.setLocallowNm("강남동");
        when(lawdCodeService.findRegionByRegionCd("1120010100")).thenReturn(lawdDto);

        LikeRegion existing = new LikeRegion();
        when(mapper.findByMemberIdAndRegionCd(1L, "11", "200", "101", false)).thenReturn(existing);
        when(mapper.updateLikeRegion(existing)).thenReturn(1);

        assertDoesNotThrow(() -> service.deleteWishlist(1L, "1120010100000"));
        verify(mapper).updateLikeRegion(existing);
    }

    @Test
    void deleteWishlist_failUpdate() {
        LawdCdResponseDTO lawdDto = new LawdCdResponseDTO();
        lawdDto.setLocallowNm("강남동");
        when(lawdCodeService.findRegionByRegionCd("1120010100")).thenReturn(lawdDto);

        LikeRegion existing = new LikeRegion();
        when(mapper.findByMemberIdAndRegionCd(1L, "11", "200", "101", false)).thenReturn(existing);
        when(mapper.updateLikeRegion(existing)).thenReturn(0);

        CustomException ex = assertThrows(CustomException.class, () -> service.deleteWishlist(1L, "1120010100000"));
        assertEquals(ErrorCode.WISHLIST_PROCESS_FAIL, ex.getErrorCode());
    }

    @Test
    void deleteWishlist_notFound() {
        LawdCdResponseDTO lawdDto = new LawdCdResponseDTO();
        lawdDto.setLocallowNm("강남동");
        when(lawdCodeService.findRegionByRegionCd("1120010100")).thenReturn(lawdDto);

        when(mapper.findByMemberIdAndRegionCd(anyLong(), anyString(), anyString(), anyString(), anyBoolean()))
                .thenReturn(null);

        CustomException ex = assertThrows(CustomException.class, () -> service.deleteWishlist(1L, "1120010100"));
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
