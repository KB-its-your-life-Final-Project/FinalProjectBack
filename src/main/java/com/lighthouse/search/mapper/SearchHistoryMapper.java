package com.lighthouse.search.mapper;

import com.lighthouse.search.dto.SearchHistoryDTO;
import com.lighthouse.search.entity.SearchHistory;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SearchHistoryMapper {
    int saveSearchHistory(SearchHistory entity);
    List<SearchHistory> findAllSearchHistoryByCondition( SearchHistoryDTO dto );
}
