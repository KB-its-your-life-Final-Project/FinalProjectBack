package com.lighthouse.search.service;

import com.lighthouse.response.CustomException;
import com.lighthouse.response.ErrorCode;
import com.lighthouse.search.convertor.SearchDTOConverter;
import com.lighthouse.search.dto.SearchHistoryDTO;
import com.lighthouse.search.entity.SearchHistory;
import com.lighthouse.search.mapper.SearchHistoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@Slf4j
@RequiredArgsConstructor
public class SearchHistoryService {
    private final SearchHistoryMapper mapper;
    private final SearchDTOConverter converter;
    public void saveSearchHistory(SearchHistoryDTO dto){
        SearchHistory entity = converter.toEntity(dto);
        int result = mapper.saveSearchHistory(entity);
        if(result != 1){
            throw new CustomException(ErrorCode.SEARCH_HISTORY_PROCESS_FAIL);
        }
        log.info("save search history. member id : {}, keyword: {}", dto.getMemberId(), entity.getKeyword());
    }
    public List<SearchHistoryDTO> findAllSearchHistoryByCondition(SearchHistoryDTO dto){
        List<SearchHistory> searchHistory = mapper.findAllSearchHistoryByCondition(dto);
        return converter.toDTOList(searchHistory);
    }
}
