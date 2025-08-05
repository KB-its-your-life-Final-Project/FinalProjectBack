package com.lighthouse.lawdCode.mapper;

import com.lighthouse.lawdCode.dto.DongDto;
import com.lighthouse.lawdCode.dto.SidoDto;
import com.lighthouse.lawdCode.dto.SigugunDto;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AddressMapper {
    // 시/도 목록 조회 (코드와 이름 함께)
    List<SidoDto> selectDistinctSidoWithName();

    // 시/군/구 목록 조회 (시/도별) - 코드와 이름 함께
    List<SigugunDto> selectDistinctSggWithNameBySidoCd(@Param("sidoCd") String sidoCd);

    // 읍/면/동 목록 조회 (시/도별, 시/군/구별)
    List<DongDto> selectDistinctDongBySidoCdAndSggCd(@Param("sidoCd") String sidoCd, @Param("sggCd") String sggCd);
    
    // 건물명 목록 조회 (지역코드와 읍면동명으로)
    List<String> selectBuildingNamesByRegionCodeAndDongName(@Param("regionCode") String regionCode, @Param("dongName") String dongName);
}
