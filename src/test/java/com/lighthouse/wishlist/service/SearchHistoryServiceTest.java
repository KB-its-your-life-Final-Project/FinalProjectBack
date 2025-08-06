package com.lighthouse.wishlist.service;

import com.lighthouse.response.CustomException;
import com.lighthouse.response.ErrorCode;
import com.lighthouse.search.entity.SearchHistory;
import com.lighthouse.search.mapper.SearchHistoryMapper;
import com.lighthouse.search.service.SearchHistoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class SearchHistoryServiceTest {

    @Mock
    private SearchHistoryMapper mapper;

    @InjectMocks
    private SearchHistoryService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void saveSearchHistory_success() {
        Long memberId = 1L;
        String keyword = "test keyword";

        when(mapper.saveSearchHistory(any(SearchHistory.class))).thenReturn(0);

        assertDoesNotThrow(() -> service.saveSearchHistory(memberId, keyword));

        verify(mapper, times(1)).saveSearchHistory(any(SearchHistory.class));
    }

    @Test
    void saveSearchHistory_fail_throwsException() {
        Long memberId = 1L;
        String keyword = "fail keyword";

        when(mapper.saveSearchHistory(any(SearchHistory.class))).thenReturn(1);

        CustomException ex = assertThrows(CustomException.class, () ->
                service.saveSearchHistory(memberId, keyword)
        );
        assertEquals(ErrorCode.SEARCH_HISTORY_PROCESS_FAIL, ex.getErrorCode());

        verify(mapper, times(1)).saveSearchHistory(any(SearchHistory.class));
    }
}