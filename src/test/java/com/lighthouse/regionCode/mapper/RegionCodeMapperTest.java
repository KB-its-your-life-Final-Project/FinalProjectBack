package com.lighthouse.regionCode.mapper;

import com.lighthouse.config.EnvLoader;
import com.lighthouse.config.RootConfig;
import com.lighthouse.regionCode.dto.RegionCdRequestDTO;
import com.lighthouse.regionCode.dto.RegionCdResponseDTO;
import com.lighthouse.security.config.SecurityConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = { RootConfig.class, SecurityConfig.class }, initializers = EnvLoader.class)
@Slf4j
@ActiveProfiles("local")
class RegionCodeMapperTest {
    @Autowired
    private RegionCodeMapper mapper;

    @Test
    void testFindUmdNmByRegionCd() {
        String testRegionCd = "1168010300";

        // when
        String umdNm = mapper.findByFullRegionCd(testRegionCd).getLocallowNm();

        // then
        assertThat(umdNm)
                .isNotNull()
                .isEqualTo("개포동"); // 예상 값, DB에 맞춰 조정 필요
    }

    @Test
    void findAllRegionByPartialCd() {
        // 전체 테스트용 DTO 변수들
        RegionCdRequestDTO dtoAllNull = new RegionCdRequestDTO(); // 모두 null
        RegionCdRequestDTO dtoAllEmpty = new RegionCdRequestDTO("","","");

        RegionCdRequestDTO dtoSidoOnly = new RegionCdRequestDTO();
        dtoSidoOnly.setSidoCd("11");

        RegionCdRequestDTO dtoSidoSsg = new RegionCdRequestDTO();
        dtoSidoSsg.setSidoCd("11");
        dtoSidoSsg.setSggCd("680");

        RegionCdRequestDTO dtoFull = new RegionCdRequestDTO();
        dtoFull.setSidoCd("11");
        dtoFull.setSggCd("680");
        dtoFull.setUmdCd("103");

        // 1. 모두 null 또는 빈 문자열 -> 전체 결과 또는 적절한 결과 반환 예상
        var resultNull = mapper.findAllRegionByPartialCd(dtoAllNull);
        log.info("resultNull: {}", resultNull.get(0).getRegionCd());
        assertThat(resultNull).isNotNull().isNotEmpty();

        var resultEmpty = mapper.findAllRegionByPartialCd(dtoAllEmpty);
        log.info("resultEmpty: {}", resultEmpty.get(0).getRegionCd());
        assertThat(resultEmpty).isNotNull().isNotEmpty();

        // 2. sidoCd만 지정
        var resultSidoOnly = mapper.findAllRegionByPartialCd(dtoSidoOnly);
        log.info("resultSidoOnly: {}", resultSidoOnly.get(0).getRegionCd());
        assertThat(resultSidoOnly).isNotNull().isNotEmpty();
        assertThat(resultSidoOnly).allMatch(dto -> dto.getRegionCd().startsWith("11"));

        // 3. sidoCd, sggCd 지정
        var resultSidoSsg = mapper.findAllRegionByPartialCd(dtoSidoSsg);
        log.info("resultSidoSsg: {}", resultSidoSsg.get(0).getRegionCd());
        assertThat(resultSidoSsg).isNotNull().isNotEmpty();
        assertThat(resultSidoSsg).allMatch(dto -> dto.getRegionCd().startsWith("11680"));

        // 4. sidoCd, ssgCd, umdCd 모두 지정 (완전 일치)
        var resultFull = mapper.findAllRegionByPartialCd(dtoFull);
        log.info("resultFull: {}", resultFull.get(0).getRegionCd());
        assertThat(resultFull).isNotNull().isNotEmpty();
        assertThat(resultFull).allMatch(dto -> dto.getRegionCd().equals("1168010300")); // 정확한 값은 DB에 따라 조정 필요
    }
    @Test
    void findByFullRegionCd() {
        // given
        String fullRegionCd = "1168010300"; // 실제 DB에 존재하는 10자리 전체 코드로 설정

        // when
        RegionCdResponseDTO result = mapper.findByFullRegionCd(fullRegionCd);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getRegionCd()).isEqualTo(fullRegionCd);
        // 예상 값이 있으면 추가 검증 가능
        assertThat(result.getLocallowNm()).isEqualTo("개포동"); // DB 값에 맞게 수정
    }
}