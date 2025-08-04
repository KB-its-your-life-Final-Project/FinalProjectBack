package com.lighthouse.regionCode.service;

import com.lighthouse.regionCode.dto.RegionCdRequestDTO;
import com.lighthouse.regionCode.dto.RegionCdResponseDTO;
import com.lighthouse.regionCode.mapper.RegionCodeMapper;
import com.lighthouse.response.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class RegionCodeServiceTest {

    private RegionCodeService service;
    private RegionCodeMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mockito.mock(RegionCodeMapper.class);  // mapper mock 생성
        service = new RegionCodeService(mapper);        // 서비스에 mock 주입
    }

    @Test
    void findRegionByRegionCd_found() {
        // given
        String regionCd = "1168010300";
        RegionCdResponseDTO dto = new RegionCdResponseDTO();
        dto.setRegionCd(regionCd);
        dto.setLocallowNm("개포동");

        when(mapper.findByFullRegionCd(regionCd)).thenReturn(dto);

        // when
        RegionCdResponseDTO result = service.findRegionByRegionCd(regionCd);

        // then
        assertNotNull(result);
        assertEquals("개포동", result.getLocallowNm());
    }

    @Test
    void findRegionByRegionCd_notFound() {
        when(mapper.findByFullRegionCd("0000000000")).thenReturn(null);

        CustomException thrown = assertThrows(CustomException.class, () -> {
            service.findRegionByRegionCd("0000000000");
        });

        assertEquals("잘못된 인자가 포함된 요청입니다.", thrown.getMessage());
    }
    @Test
    void findAllRegionCdByPartialCd_returnsList() {
        // given
        RegionCdRequestDTO dto = new RegionCdRequestDTO();
        dto.setSidoCd("11");
        dto.setSggCd(null);
        dto.setUmdCd(null);

        RegionCdResponseDTO response1 = new RegionCdResponseDTO("1100000000", "서울시", "강남구", 100, 200);
        RegionCdResponseDTO response2 = new RegionCdResponseDTO("1100100000", "서울시", "서초구", 110, 210);

        List<RegionCdResponseDTO> mockResult = Arrays.asList(response1, response2);

        when(mapper.findAllRegionByPartialCd(dto)).thenReturn(mockResult);

        // when
        List<RegionCdResponseDTO> result = service.findAllRegionCdByPartialCd(dto);

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("1100000000", result.get(0).getRegionCd());
        assertEquals("서초구", result.get(1).getLocallowNm());
    }
}
