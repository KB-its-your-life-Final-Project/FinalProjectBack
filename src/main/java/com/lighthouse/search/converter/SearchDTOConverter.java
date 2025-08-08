package com.lighthouse.search.converter;


import com.lighthouse.search.dto.SearchHistoryDTO;
import com.lighthouse.search.entity.SearchHistory;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SearchDTOConverter {
    public SearchHistoryDTO toDTO(SearchHistory entity){
        SearchHistoryDTO dto = new SearchHistoryDTO();
        dto.setKeyword(entity.getKeyword());
        dto.setType(entity.getType());
        dto.setKeyword(entity.getKeyword());
        return dto;
    }
    public List<SearchHistoryDTO> toDTOList(Collection<SearchHistory> entities) {
        return entities.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    public SearchHistory toEntity(SearchHistoryDTO dto) {
        SearchHistory entity = new SearchHistory();
        entity.setKeyword(dto.getKeyword());
        entity.setType(dto.getType());
        entity.setMemberId(dto.getMemberId());
        return entity;
    }
}
