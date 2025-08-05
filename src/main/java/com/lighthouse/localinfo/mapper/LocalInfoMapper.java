package com.lighthouse.localinfo.mapper;

import com.lighthouse.localinfo.dto.LocalInfoResponseDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface LocalInfoMapper {

    /**
     * 키워드로 지역 목록 검색
     * @param keyword 검색 키워드
     * @return 검색된 지역 목록 (locataddNm 필드 포함)
     */
    List<LocalInfoResponseDTO> searchByKeyword(@Param("keyword") String keyword);

    /**
     * @param regionCd 조회할 전체 지역 주소명
     * @return 지역 상세 정보 (grid, xy 좌표 포함)를 담은 Optional 객체
     */
    Optional<LocalInfoResponseDTO> findByRegionCd(@Param("regionCd") String regionCd); // 추가
}