package com.lighthouse.wishlist.mapper;

import com.lighthouse.wishlist.dto.EstateWishlistResponseDTO;
import com.lighthouse.wishlist.entity.LikeEstate;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface EstateWishlistMapper {
    int saveLikeEstate(LikeEstate item);
    int updateLikeEstate(LikeEstate item);
    LikeEstate findByMemberIdAndEstateId(@Param("memberId") Long memberId,
                                         @Param("estateId") Long estateId);
    List<EstateWishlistResponseDTO> findEstatesByMemberId(Long memberId);
}
