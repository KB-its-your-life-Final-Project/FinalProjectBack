package com.lighthouse.youthProgram.service.external;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lighthouse.youthProgram.dto.YouthProgramDTO;
import com.lighthouse.youthProgram.dto.YouthProgramResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class YouthProgramClient {
    private static final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String YOUTH_PROGRAM_URL = "https://www.youthcenter.go.kr/go/ythip/getContent";
    private static final Set<String> ALLOWED_CATEGORIES = Set.of("취업지원", "직업훈련", "대외활동");

    @Value("${YOUTH_PROGRAM_API_KEY}")
    private String apiKey;

    public List<YouthProgramDTO> getYouthPrograms() {
        log.info("YouthProgramClient.getNews() 실행 ======");
        try {
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(YOUTH_PROGRAM_URL)
                    .queryParam("apiKeyNm", apiKey)         // 인가코드
                    .queryParam("pageNum", 1)        // 페이지 번호
                    .queryParam("pageSize", "100")   // 페이지 크기 (표시할 게시물 개수)
                    .queryParam("rtnType", "json");  // 호출 문서 종류
            String uri = uriBuilder.toUriString();
            String jsonResponse = restTemplate.getForObject(uri, String.class);
            YouthProgramResponseDTO response = objectMapper.readValue(jsonResponse, YouthProgramResponseDTO.class);
            List<YouthProgramDTO> youthContentAllList =  response.getResult().getYouthPolicyList();
            // 프로그램 관련 내용만 필터링
            List<YouthProgramDTO> youthProgramDtoList = youthContentAllList.stream()
                    .filter(program -> program.getPstSeNm() != null &&
                            ALLOWED_CATEGORIES.contains(program.getPstSeNm()))
                    .collect(Collectors.toList());
            log.info("전체 온통청년 콘텐츠 수: {}, 필터링된 콘텐츠 (프로그램) 수: {}",
                    youthContentAllList.size(), youthProgramDtoList.size());
            return youthProgramDtoList;
        } catch (Exception e) {
            log.error("온통청년(청년콘텐츠) API 호출 중 예외 발생", e);
            throw new RuntimeException("온통청년(청년콘텐츠) API 호출 실패", e);
        }
    }
 }