package com.lighthouse.lawdCode.service;

import com.lighthouse.config.EnvLoader;
import com.lighthouse.config.RootConfig;
import com.lighthouse.lawdCode.dto.LawdCdRequestDTO;
import com.lighthouse.lawdCode.dto.LawdCdResponseDTO;
import com.lighthouse.lawdCode.mapper.LawdCodeMapper;
import com.lighthouse.response.CustomException;
import com.lighthouse.security.config.SecurityConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

@Slf4j
@WebAppConfiguration
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { RootConfig.class, SecurityConfig.class }, initializers = EnvLoader.class)
@ActiveProfiles("local")
class LawdCodeServiceTest {

    private LawdCodeService service;
    private LawdCodeMapper mapper;

    @BeforeEach // Mock 사용 시 주석 제거
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

        assertEquals("해당 지역 코드를 찾을 수 없습니다.", thrown.getMessage());
    }
    @Test
    void findAllRegionCdByPartialCd_returnsList() {
        // given
        LawdCdRequestDTO dto = new LawdCdRequestDTO();
        dto.setSidoCd("11");
        dto.setSggCd(null);
        dto.setUmdCd(null);

        LawdCdResponseDTO response1 = new LawdCdResponseDTO(
                "1111013900","11","110","139","00",
                "1111013900","1111013900","서울특별시 종로구 팔판동",
                39,"","1111000000", "팔판동","",
                60, 127,
                LocalDateTime.of(2025, 8, 18, 15, 30, 0));
        LawdCdResponseDTO response2 = new LawdCdResponseDTO(
                "1111013900","11","110","139","00",
                "1111013900","1111013900","서울특별시 종로구 팔판동",
                39,"","1111000000", "팔판동","",
                60, 127,
                LocalDateTime.of(2025, 8, 18, 15, 30, 0));

        List<LawdCdResponseDTO> mockResult = Arrays.asList(response1, response2);

        when(mapper.findAllRegionByPartialCd(dto)).thenReturn(mockResult);

        // when
        List<LawdCdResponseDTO> result = service.findAllRegionCdByPartialCd(dto);

        // then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("1111013900", result.get(0).getRegionCd());
        assertEquals("팔판동", result.get(1).getLocallowNm());
    }

    private LawdCdResponseDTO createMockResponse(String regionCd) {
        LawdCdResponseDTO dto = new LawdCdResponseDTO();
        dto.setRegionCd(regionCd);
        dto.setLocallowNm("테스트동");
        return dto;
    }

    /**
     * (Mock) 고유 시군구코드 조회 테스트
     */
    @Test
    @DisplayName("getAllUniqueRegionCodesAsSet - 고유 시군구코드 조회 테스트")
    void getAllUniqueRegionCodesAsSetTest() {
        // Mock 데이터
        List<LawdCdResponseDTO> lawdCodes = Arrays.asList(
                createMockResponse("1100010100"), // 11000
                createMockResponse("1114010100"), // 11140
                createMockResponse("1100010200"), // 11000 (중복)
                createMockResponse(null)          // null (필터링됨)
        );
        // when - 테스트 대상 메소드 호출
        Set<Integer> result = service.getAllUniqueRegionCodesAsSet(lawdCodes);
        // then - 결과 검증
        assertThat(result).hasSize(2)
                .contains(11000, 11140);
    }

    /**
     * (Mock) 페이지네이션 사용한 고유 시군구코드 조회 테스트
     */
    @Test
    @DisplayName("getAllUniqueRegionCodesWithPagination - 페이지네이션 사용한 고유 시군구코드 조회 테스트")
    void getAllUniqueRegionCodesWithPaginationMockTest() {
        // Mock 데이터
        List<LawdCdResponseDTO> page1 = Arrays.asList(
                createMockResponse("1100010100"), // 11000
                createMockResponse("1114010100"), // 11140
                createMockResponse("1100010200")  // 11000 (중복)
        );
        List<LawdCdResponseDTO> page2 = Arrays.asList(
                createMockResponse("2872033025"), // 28720
                createMockResponse("2911010300"), // 29110
                createMockResponse("2872033026")  // 28720 (중복)
        );
        List<LawdCdResponseDTO> page3 = Arrays.asList();  // 빈 페이지 (종료 조건)

        // Mock 설정 - offset에 따라 다른 페이지 반환
        when(mapper.findAll(any(LawdCdRequestDTO.class))).thenAnswer(invocation -> {
            LawdCdRequestDTO requestDto = invocation.getArgument(0);
            int offset = requestDto.getOffset() != null ? requestDto.getOffset() : 0;
            switch (offset) {
                case 0: return page1;
                case 1000: return page2;
                default: return page3;
            }
        });

        // when - 테스트 대상 메소드 호출
        List<Integer> result = service.getAllUniqueRegionCodesWithPagination();
        // then - 결과 검증
        assertThat(result).hasSize(4)
                .containsExactly(11000, 11140, 28720, 29110) // 정렬된 순서로 검증
                .doesNotHaveDuplicates();
        // Mock 호출 검증
        verify(mapper, times(3)).findAll(any(LawdCdRequestDTO.class));
        // 페이지네이션 DTO 생성 검증
        ArgumentCaptor<LawdCdRequestDTO> dtoCaptor = ArgumentCaptor.forClass(LawdCdRequestDTO.class);
        verify(mapper, times(3)).findAll(dtoCaptor.capture());
        List<LawdCdRequestDTO> capturedDtos = dtoCaptor.getAllValues();
        assertThat(capturedDtos.get(0).getOffset()).isEqualTo(0);
        assertThat(capturedDtos.get(0).getLimit()).isEqualTo(1000);
        assertThat(capturedDtos.get(1).getOffset()).isEqualTo(1000);
        assertThat(capturedDtos.get(1).getLimit()).isEqualTo(1000);
        assertThat(capturedDtos.get(2).getOffset()).isEqualTo(2000);
        assertThat(capturedDtos.get(2).getLimit()).isEqualTo(1000);
    }

//    /**
//     * (실제 데이터) 모든 시군구코드 가져오기 테스트
//     */
//    @Test
//    @DisplayName("getAllUniqueRegionCodesWithPagination - 중복제거/정렬된 시군구코드 가져오기 테스트")
//    void getAllUniqueRegionCodesWithPaginationTest() {
//        List<Integer> allUniqueLawdCodes = service.getAllUniqueRegionCodesWithPagination();
//        log.info("📋 불러온 시군구 리스트: {}", allUniqueLawdCodes);
//        allUniqueLawdCodes.forEach(code -> System.out.print(code + " "));
//        assertFalse(allUniqueLawdCodes.isEmpty());
//    }
}
