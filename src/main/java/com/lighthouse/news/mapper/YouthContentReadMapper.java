package com.lighthouse.news.mapper;

import org.apache.ibatis.annotations.Param;

public interface YouthContentReadMapper {
    void insertReadRecord(@Param("memberId") Long memberId, @Param("contentId") Long contentId);
}
