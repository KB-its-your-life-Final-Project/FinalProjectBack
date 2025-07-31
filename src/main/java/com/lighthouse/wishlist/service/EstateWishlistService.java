package com.lighthouse.wishlist.service;

import com.lighthouse.response.CustomException;
import com.lighthouse.response.ErrorCode;
import com.lighthouse.wishlist.dto.EstateWishlistResponseDTO;
import com.lighthouse.wishlist.entity.LikeEstate;
import com.lighthouse.wishlist.mapper.EstateWishlistMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class EstateWishlistService {
    private final EstateWishlistMapper mapper;
    public void saveOrUpdateWishlist(Long memberId, Long estateId) {
        LikeEstate existing = mapper.findByMemberIdAndEstateId(memberId, estateId);
        if (existing != null) {
            existing.setIsLike(1);
            int updated = mapper.updateLikeEstate(existing);
            if (updated != 1) {
                throw new CustomException(ErrorCode.WISHLIST_PROCESS_FAIL);
            }
            log.info("Wishlist updated for memberId={}, estateId={}", memberId, estateId);

        } else {
            LikeEstate newItem = new LikeEstate();
            newItem.setMemberId(memberId);
            newItem.setEstateId(estateId);
            newItem.setIsLike(1);
            int inserted = mapper.saveLikeEstate(newItem);
            if (inserted != 1) {
                throw new CustomException(ErrorCode.WISHLIST_PROCESS_FAIL);
            }
            log.info("Wishlist inserted for memberId={}, estateId={}", memberId, estateId);
        }
    }
    public void deleteWishlist(Long memberId, Long estateId) {
        LikeEstate existing = mapper.findByMemberIdAndEstateId(memberId, estateId);
        if (existing != null) {
            existing.setIsLike(2);
            int updated = mapper.updateLikeEstate(existing);
            if (updated != 1) {
                throw new CustomException(ErrorCode.WISHLIST_PROCESS_FAIL);
            }
            log.info("Wishlist soft deleted for memberId={}, estateId={}", memberId, estateId);
        } else {
            throw new CustomException(ErrorCode.WISHLIST_NOT_FOUND);
        }
    }

    public List<EstateWishlistResponseDTO> getEstateIdsByMemberId(Long memberId) {
        return mapper.findEstatesByMemberId(memberId);
    }
}
