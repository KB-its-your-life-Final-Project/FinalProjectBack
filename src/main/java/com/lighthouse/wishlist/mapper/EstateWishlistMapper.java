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
    LikeEstate findByMemberIdAndJibunAddr(@Param("memberId") Long memberId,
                                          @Param("jibunAddr") String jibunAddr,
                                          @Param("checkLike") Boolean checkLike);
    List<EstateWishlistResponseDTO> findAllEstateByMemberId(Long memberId);
    BuildingInfoDTO findByEstateId(@Param("estateId") Long estateId);
}
