package com.lighthouse.wishlist.entity;

import io.swagger.models.auth.In;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LikeEstate {
    private Long memberId;
    private Integer estateId;
    private Integer isLike;
    private String jibunAddr;
    private String buildingName;
    private Integer buildingType;
    private Double latitude;
    private Double longitude;
}
