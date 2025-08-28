package com.lighthouse.wishlist.controller;

import com.lighthouse.response.SuccessCode;
import com.lighthouse.security.util.JwtUtil;
import com.lighthouse.wishlist.dto.EstateWishlistRequestDTO;
import com.lighthouse.wishlist.dto.EstateWishlistResponseDTO;
import com.lighthouse.wishlist.service.EstateWishlistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class EstateWishlistControllerTest {

    private EstateWishlistController controller;
    private EstateWishlistService service;
    private JwtUtil jwtProcessor;

    private final String token = "mock.jwt.token";

    @BeforeEach
    void setUp() {
        service = mock(EstateWishlistService.class);
        jwtProcessor = mock(JwtUtil.class);
        controller = new EstateWishlistController(service, jwtProcessor);
    }

    @Test
    @DisplayName("찜 추가 - 성공")
    void addWishlist() {
        EstateWishlistRequestDTO dto = new EstateWishlistRequestDTO();
        Long memberId = 1L;
        String rawToken = "mock.jwt.token";

        when(jwtProcessor.getSubjectFromToken(rawToken)).thenReturn(memberId.toString());

        var response = controller.addWishlist(dto, token);

        verify(service).saveOrUpdateWishlist(memberId, dto);
        assertNotNull(response.getBody());
        assertEquals(SuccessCode.WISHLIST_SAVE_SUCCESS.getCode(), response.getBody().getCode());
    }

    @Test
    @DisplayName("찜 삭제 - 성공")
    void removeWishlist() {
        String jibunAddr = "아주동 1575";
        Long memberId = 2L;
        String rawToken = "mock.jwt.token";

        when(jwtProcessor.getSubjectFromToken(rawToken)).thenReturn(memberId.toString());

        var response = controller.removeWishlist(jibunAddr, token);

        verify(service).deleteWishlist(memberId, jibunAddr);
        assertNotNull(response.getBody());
        assertEquals(SuccessCode.WISHLIST_DELETE_SUCCESS.getCode(), response.getBody().getCode());
    }

    @Test
    @DisplayName("찜 목록 조회 - 성공")
    void getWishlist() {
        Long memberId = 3L;
        EstateWishlistResponseDTO dto1 = new EstateWishlistResponseDTO(
                1L,          // estateId
                500_000L,    // amount
                null,        // deposit
                null,        // monthlyRent
                "서울시 강남구", // jibunAddr
                1,           // buildingType
                "강남빌딩"     // buildingName
        );

        EstateWishlistResponseDTO dto2 = new EstateWishlistResponseDTO(
                2L,
                null,
                300_000L,
                50_000L,
                "서울시 서초구",
                2,
                "서초아파트"
        );
        List<EstateWishlistResponseDTO> estates = List.of(dto1, dto2);
        String rawToken = "mock.jwt.token";

        when(jwtProcessor.getSubjectFromToken(rawToken)).thenReturn(memberId.toString());
        when(service.getAllEstateByMemberId(memberId)).thenReturn(estates);

        var response = controller.getEstateIdsByMemberId(token);

        verify(service).getAllEstateByMemberId(memberId);
        assertEquals(estates, response.getBody().getData());
        assertEquals(SuccessCode.WISHLIST_GETLIST_SUCCESS.getCode(), response.getBody().getCode());
    }
}
