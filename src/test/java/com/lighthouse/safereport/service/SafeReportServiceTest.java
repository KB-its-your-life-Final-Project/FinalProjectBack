package com.lighthouse.safereport.service;

import com.lighthouse.safereport.mapper.SafeReportMapper;
import com.lighthouse.safereport.vo.FormDataVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SafeReportServiceTest {

    @Mock
    private SafeReportMapper mockMapper;

    @InjectMocks
    private SafeReportService service;

    @Test
    void getReportByRoadAddress() {
        //given
        double lat = 37.1234;
        double lng = 127.5678;

        FormDataVO expected = new FormDataVO();
        expected.setDealAmount(25000);
        expected.setBuildYear(2015);

        when(mockMapper.selectByCoord(lat,lng)).thenReturn(expected);

        //when
        FormDataVO result = service.getReportByRoadAddress(lat,lng);

        //then
        assertThat(result.getDealAmount()).isEqualTo(25000);
        assertThat(result.getBuildYear()).isEqualTo(2015);
    }
}