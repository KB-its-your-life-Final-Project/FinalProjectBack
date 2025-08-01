package com.lighthouse.wishlist.mapper;

import com.lighthouse.wishlist.dto.SeparatedRegionDTO;
import com.lighthouse.wishlist.entity.LikeRegion;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RegionWishlistMapper {
    int saveLikeRegion(LikeRegion item);
    int updateLikeRegion(LikeRegion item);
    LikeRegion findByMemberIdAndRegionCd(@Param("memberId") Long memberId,
                                         @Param("sidoCd") String sidoCd,
                                         @Param("ssgCd") String ssgCd,
                                         @Param("umdCd") String umdCd);
    List<SeparatedRegionDTO> findRegionsByMemberId(Long memberId);
}
