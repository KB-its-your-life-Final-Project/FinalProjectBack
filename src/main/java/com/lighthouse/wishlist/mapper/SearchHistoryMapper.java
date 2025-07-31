package com.lighthouse.wishlist.mapper;

import com.lighthouse.wishlist.dto.SearchHistoryResponseDTO;
import com.lighthouse.wishlist.entity.SearchHistory;

import java.util.List;

public interface SearchHistoryMapper {
    int saveSearchHistory(SearchHistory searchHistory);
    List<SearchHistoryResponseDTO> findSearchHistoryByMemberId(Long memberId);
}
