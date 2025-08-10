package com.lighthouse.wishlist.mapper;

import com.lighthouse.wishlist.dto.BuildingInfoDTO;
import com.lighthouse.wishlist.dto.EstateWishlistResponseDTO;
import com.lighthouse.wishlist.entity.LikeEstate;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EstateWishlistMapper {
    int saveLikeEstate(LikeEstate item);
    int updateLikeEstate(LikeEstate item);
    LikeEstate findByMemberIdAndCoord(@Param("memberId") Long memberId,
                                          @Param("latitude") Double latitude,
                                          @Param("longitude") Double longitude,
                                          @Param("checkLike") Boolean checkLike);
    List<EstateWishlistResponseDTO> findAllEstateByMemberId(Long memberId);
    BuildingInfoDTO findByEstateId(@Param("estateId") Integer estateId);
}
