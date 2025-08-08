package com.lighthouse.search.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SearchHistory {
    private Integer id;
    private Long memberId;
    private String keyword;
    private Integer type;
}
