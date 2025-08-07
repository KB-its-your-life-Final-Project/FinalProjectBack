package com.lighthouse.wishlist.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EstateWishlistResponseDTO {
    private Long estateId;
    private Long amount;        // 매매가
    private Long deposit;       // 전세금 or 보증금
    private Long monthlyRent;   // 월세
    private String jibunAddr;   // 지번 주소
    private int buildingType;
    private String buildingName;
}
