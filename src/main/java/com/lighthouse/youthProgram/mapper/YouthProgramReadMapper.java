package com.lighthouse.youthProgram.mapper;

import org.apache.ibatis.annotations.Param;

public interface YouthProgramReadMapper {
    void insertReadRecord(@Param("memberId") Long memberId, @Param("contentId") Long contentId);
}
