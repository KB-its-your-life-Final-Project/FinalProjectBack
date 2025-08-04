package com.lighthouse.news.service;

import com.lighthouse.news.dto.YouthContentDTO;
import com.lighthouse.news.mapper.YouthContentMapper;
import com.lighthouse.news.mapper.YouthContentReadMapper;
import com.lighthouse.news.service.external.YouthContentClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class YouthContentService {
    private final YouthContentMapper youthContentMapper;
    private final YouthContentReadMapper youthContentReadMapper;
    private final YouthContentClient youthContentClient;

    @Transactional
    public void syncYouthContentsFromApi() {
        List<YouthContentDTO> contentList = youthContentClient.getYouthContents();
        for (YouthContentDTO dto : contentList) {
            if (!youthContentMapper.existsByPostSn(dto.getPstSn())) {
                youthContentMapper.insertYouthContent(dto);
            }
        }
    }

    public List<YouthContentDTO> getUnreadContents(Long memberId, int page, int size) {
        int offset = (page - 1) * size;
        return youthContentMapper.findAllUnreadContents(memberId, offset, size);
    }

    public void markContentAsRead(Long memberId, Long contentId) {
        youthContentReadMapper.insertReadRecord(memberId, contentId);
    }
}
