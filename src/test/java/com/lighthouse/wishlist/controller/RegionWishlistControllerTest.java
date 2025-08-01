package com.lighthouse.wishlist.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lighthouse.response.SuccessCode;
import com.lighthouse.security.util.JwtUtil;
import com.lighthouse.wishlist.dto.RegionWishlistRequestDTO;
import com.lighthouse.wishlist.dto.SeparatedRegionDTO;
import com.lighthouse.wishlist.dto.RegionWishlistResponseDTO;
import com.lighthouse.wishlist.service.RegionWishlistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class RegionWishlistControllerTest {

    private RegionWishlistController controller;
    private RegionWishlistService service;
    private JwtUtil jwtUtil;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String bearerToken = "Bearer mock.jwt.token";
    private final String rawToken = "mock.jwt.token";

    @BeforeEach
    void setUp() {
        service = mock(RegionWishlistService.class);
        jwtUtil = mock(JwtUtil.class);
        controller = new RegionWishlistController(service, jwtUtil);
    }

    @Test
    @DisplayName("찜 지역 추가 - 성공")
    void addWishlist() {
        RegionWishlistRequestDTO dto = new RegionWishlistRequestDTO();
        dto.setRegionCd("1168010300");
        Long memberId = 1L;

        when(jwtUtil.getSubjectFromToken(rawToken)).thenReturn(memberId.toString());

        var response = controller.addWishlist(dto, bearerToken);

        verify(service).saveOrUpdateWishlist(memberId, dto.getRegionCd());
        assertNotNull(response.getBody());
        assertEquals(SuccessCode.WISHLIST_SAVE_SUCCESS.getCode(), response.getBody().getCode());
    }

    @Test
    @DisplayName("찜 지역 삭제 - 성공")
    void removeWishlist() {
        String regionCd = "1168010300";
        Long memberId = 2L;

        when(jwtUtil.getSubjectFromToken(rawToken)).thenReturn(memberId.toString());

        var response = controller.removeWishlist(regionCd, bearerToken);

        verify(service).deleteWishlist(memberId, regionCd);
        assertNotNull(response.getBody());
        assertEquals(SuccessCode.WISHLIST_DELETE_SUCCESS.getCode(), response.getBody().getCode());
    }

    @Test
    @DisplayName("찜 지역 목록 조회 - 성공")
    void getWishlist() {
        Long memberId = 3L;

        SeparatedRegionDTO dto = new SeparatedRegionDTO();
        dto.setSidoCd("11");
        dto.setSsgCd("680");
        dto.setUmdCd("103");
        dto.setUmdNm("개포동");

        when(jwtUtil.getSubjectFromToken(rawToken)).thenReturn(memberId.toString());
        when(service.getEstateIdsByMemberId(memberId)).thenReturn(List.of(dto));

        var response = controller.getRegionsByMemberId(bearerToken);

        verify(service).getEstateIdsByMemberId(memberId);
        assertNotNull(response.getBody());
        assertEquals(SuccessCode.WISHLIST_GETLIST_SUCCESS.getCode(), response.getBody().getCode());

        List<RegionWishlistResponseDTO> data = response.getBody().getData();
        assertEquals(1, data.size());
        assertEquals("1168010300", data.get(0).getRegionCd());
        assertEquals("개포동", data.get(0).getUmdNm());
    }
}
