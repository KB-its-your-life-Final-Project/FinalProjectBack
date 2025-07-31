package com.lighthouse.wishlist.service;

import com.lighthouse.response.CustomException;
import com.lighthouse.response.ErrorCode;
import com.lighthouse.wishlist.dto.SearchHistoryResponseDTO;
import com.lighthouse.wishlist.entity.SearchHistory;
import com.lighthouse.wishlist.mapper.SearchHistoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@Slf4j
@RequiredArgsConstructor
public class SearchHistoryService {
    private final SearchHistoryMapper mapper;
    public void saveSearchHistory(Long memberId, String keyword){
        SearchHistory searchHistory = new SearchHistory();
        searchHistory.setMemberId(memberId);
        searchHistory.setKeyword(keyword);
        int result = mapper.saveSearchHistory(searchHistory);
        if(result != 1){
            throw new CustomException(ErrorCode.SEARCH_HISTORY_PROCESS_FAIL);
        }
        log.info("save search history. member id : {}, keyword: {}", memberId, keyword);
    }
    public List<SearchHistoryResponseDTO> findSearchHistoryByMemberId(Long memberId){
        return  mapper.findSearchHistoryByMemberId(memberId);
    }
}
