package com.lighthouse.wishlist.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String token = "Bearer mock.jwt.token";

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
        dto.setEstateId(100L);
        Long memberId = 1L;
        String rawToken = "mock.jwt.token";

        when(jwtProcessor.getSubjectFromToken(rawToken)).thenReturn(memberId.toString());

        var response = controller.addWishlist(dto, token);

        verify(service).saveOrUpdateWishlist(memberId, dto.getEstateId());
        assertNotNull(response.getBody());
        assertEquals(SuccessCode.WISHLIST_SAVE_SUCCESS.getCode(), response.getBody().getCode());
    }

    @Test
    @DisplayName("찜 삭제 - 성공")
    void removeWishlist() {
        Long estateId = 200L;
        Long memberId = 2L;
        String rawToken = "mock.jwt.token";

        when(jwtProcessor.getSubjectFromToken(rawToken)).thenReturn(memberId.toString());

        var response = controller.removeWishlist(estateId, token);

        verify(service).deleteWishlist(memberId, estateId);
        assertNotNull(response.getBody());
        assertEquals(SuccessCode.WISHLIST_DELETE_SUCCESS.getCode(), response.getBody().getCode());
    }

    @Test
    @DisplayName("찜 목록 조회 - 성공")
    void getWishlist() {
        Long memberId = 3L;
        List<EstateWishlistResponseDTO> estates = List.of(new EstateWishlistResponseDTO(602L,"asd"), new EstateWishlistResponseDTO(503L, "zxczx"));
        String rawToken = "mock.jwt.token";

        when(jwtProcessor.getSubjectFromToken(rawToken)).thenReturn(memberId.toString());
        when(service.getEstateIdsByMemberId(memberId)).thenReturn(estates);

        var response = controller.getEstateIdsByMemberId(token);

        verify(service).getEstateIdsByMemberId(memberId);
        assertEquals(estates, response.getBody().getData());
        assertEquals(SuccessCode.WISHLIST_GETLIST_SUCCESS.getCode(), response.getBody().getCode());
    }
}
