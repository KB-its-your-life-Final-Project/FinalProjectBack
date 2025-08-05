package com.lighthouse.lawdCode.service;

import com.lighthouse.lawdCode.dto.LawdCdRequestDTO;
import com.lighthouse.lawdCode.dto.LawdCdResponseDTO;
import com.lighthouse.lawdCode.mapper.LawdCodeMapper;
import com.lighthouse.response.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.YearMonth;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
//    @Test
//    void findAllRegionCdByPartialCd_returnsList() {
//        // given
//        LawdCdRequestDTO dto = new LawdCdRequestDTO();
//        dto.setSidoCd("11");
//        dto.setSggCd(null);
//        dto.setUmdCd(null);
//
//        LawdCdResponseDTO response1 = new LawdCdResponseDTO("1100000000", "서울시", "강남구", 100, 200);
//        LawdCdResponseDTO response2 = new LawdCdResponseDTO("1100100000", "서울시", "서초구", 110, 210);
//
//        List<LawdCdResponseDTO> mockResult = Arrays.asList(response1, response2);
//
//        when(mapper.findAllRegionByPartialCd(dto)).thenReturn(mockResult);
//
//        // when
//        List<LawdCdResponseDTO> result = service.findAllRegionCdByPartialCd(dto);
//
//        // then
//        assertNotNull(result);
//        assertEquals(2, result.size());
//        assertEquals("1100000000", result.get(0).getRegionCd());
//        assertEquals("서초구", result.get(1).getLocallowNm());
//    }

    @Test
    @DisplayName("시군구코드 1000개씩 가져오기 테스트")
    void getUniqueRegionCodesAsList_paginationLoop() {
        // 1000개의 데이터를 반복해서 가져옴
        LawdCdRequestDTO baseDto = new LawdCdRequestDTO();
        baseDto.setLimit(1000);

        // Mock 데이터
        List<LawdCdResponseDTO> page1 = Arrays.asList(
                createMockResponse("1100010100"), // 11000
                createMockResponse("1114010100") // 11140
        );
        List<LawdCdResponseDTO> page2 = Arrays.asList(
                createMockResponse("2872033025"), // 28720
                createMockResponse("2911010300")  // 29110
        );
        List<LawdCdResponseDTO> page3 = Arrays.asList(); // 빈 페이지

        when(mapper.findAll(any(LawdCdRequestDTO.class))).thenAnswer(invocation -> {
            LawdCdRequestDTO requestDto = invocation.getArgument(0);
            int offset = requestDto.getOffset() != null ? requestDto.getOffset() : 0;

            if (offset == 0) return page1;
            else if (offset == 1000) return page2;
            else return page3;
        });

        // when - 루프
        Set<Integer> allUniqueCodes = new HashSet<>();
        int currentOffset = 0;

        while (true) {
            LawdCdRequestDTO dto = new LawdCdRequestDTO();
            dto.setLimit(1000);
            dto.setOffset(currentOffset);
            List<Integer> pageResult = service.getUniqueRegionCodesAsList(dto);
            if (pageResult.isEmpty()) break;
            allUniqueCodes.addAll(pageResult);
            currentOffset += 1000;
        }

        // then
        assertEquals(4, allUniqueCodes.size());
        assertTrue(allUniqueCodes.contains(11000));
        assertTrue(allUniqueCodes.contains(11140));
        assertTrue(allUniqueCodes.contains(28720));
        assertTrue(allUniqueCodes.contains(29110));
    }

    private LawdCdResponseDTO createMockResponse(String regionCd) {
        LawdCdResponseDTO dto = new LawdCdResponseDTO();
        dto.setRegionCd(regionCd);
        dto.setLocallowNm("테스트동");
        return dto;
    }
}
