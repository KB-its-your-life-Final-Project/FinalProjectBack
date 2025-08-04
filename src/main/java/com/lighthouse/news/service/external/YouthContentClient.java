package com.lighthouse.news.service.external;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lighthouse.news.dto.YouthContentDTO;
import com.lighthouse.news.dto.YouthContentResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class YouthContentClient {
    private static final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String YOUTH_CONTENT_URL = "https://www.youthcenter.go.kr/go/ythip/getContent";

    @Value("${YOUTH_CONTENT_API_KEY}")
    private String apiKey;

    public List<YouthContentDTO> getYouthContents() {
        log.info("YouthContentClient.getNews() 실행 ======");
        try {
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(YOUTH_CONTENT_URL)
                    .queryParam("apiKeyNm", apiKey)         // 인가코드
                    .queryParam("pageNum", 1)        // 페이지 번호
                    .queryParam("pageSize", "100")   // 페이지 크기 (표시할 게시물 개수)
                    .queryParam("rtnType", "json");  // 호출 문서
            String uri = uriBuilder.toUriString();
            String jsonResponse = restTemplate.getForObject(uri, String.class);
            YouthContentResponseDTO response = objectMapper.readValue(jsonResponse, YouthContentResponseDTO.class);
            List<YouthContentDTO> youthContentDtoList =  response.getResult().getYouthPolicyList();
            log.info("youthContentDtoList: {}", youthContentDtoList);
            return youthContentDtoList;
        } catch (Exception e) {
            log.error("온통청년(청년콘텐츠) API 호출 중 예외 발생", e);
            throw new RuntimeException("온통청년(청년콘텐츠) API 호출 실패", e);
        }
    }
 }