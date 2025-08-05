package com.lighthouse.homeregister.mapper;

import com.lighthouse.homeregister.entity.HomeRegister;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface HomeRegisterMapper {
    // 집 정보 등록
    void insertHome(HomeRegister homeEntity);
    
    // 사용자별 집 정보 조회
    HomeRegister selectHomeByUserId(@Param("userId") Integer userId);
    
    // 집 정보 수정
    void updateHome(HomeRegister homeEntity);
    
}
