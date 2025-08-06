package com.lighthouse.wishlist.controller;

import com.lighthouse.response.ApiResponse;
import com.lighthouse.response.SuccessCode;
import com.lighthouse.security.util.JwtUtil;
import com.lighthouse.wishlist.dto.RegionWishlistRequestDTO;
import com.lighthouse.wishlist.dto.RegionWishlistResponseDTO;
import com.lighthouse.wishlist.dto.SeparatedRegionDTO;
import com.lighthouse.wishlist.service.RegionWishlistService;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist/region")
@RequiredArgsConstructor
@Slf4j
@Api(tags="지역 관심 리스트")
public class RegionWishlistController {
    private final RegionWishlistService service;
    private final JwtUtil jwtUtil;
    @PostMapping("")
    public ResponseEntity<ApiResponse<Void>> addWishlist (@RequestBody RegionWishlistRequestDTO estateId, @CookieValue("accessToken") String token) {
        Long memberId = Long.valueOf(jwtUtil.getSubjectFromToken(token));
        service.saveOrUpdateWishlist(memberId,estateId.getRegionCd());
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.WISHLIST_SAVE_SUCCESS));
    }
    @DeleteMapping("/{regionCd}")
    public ResponseEntity<ApiResponse<Void>> removeWishlist (@PathVariable String regionCd, @CookieValue("accessToken") String token) {
        Long memberId = Long.valueOf(jwtUtil.getSubjectFromToken(token));
        service.deleteWishlist(memberId,regionCd);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.WISHLIST_DELETE_SUCCESS));
    }
    @GetMapping("")
    public ResponseEntity<ApiResponse<List<RegionWishlistResponseDTO>>> getRegionsByMemberId(@CookieValue("accessToken") String token) {
        Long memberId = Long.valueOf(jwtUtil.getSubjectFromToken(token));
        List<SeparatedRegionDTO> result = service.getEstateIdsByMemberId(memberId);
        List<RegionWishlistResponseDTO> response = result.stream()
                .map(dto -> {
                    String fullRegionCd = dto.getSidoCd() + dto.getSsgCd() + dto.getUmdCd() + "00";
                    return new RegionWishlistResponseDTO(fullRegionCd, dto.getUmdNm());
                })
                .toList();
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.WISHLIST_GETLIST_SUCCESS,response));
    }
    @GetMapping("/check/{regionCd}")
    public ResponseEntity<ApiResponse<Boolean>> existWishlistByRegionCd (@PathVariable String regionCd, @CookieValue("accessToken") String token) {
        Long memberId = Long.valueOf(jwtUtil.getSubjectFromToken(token));
        boolean exist = service.existByMemberIdAndRegionCd(memberId, regionCd);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.WISHLIST_FIND_SUCCESS,exist));
    }
}
