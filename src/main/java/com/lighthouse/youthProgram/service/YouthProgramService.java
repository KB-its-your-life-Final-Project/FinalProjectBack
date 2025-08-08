package com.lighthouse.youthProgram.service;

import com.lighthouse.youthProgram.dto.YouthProgramDTO;
import com.lighthouse.youthProgram.mapper.YouthProgramMapper;
import com.lighthouse.youthProgram.mapper.YouthProgramReadMapper;
import com.lighthouse.youthProgram.service.external.YouthProgramClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class YouthProgramService {
    private final YouthProgramMapper youthProgramMapper;
    private final YouthProgramReadMapper youthProgramReadMapper;
    private final YouthProgramClient youthProgramClient;

    @Transactional
    public void syncYouthProgramsFromApi() {
        List<YouthProgramDTO> contentList = youthProgramClient.getYouthPrograms();
        for (YouthProgramDTO dto : contentList) {
            if (!youthProgramMapper.existsByPostSn(dto.getPstSn())) {
                youthProgramMapper.insertYouthProgram(dto);
            }
        }
    }

    public List<YouthProgramDTO> getUnreadPrograms(Long memberId, int page, int size) {
        int offset = (page - 1) * size;
        return youthProgramMapper.findAllUnreadPrograms(memberId, offset, size);
    }

    public void markProgramAsRead(Long memberId, Long contentId) {
        youthProgramReadMapper.insertReadRecord(memberId, contentId);
    }
}
