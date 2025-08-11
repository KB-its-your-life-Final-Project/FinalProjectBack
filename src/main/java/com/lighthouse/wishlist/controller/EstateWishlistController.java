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
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist/estate")
@RequiredArgsConstructor
@Slf4j
@Api(tags="건물 관심 리스트", description = "로그인 회원의 관심 매물을 등록·삭제하고, 목록 조회 및 존재 여부 확인")
public class EstateWishlistController {
    private final EstateWishlistService service;
    private final JwtUtil jwtUtil;
    @PostMapping("")
    public ResponseEntity<ApiResponse<Void>> addWishlist (@RequestBody EstateWishlistRequestDTO dto, @ApiIgnore @CookieValue("accessToken") String token) {
        Long memberId = Long.valueOf(jwtUtil.getSubjectFromToken(token));
        service.saveOrUpdateWishlist(memberId,dto);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.WISHLIST_SAVE_SUCCESS));
    }
    @DeleteMapping("")
    public ResponseEntity<ApiResponse<Void>> removeWishlist (@RequestParam String jibunAddr, @ApiIgnore @CookieValue("accessToken") String token) {
        Long memberId = Long.valueOf(jwtUtil.getSubjectFromToken(token));
        service.deleteWishlist(memberId,jibunAddr);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.WISHLIST_DELETE_SUCCESS));
    }
    @GetMapping("")
    public ResponseEntity<ApiResponse<List<EstateWishlistResponseDTO>>> getEstateIdsByMemberId(@ApiIgnore @CookieValue("accessToken") String token) {
        Long memberId = Long.valueOf(jwtUtil.getSubjectFromToken(token));
        List<EstateWishlistResponseDTO> result = service.getAllEstateByMemberId(memberId);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.WISHLIST_GETLIST_SUCCESS,result));
    }
    @GetMapping("/check")
    public ResponseEntity<ApiResponse<Boolean>> existWishlistByJibunAddr (@RequestParam String jibunAddr, @ApiIgnore @CookieValue("accessToken") String token) {
        Long memberId = Long.valueOf(jwtUtil.getSubjectFromToken(token));
        boolean exist = service.existByMemberIdAndJibunAddr(memberId, jibunAddr);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.WISHLIST_FIND_SUCCESS,exist));
    }
}
