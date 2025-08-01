package com.lighthouse.wishlist.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LikeEstate {
    private Long memberId;
    private Long estateId;
    private Integer isLike;
}
