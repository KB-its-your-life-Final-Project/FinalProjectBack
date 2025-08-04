package com.lighthouse.wishlist.service;

import com.lighthouse.regionCode.service.RegionCodeService;
import com.lighthouse.response.CustomException;
import com.lighthouse.response.ErrorCode;
import com.lighthouse.wishlist.dto.SeparatedRegionDTO;
import com.lighthouse.wishlist.entity.LikeRegion;
import com.lighthouse.wishlist.mapper.RegionWishlistMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class RegionWishlistService {
    private final RegionWishlistMapper mapper;
    private final RegionCodeService regionCdService;

    public void saveOrUpdateWishlist(Long memberId, String regionCd) {
        String sidoCd = regionCd.substring(0, 2);
        String ssgCd = regionCd.substring(2, 5);
        String umdCd = regionCd.substring(5, 8);
        String umdRegionCd = sidoCd + ssgCd + umdCd + "00";
        String umdNm = regionCdService.findRegionByRegionCd(umdRegionCd).getLocallowNm();

        LikeRegion existing = mapper.findByMemberIdAndRegionCd(memberId, sidoCd, ssgCd, umdCd);
        if (existing != null) {
            existing.setIsLike(1);
            int updated = mapper.updateLikeRegion(existing);
            if (updated != 1) {
                throw new CustomException(ErrorCode.WISHLIST_PROCESS_FAIL);
            }
            log.info("Wishlist updated for memberId={}, regionCd={}", memberId, regionCd);

        } else {
            LikeRegion newItem = new LikeRegion();
            newItem.setMemberId(memberId);
            newItem.setSidoCd(sidoCd);
            newItem.setSsgCd(ssgCd);
            newItem.setUmdCd(umdCd);
            newItem.setIsLike(1);
            newItem.setUmdNm(umdNm);
            int inserted = mapper.saveLikeRegion(newItem);
            if (inserted != 1) {
                throw new CustomException(ErrorCode.WISHLIST_PROCESS_FAIL);
            }
            log.info("Wishlist inserted for memberId={}, regionCd={}", memberId, regionCd);
        }
    }
    public void deleteWishlist(Long memberId, String regionCd) {
        String sidoCd = regionCd.substring(0, 2);
        String ssgCd = regionCd.substring(2, 5);
        String umdCd = regionCd.substring(5, 8);

        LikeRegion existing = mapper.findByMemberIdAndRegionCd(memberId, sidoCd, ssgCd, umdCd);
        if (existing != null) {
            existing.setIsLike(2);
            int updated = mapper.updateLikeRegion(existing);
            if (updated != 1) {
                throw new CustomException(ErrorCode.WISHLIST_PROCESS_FAIL);
            }
            log.info("Wishlist soft deleted for memberId={}, regionCd={}", memberId, regionCd);
        } else {
            throw new CustomException(ErrorCode.WISHLIST_NOT_FOUND);
        }
    }

    public List<SeparatedRegionDTO> getEstateIdsByMemberId(Long memberId) {
        return mapper.findRegionsByMemberId(memberId);
    }
}
