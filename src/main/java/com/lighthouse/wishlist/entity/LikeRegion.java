package com.lighthouse.wishlist.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LikeRegion {
    private Long memberId;
    private String sidoCd;
    private String ssgCd;
    private String umdCd;
    private Integer isLike;
    private String umdNm;
}