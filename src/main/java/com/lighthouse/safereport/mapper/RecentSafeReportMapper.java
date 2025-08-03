package com.lighthouse.safereport.mapper;

import com.lighthouse.safereport.entity.RecentSafeReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface RecentSafeReportMapper {
    
    //사용자 ID와 estateId로 최근 본 안심레포트 조회
    RecentSafeReport findByUserIdAndEstateId(@Param("userId") Integer userId, @Param("estateId") Integer estateId);
    
    //최근 본 안심레포트 저장
    void insertRecentSafeReport(RecentSafeReport recentSafeReport);
    
    //최근 본 안심레포트 수정
    void updateRecentSafeReport(RecentSafeReport recentSafeReport);
    
    //사용자별 최근 본 안심레포트 목록 조회 (최신순)
    List<RecentSafeReport> findByUserIdOrderByCreatedAtDesc(@Param("userId") Integer userId);
    
    //특정 최근 본 안심레포트 조회
    RecentSafeReport findByIdAndUserId(@Param("id") Integer id, @Param("userId") Integer userId);
    
    //최근 본 안심레포트 삭제
    void deleteByIdAndUserId(@Param("id") Integer id, @Param("userId") Integer userId);
} 