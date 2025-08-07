package com.lighthouse.search.mapper;

import com.lighthouse.config.EnvLoader;
import com.lighthouse.config.RootConfig;
import com.lighthouse.search.converter.SearchDTOConverter;
import com.lighthouse.search.dto.SearchHistoryDTO;
import com.lighthouse.security.config.SecurityConfig;
import com.lighthouse.search.entity.SearchHistory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;


import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = { RootConfig.class, SecurityConfig.class }, initializers = EnvLoader.class)
@Slf4j
@ActiveProfiles("local")
@Transactional
@Rollback
class SearchHistoryMapperTest {
    @Autowired
    private SearchHistoryMapper mapper;
    @Autowired
    private SearchDTOConverter searchDTOConverter;
    @Test
    void saveSearchHistory() {
        SearchHistory searchHistory = new SearchHistory();
        String keyword = "서울특별시 광진구 화양동 21-20";
        searchHistory.setKeyword(keyword);
        searchHistory.setMemberId(34L);
        searchHistory.setType(1);
        // when
        int result = mapper.saveSearchHistory(searchHistory);

        // then
        assertThat(result)
                .isEqualTo(1);
    }

    @Test
    void findSearchHistoryByMemberId() {
        // given
        Long memberId = 34L;
        String keyword = "화양동";

        SearchHistory searchHistory = new SearchHistory();
        searchHistory.setMemberId(memberId);
        searchHistory.setKeyword(keyword);
        searchHistory.setType(1);
        SearchHistoryDTO dto = searchDTOConverter.toDTO(searchHistory);
        mapper.saveSearchHistory(searchHistory);

        searchHistory.setKeyword("대치동");
        mapper.saveSearchHistory(searchHistory);
        // when
        var histories = mapper.findAllSearchHistoryByCondition(dto);

        // then
        assertThat(histories).isNotNull();
        assertThat(histories).isNotEmpty();
        assertThat(histories.get(0).getKeyword()).isEqualTo(keyword);

        log.info("Found search histories for member {}: {}, {}", memberId, histories.get(0).getKeyword(), histories.get(1).getKeyword());
    }
}