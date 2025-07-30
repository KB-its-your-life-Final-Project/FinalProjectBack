package com.lighthouse.localinfo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lighthouse.localinfo.vo.WeatherVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // Slf4j 로거 사용
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate; // RestTemplate 사용
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j // Slf4j 로거 사용 (log.info, log.error 등)
public class WeatherService {

    @Value("${data.go.kr.api.key}")
    private String apiKey;

    private final String BASE_URL = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst";

    public WeatherVO getWeatherFromKMA(int gridX, int gridY) {
        return fetchCommon(gridX, gridY, "날씨 정보");
    }

    private WeatherVO fetchCommon(int gridX, int gridY, String logPrefix) {
        try {
            LocalDateTime now = LocalDateTime.now();
            String baseTime = getBaseTimeForShortTermForecast(now);
            String baseDate;

            if ("2300".equals(baseTime) && now.toLocalTime().isBefore(LocalTime.of(2, 10))) {
                baseDate = now.minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            } else {
                baseDate = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            }

            URI uri = UriComponentsBuilder
                    .fromUriString(BASE_URL)
                    .queryParam("serviceKey", apiKey)
                    .queryParam("pageNo", "1")
                    .queryParam("numOfRows", "100")
                    .queryParam("dataType", "JSON")
                    .queryParam("base_date", baseDate)
                    .queryParam("base_time", baseTime)
                    .queryParam("nx", String.valueOf(gridX))
                    .queryParam("ny", String.valueOf(gridY))
                    .build(true)
                    .toUri();

            log.info("{} API 요청 URL: {}", logPrefix, uri);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);

            return parseWeatherResponse(response.getBody());

        } catch (RestClientException e) {
            log.error("❌ {} API 통신 오류: {}", logPrefix, e.getMessage(), e);
            return null;
        } catch (Exception e) {
            log.error("❌ {} 데이터 처리 중 예기치 않은 오류 발생: {}", logPrefix, e.getMessage(), e);
            return null;
        }
    }

    private WeatherVO parseWeatherResponse(String jsonResponse) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(jsonResponse);

            JsonNode header = root.path("response").path("header");
            String resultCode = header.path("resultCode").asText();
            String resultMsg = header.path("resultMsg").asText();

            if (!"00".equals(resultCode)) {
                log.warn("❌ 날씨 API 응답 오류 - 코드: {}, 메시지: {}", resultCode, resultMsg);
                return null;
            }

            JsonNode itemsNode = root.path("response").path("body").path("items").path("item");
            if (!itemsNode.isArray() || itemsNode.isEmpty()) {
                log.warn("날씨 API 응답에 아이템이 없거나 형식이 올바르지 않습니다.");
                return null;
            }

            WeatherVO weatherVO = new WeatherVO();

            // TMX와 TMN은 하루에 한 번만 제공되므로, 먼저 찾으면 저장합니다.
            // TMP, SKY는 가장 최신 예보 시간(fcstTime) 기준으로 설정합니다.
            Integer latestTmp = null;
            String latestSky = null;
            String latestFcstTime = null; // TMP, SKY를 위한 가장 최신 예보 시간

            for (JsonNode item : itemsNode) {
                String category = item.path("category").asText();
                String fcstValue = item.path("fcstValue").asText();
                String fcstTime = item.path("fcstTime").asText();

                switch (category) {
                    case "TMP": // 1시간 기온
                        if (latestFcstTime == null || fcstTime.compareTo(latestFcstTime) >= 0) {
                            latestFcstTime = fcstTime;
                            try {
                                latestTmp = Double.valueOf(fcstValue).intValue();
                            } catch (NumberFormatException e) {
                                log.warn("TMP 값 파싱 오류: {}", fcstValue);
                            }
                        }
                        break;
                    case "SKY": // 하늘 상태 (1:맑음, 3:구름많음, 4:흐림)
                        if (latestFcstTime == null || fcstTime.compareTo(latestFcstTime) >= 0) {
                            latestFcstTime = fcstTime;
                            try {
                                latestSky = mapSkyCodeToCondition(Integer.parseInt(fcstValue));
                            } catch (NumberFormatException e) {
                                log.warn("SKY 값 파싱 오류: {}", fcstValue);
                            }
                        }
                        break;
                    // PTY (강수 형태) 파싱 로직은 WeatherVO에 해당 필드가 없으므로 제외합니다.
                    // case "PTY": // 강수 형태 (0:없음, 1:비, 2:비/눈, 3:눈, 4:소나기)
                    //     if (latestFcstTime == null || fcstTime.compareTo(latestFcstTime) >= 0) {
                    //         latestFcstTime = fcstTime;
                    //         try {
                    //             weatherVO.setPrecipitationType(mapPtyCodeToType(Integer.parseInt(fcstValue)));
                    //         } catch (NumberFormatException e) {
                    //             log.warn("PTY 값 파싱 오류: {}", fcstValue);
                    //         }
                    //     }
                    //     break;
                    case "TMX": // 일 최고 기온
                        if (weatherVO.getMaxTemperature() == null) {
                            try {
                                weatherVO.setMaxTemperature(Double.valueOf(fcstValue).intValue());
                            } catch (NumberFormatException e) {
                                log.warn("TMX 값 파싱 오류: {}", fcstValue);
                            }
                        }
                        break;
                    case "TMN": // 일 최저 기온
                        if (weatherVO.getMinTemperature() == null) {
                            try {
                                weatherVO.setMinTemperature(Double.valueOf(fcstValue).intValue());
                            } catch (NumberFormatException e) {
                                log.warn("TMN 값 파싱 오류: {}", fcstValue);
                            }
                        }
                        break;
                }
            }

            if (latestTmp != null) weatherVO.setTemperature(latestTmp);
            if (latestSky != null) weatherVO.setSkyCondition(latestSky);

            log.info("날씨 데이터 파싱 완료: {}", weatherVO);
            return weatherVO;

        } catch (JsonProcessingException e) {
            log.error("❌ 날씨 정보 JSON 파싱 실패: {}", e.getMessage(), e);
            return null;
        } catch (Exception e) {
            log.error("❌ 날씨 정보 파싱 중 예기치 않은 오류 발생: {}", e.getMessage(), e);
            return null;
        }
    }

    private String mapSkyCodeToCondition(int skyCode) {
        switch (skyCode) {
            case 1: return "맑음";
            case 3: return "구름많음";
            case 4: return "흐림";
            default: return "알 수 없음";
        }
    }

    private String getBaseTimeForShortTermForecast(LocalDateTime now) {
        LocalTime nowTime = now.toLocalTime();
        if (nowTime.isBefore(LocalTime.of(2, 10))) {
            return "2300";
        } else if (nowTime.isBefore(LocalTime.of(5, 10))) {
            return "0200";
        } else if (nowTime.isBefore(LocalTime.of(8, 10))) {
            return "0500";
        } else if (nowTime.isBefore(LocalTime.of(11, 10))) {
            return "0800";
        } else if (nowTime.isBefore(LocalTime.of(14, 10))) {
            return "1100";
        } else if (nowTime.isBefore(LocalTime.of(17, 10))) {
            return "1400";
        } else if (nowTime.isBefore(LocalTime.of(20, 10))) {
            return "1700";
        } else if (nowTime.isBefore(LocalTime.of(23, 10))) {
            return "2000";
        } else {
            return "2300";
        }
    }
}