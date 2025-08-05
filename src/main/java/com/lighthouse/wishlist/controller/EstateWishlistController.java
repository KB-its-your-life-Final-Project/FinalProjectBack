package com.lighthouse.wishlist.controller;

import com.lighthouse.response.ApiResponse;
import com.lighthouse.response.SuccessCode;
import com.lighthouse.security.util.JwtUtil;
import com.lighthouse.wishlist.dto.EstateWishlistRequestDTO;
import com.lighthouse.wishlist.dto.EstateWishlistResponseDTO;
import com.lighthouse.wishlist.service.EstateWishlistService;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist/estate")
@RequiredArgsConstructor
@Slf4j
@Api(tags="건물 관심 리스트")
public class EstateWishlistController {
    private final EstateWishlistService service;
    private final JwtUtil jwtUtil;
    @PostMapping("")
    public ResponseEntity<ApiResponse<Void>> addWishlist (@RequestBody EstateWishlistRequestDTO estateId, @CookieValue("accessToken") String token) {
        Long memberId = Long.valueOf(jwtUtil.getSubjectFromToken(token));
        service.saveOrUpdateWishlist(memberId,estateId.getEstateId());
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.WISHLIST_SAVE_SUCCESS));
    }
    @DeleteMapping("/{estateId}")
    public ResponseEntity<ApiResponse<Void>> removeWishlist (@PathVariable Long estateId, @CookieValue("accessToken") String token) {
        Long memberId = Long.valueOf(jwtUtil.getSubjectFromToken(token));
        service.deleteWishlist(memberId,estateId);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.WISHLIST_DELETE_SUCCESS));
    }
    @GetMapping("")
    public ResponseEntity<ApiResponse<List<EstateWishlistResponseDTO>>> getEstateIdsByMemberId(@CookieValue("accessToken") String token) {
        Long memberId = Long.valueOf(jwtUtil.getSubjectFromToken(token));
        List<EstateWishlistResponseDTO> result = service.getEstateIdsByMemberId(memberId);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.WISHLIST_GETLIST_SUCCESS,result));
    }
}
