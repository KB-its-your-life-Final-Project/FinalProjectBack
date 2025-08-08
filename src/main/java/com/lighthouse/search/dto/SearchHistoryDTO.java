package com.lighthouse.search.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchHistoryDTO {
    private Long memberId;
    private String keyword;
    private Integer type;
    private Integer limit;
}
