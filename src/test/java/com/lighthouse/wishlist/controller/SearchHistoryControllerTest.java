package com.lighthouse.wishlist.controller;

import com.lighthouse.security.util.JwtUtil;
import com.lighthouse.wishlist.dto.SearchHistoryResponseDTO;
import com.lighthouse.wishlist.service.SearchHistoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SearchHistoryControllerTest {

    private SearchHistoryController controller;
    private JwtUtil jwtUtil;
    private SearchHistoryService service;

    @BeforeEach
    void setUp() {
        jwtUtil = mock(JwtUtil.class);
        service = mock(SearchHistoryService.class);
        controller = new SearchHistoryController(jwtUtil, service);
    }

    @Test
    void testSaveSearchHistory() throws Exception {
        // Given
        String keyword = "아파트";
        String fakeToken = "Bearer faketoken123";
        String tokenWithoutBearer = "faketoken123";
        Long memberId = 100L;

        when(jwtUtil.getSubjectFromToken(tokenWithoutBearer)).thenReturn(String.valueOf(memberId));

        // When
        var response = controller.saveSearchHistory(keyword, fakeToken);

        // Then
        verify(service).saveSearchHistory(eq(memberId), eq(keyword));
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(3004, response.getBody().getCode());
    }

    @Test
    void testGetSearchHistory() {
        // Given
        String fakeToken = "Bearer faketoken456";
        String tokenWithoutBearer = "faketoken456"; // substring(7)
        Long memberId = 200L;
        List<SearchHistoryResponseDTO> fakeResult = Arrays.asList(
                new SearchHistoryResponseDTO("아파트"),
                new SearchHistoryResponseDTO("오피스텔")
        );

        when(jwtUtil.getSubjectFromToken(tokenWithoutBearer)).thenReturn(String.valueOf(memberId));
        when(service.findSearchHistoryByMemberId(memberId)).thenReturn(fakeResult);

        // When
        var response = controller.getSearchHistory(fakeToken);

        // Then
        verify(service).findSearchHistoryByMemberId(eq(memberId));
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(3005, response.getBody().getCode());
        assertEquals(2, response.getBody().getData().size());
    }
}
