package com.lighthouse.search.service;

import com.lighthouse.response.CustomException;
import com.lighthouse.response.ErrorCode;
import com.lighthouse.search.convertor.SearchDTOConverter;
import com.lighthouse.search.dto.SearchHistoryDTO;
import com.lighthouse.search.entity.SearchHistory;
import com.lighthouse.search.mapper.SearchHistoryMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class SearchHistoryServiceTest {

    private SearchHistoryService service;
    private SearchDTOConverter converter;
    private SearchHistoryMapper mapper;

    @BeforeEach
    void setUp() {
        converter = mock(SearchDTOConverter.class);
        mapper = mock(SearchHistoryMapper.class);
        service = new SearchHistoryService(mapper, converter);
    }

    @Test
    void saveSearchHistory_success() {
        // Given
        SearchHistoryDTO dto = new SearchHistoryDTO(1L, "keyword", 1, null);
        SearchHistory entity = new SearchHistory(); // ← 실제 객체를 생성해서 반환하도록 설정
        when(converter.toEntity(dto)).thenReturn(entity);
        when(mapper.saveSearchHistory(entity)).thenReturn(1);

        // When / Then
        assertDoesNotThrow(() -> service.saveSearchHistory(dto));

        // Verify mapper가 entity로 제대로 호출되었는지
        verify(mapper, times(1)).saveSearchHistory(any(SearchHistory.class));
    }

    @Test
    void saveSearchHistory_fail_throwsException() {
        // Given
        Long memberId = 1L;
        String keyword = "fail keyword";
        SearchHistoryDTO dto = new SearchHistoryDTO();
        dto.setType(1);
        dto.setKeyword(keyword);
        dto.setMemberId(memberId);

        SearchHistory entity = new SearchHistory(); // 빈 객체 생성
        when(converter.toEntity(dto)).thenReturn(entity);

        // 실패를 유도: DB insert 실패 (0 row affected)
        when(mapper.saveSearchHistory(entity)).thenReturn(0);

        // When
        CustomException ex = assertThrows(CustomException.class, () ->
                service.saveSearchHistory(dto)
        );

        // Then
        assertEquals(ErrorCode.SEARCH_HISTORY_PROCESS_FAIL, ex.getErrorCode());
        verify(mapper, times(1)).saveSearchHistory(any(SearchHistory.class));
    }

}