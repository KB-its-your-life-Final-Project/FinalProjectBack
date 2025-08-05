package com.lighthouse.youthProgram.mapper;

import com.lighthouse.youthProgram.dto.YouthProgramDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface YouthProgramMapper {
    Boolean existsByPostSn(String pstSn);

    void insertYouthProgram(YouthProgramDTO dto);

    List<YouthProgramDTO> findAllUnreadPrograms(@Param("memberId") Long memberId, @Param("offset") int offset, @Param("size") int size);
}
