package com.lighthouse.search.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SearchHistory {
    Long memberId;
    String keyword;
}
