package com.lighthouse.lawdCode.service;

import com.lighthouse.lawdCode.dto.LawdCdRequestDTO;
import com.lighthouse.lawdCode.dto.LawdCdResponseDTO;
import com.lighthouse.lawdCode.mapper.LawdCodeMapper;
import com.lighthouse.response.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class LawdCodeServiceTest {

    private LawdCodeService service;
    private LawdCodeMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = Mockito.mock(LawdCodeMapper.class);  // mapper mock 생성
        service = new com.lighthouse.lawdCode.service.LawdCodeService(mapper);        // 서비스에 mock 주입
    }

    @Test
    void findRegionByRegionCd_found() {
        // given
        String regionCd = "1168010300";
        LawdCdResponseDTO dto = new LawdCdResponseDTO();
        dto.setRegionCd(regionCd);
        dto.setLocallowNm("개포동");

        when(mapper.findByFullRegionCd(regionCd)).thenReturn(dto);

        // when
        LawdCdResponseDTO result = service.findRegionByRegionCd(regionCd);

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
        LawdCdRequestDTO dto = new LawdCdRequestDTO();
        dto.setSidoCd("11");
        dto.setSggCd(null);
        dto.setUmdCd(null);

        LawdCdResponseDTO response1 = new LawdCdResponseDTO("1100000000", "서울시", "강남구", 100, 200);
        LawdCdResponseDTO response2 = new LawdCdResponseDTO("1100100000", "서울시", "서초구", 110, 210);

        List<LawdCdResponseDTO> mockResult = Arrays.asList(response1, response2);

        when(mapper.findAllRegionByPartialCd(dto)).thenReturn(mockResult);

        // when
        List<LawdCdResponseDTO> result = service.findAllRegionCdByPartialCd(dto);

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("1100000000", result.get(0).getRegionCd());
        assertEquals("서초구", result.get(1).getLocallowNm());
    }
}
