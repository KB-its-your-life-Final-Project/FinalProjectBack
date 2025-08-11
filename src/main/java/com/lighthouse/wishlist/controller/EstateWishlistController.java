package com.lighthouse.wishlist.controller;

import com.lighthouse.response.ApiResponse;
import com.lighthouse.response.SuccessCode;
import com.lighthouse.security.util.JwtUtil;
import com.lighthouse.wishlist.dto.EstateWishlistRequestDTO;
import com.lighthouse.wishlist.dto.EstateWishlistResponseDTO;
import com.lighthouse.wishlist.service.EstateWishlistService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
    @ApiOperation(
            value = "관심 목록에 등록",
            notes = "회원이 특정 건물을 관심 목록으로 등록합니다."
    )
    public ResponseEntity<ApiResponse<Void>> addWishlist (@RequestBody EstateWishlistRequestDTO dto, @ApiIgnore @CookieValue("accessToken") String token) {
        Long memberId = Long.valueOf(jwtUtil.getSubjectFromToken(token));
        service.saveOrUpdateWishlist(memberId,dto);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.WISHLIST_SAVE_SUCCESS));
    }
    @DeleteMapping("")
    @ApiOperation(
            value = "관심 목록(건물) 삭제",
            notes = "등록한 관심 목록(지역이나 건물)을 삭제합니다."
    )
    public ResponseEntity<ApiResponse<Void>> removeWishlist (@RequestParam String jibunAddr, @ApiIgnore @CookieValue("accessToken") String token) {
        Long memberId = Long.valueOf(jwtUtil.getSubjectFromToken(token));
        service.deleteWishlist(memberId,jibunAddr);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.WISHLIST_DELETE_SUCCESS));
    }
    @GetMapping("")
    @ApiOperation(
            value = "회원의 관심 목록 조회",
            notes = "등록된 모든 관심 목록을 조회합니다."
    )
    public ResponseEntity<ApiResponse<List<EstateWishlistResponseDTO>>> getEstateIdsByMemberId(@ApiIgnore @CookieValue("accessToken") String token) {
        Long memberId = Long.valueOf(jwtUtil.getSubjectFromToken(token));
        List<EstateWishlistResponseDTO> result = service.getAllEstateByMemberId(memberId);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.WISHLIST_GETLIST_SUCCESS,result));
    }
    @GetMapping("/check")
    @ApiOperation(
            value = "관심 등록 여부 확인",
            notes = "관심 리스트에 등록했는지 여부를 반환합니다."
    )
    public ResponseEntity<ApiResponse<Boolean>> existWishlistByJibunAddr (@RequestParam String jibunAddr, @ApiIgnore @CookieValue("accessToken") String token) {
        Long memberId = Long.valueOf(jwtUtil.getSubjectFromToken(token));
        boolean exist = service.existByMemberIdAndJibunAddr(memberId, jibunAddr);
        return ResponseEntity.ok(ApiResponse.success(SuccessCode.WISHLIST_FIND_SUCCESS,exist));
    }
}
