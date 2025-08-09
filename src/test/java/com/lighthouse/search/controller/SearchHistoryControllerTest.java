package com.lighthouse.search.controller;

import com.lighthouse.search.dto.SearchHistoryDTO;
import com.lighthouse.security.util.JwtUtil;
import com.lighthouse.search.service.SearchHistoryService;
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
        Long memberId = 100L;
        SearchHistoryDTO dto = new SearchHistoryDTO(memberId, keyword, 1, null);
        String fakeToken = "Bearer faketoken123";
        String tokenWithoutBearer = "faketoken123";


        when(jwtUtil.getSubjectFromToken(anyString())).thenReturn("100");

        // When
        var response = controller.saveSearchHistory(dto, fakeToken);

        // Then
        verify(service).saveSearchHistory(eq(dto));
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(3006, response.getBody().getCode());
    }

    @Test
    void testGetSearchHistory() {
        // Given
        String fakeToken = "Bearer faketoken456";
        String tokenWithoutBearer = "faketoken456"; // substring(7)
        Long memberId = 200L;
        SearchHistoryDTO dto = new SearchHistoryDTO();
        dto.setMemberId(memberId);
        List<SearchHistoryDTO> fakeResult = Arrays.asList(
                new SearchHistoryDTO(200L, "아파트",1, null),
                new SearchHistoryDTO(200L, "오피스텔",1, null)
        );

        when(jwtUtil.getSubjectFromToken(anyString())).thenReturn("100");
        when(service.findAllSearchHistoryByCondition(dto)).thenReturn(fakeResult);

        // When
        var response = controller.getSearchHistory(fakeToken, dto);

        // Then
        verify(service).findAllSearchHistoryByCondition(eq(dto));
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(3005, response.getBody().getCode());
        assertEquals(2, response.getBody().getData().size());
    }
}
