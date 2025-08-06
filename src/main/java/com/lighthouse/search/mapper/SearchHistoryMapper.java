package com.lighthouse.search.mapper;

import com.lighthouse.search.dto.SearchHistoryResponseDTO;
import com.lighthouse.search.entity.SearchHistory;

import java.util.List;

public interface SearchHistoryMapper {
    int saveSearchHistory(SearchHistory searchHistory);
    List<SearchHistoryResponseDTO> findSearchHistoryByMemberId(Long memberId);
}
