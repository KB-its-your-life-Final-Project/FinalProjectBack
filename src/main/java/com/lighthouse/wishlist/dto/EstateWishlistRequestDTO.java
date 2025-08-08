package com.lighthouse.wishlist.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EstateWishlistRequestDTO {
    private Long estateId;
    private String jibunAddr;
}
