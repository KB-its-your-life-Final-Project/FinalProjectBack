package com.lighthouse.wishlist.controller;

import com.lighthouse.response.ApiResponse;
import com.lighthouse.response.SuccessCode;
import com.lighthouse.security.util.JwtUtil;
import com.lighthouse.wishlist.dto.RegionWishlistRequestDTO;
import com.lighthouse.wishlist.dto.RegionWishlistResponseDTO;
import com.lighthouse.wishlist.dto.SeparatedRegionDTO;
import com.lighthouse.wishlist.service.RegionWishlistService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist/region")
@RequiredArgsConstructor
@Slf4j
@Api(tags="Region WishList", description= "관심 지역 추가,삭제,목록 조회 및 존재 여부 확인")
public class RegionWishlistController {
    private final RegionWishlistService service;
    private final JwtUtil jwtUtil;
    @PostMapping("")
    @ApiOperation(
            value = "관심 지역 등록 또는 갱신",
            notes = "회원이 선택한 관심 지역을 등록하거나 갱신합니다."
    )
    public ResponseEntity<ApiResponse<Void>> addWishlist (@RequestBody RegionWishlistRequestDTO estateId, @ApiIgnore @CookieValue("accessToken") String token) {
        Long memberId = Long.valueOf(jwtUtil.getSubjectFromToken(token));
        service.saveOrUpdateWishlist(memberId,estateId.getRegionCd());
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.WISHLIST_SAVE_SUCCESS));
    }
    @DeleteMapping("/{regionCd}")
    @ApiOperation(
            value = "관심 지역 삭제",
            notes = "회원이 등록한 관심 지역을 삭제합니다."
    )
    public ResponseEntity<ApiResponse<Void>> removeWishlist (@PathVariable String regionCd, @ApiIgnore @CookieValue("accessToken") String token) {
        Long memberId = Long.valueOf(jwtUtil.getSubjectFromToken(token));
        service.deleteWishlist(memberId,regionCd);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.WISHLIST_DELETE_SUCCESS));
    }
    @GetMapping("")
    @ApiOperation(
            value = "회원의 관심 지역 목록 조회",
            notes = "등록된 모든 관심 지역 목록을 조회합니다."
    )
    public ResponseEntity<ApiResponse<List<RegionWishlistResponseDTO>>> getRegionsByMemberId( @ApiIgnore @CookieValue("accessToken") String token) {
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
    @ApiOperation(
            value = "관심 지역 등록 여부 확인",
            notes = "회원이 관심 지역을 관심 리스트에 등록 여부를 확인합니다."
    )
    public ResponseEntity<ApiResponse<Boolean>> existWishlistByRegionCd (@PathVariable String regionCd, @ApiIgnore @CookieValue("accessToken") String token) {
        Long memberId = Long.valueOf(jwtUtil.getSubjectFromToken(token));
        boolean exist = service.existByMemberIdAndRegionCd(memberId, regionCd);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.WISHLIST_FIND_SUCCESS,exist));
    }
}
