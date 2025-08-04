package com.lighthouse.localinfo.mapper;

import com.lighthouse.localinfo.dto.SafetyDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.Optional;

@Mapper
public interface SafetyMapper {
    Optional<SafetyDTO> findSafetyDetailsByRegionCd(@Param("regionCd") String regionCd);
}
