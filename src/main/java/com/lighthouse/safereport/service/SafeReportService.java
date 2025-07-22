package com.lighthouse.safereport.service;

import com.lighthouse.safereport.mapper.SafeReportMapper;
import com.lighthouse.safereport.vo.FormDataVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SafeReportService {
    private final SafeReportMapper safeReportMapper;
    public FormDataVO getReportByRoadAddress(double lat, double lng) {
        return safeReportMapper.selectByCoord(lat, lng);
    }
}
