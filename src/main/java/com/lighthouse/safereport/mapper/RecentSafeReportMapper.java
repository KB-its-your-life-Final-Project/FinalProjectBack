package com.lighthouse.safereport.mapper;

import com.lighthouse.safereport.entity.RecentSafeReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface RecentSafeReportMapper {
    
    /**
     * 사용자 ID와 위치로 최근 본 안심레포트 조회
     */
    RecentSafeReport findByUserIdAndLocation(@Param("userId") Long userId, 
                                           @Param("latitude") BigDecimal latitude, 
                                           @Param("longitude") BigDecimal longitude);
    
    /**
     * 최근 본 안심레포트 저장
     */
    void insertRecentSafeReport(RecentSafeReport recentSafeReport);
    
    /**
     * 최근 본 안심레포트 수정
     */
    void updateRecentSafeReport(RecentSafeReport recentSafeReport);
    
    /**
     * 사용자별 최근 본 안심레포트 목록 조회 (최신순)
     */
    List<RecentSafeReport> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
    
    /**
     * 특정 최근 본 안심레포트 조회
     */
    RecentSafeReport findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
    
    /**
     * 최근 본 안심레포트 삭제
     */
    void deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
} 