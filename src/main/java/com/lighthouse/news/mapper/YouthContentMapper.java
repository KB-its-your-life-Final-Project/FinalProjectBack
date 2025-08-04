package com.lighthouse.news.mapper;

import com.lighthouse.news.dto.YouthContentDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface YouthContentMapper {
    Boolean existsByPostSn(String pstSn);

    void insertYouthContent(YouthContentDTO dto);

    List<YouthContentDTO> findAllUnreadContents(@Param("memberId") Long memberId, @Param("offset") int offset, @Param("size") int size);
}
