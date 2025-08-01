package com.lighthouse.localinfo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lighthouse.localinfo.vo.WeatherVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherService {

    @Value("${data.go.kr.api.key}")
    private String apiKey;

    private final String BASE_URL = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst";

    public WeatherVO getWeatherFromKMA(int gridX, int gridY) {
        return fetchCommon(gridX, gridY, "날씨 정보");
    }

    private WeatherVO fetchCommon(int gridX, int gridY, String logPrefix) {
        try {
            // 현재 시간 기준으로 base_date와 base_time 계산
            String baseTime = getBaseTimeForShortTermForecast();
            String baseDate = getBaseDateForShortTermForecast(baseTime);

            // 기상청 샘플 코드 방식으로 URL 구성
            StringBuilder urlBuilder = new StringBuilder(BASE_URL);
            urlBuilder.append("?" + URLEncoder.encode("serviceKey","UTF-8") + "=" + apiKey);
            urlBuilder.append("&" + URLEncoder.encode("pageNo","UTF-8") + "=" + URLEncoder.encode("1", "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("numOfRows","UTF-8") + "=" + URLEncoder.encode("100", "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("dataType","UTF-8") + "=" + URLEncoder.encode("JSON", "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("base_date","UTF-8") + "=" + URLEncoder.encode(baseDate, "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("base_time","UTF-8") + "=" + URLEncoder.encode(baseTime, "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("nx","UTF-8") + "=" + URLEncoder.encode(String.valueOf(gridX), "UTF-8"));
            urlBuilder.append("&" + URLEncoder.encode("ny","UTF-8") + "=" + URLEncoder.encode(String.valueOf(gridY), "UTF-8"));

            String url = urlBuilder.toString();
            log.info("{} API 요청 URL: {}", logPrefix, url);

            // HttpURLConnection으로 API 호출 (기상청 샘플 방식)
            URL apiUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) apiUrl.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-type", "application/json");

            log.info("Response code: {}", conn.getResponseCode());

            // 응답 읽기
            BufferedReader rd;
            if(conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
                rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            } else {
                rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
            }

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            rd.close();
            conn.disconnect();

            String response = sb.toString();
            log.debug("API 응답: {}", response);

            return parseWeatherResponse(response);

        } catch (Exception e) {
            log.error("❌ {} 데이터 처리 중 예기치 않은 오류 발생: {}", logPrefix, e.getMessage(), e);
            return createMockWeatherData();
        }
    }

    private WeatherVO parseWeatherResponse(String jsonResponse) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(jsonResponse);

            // 응답 헤더 확인
            JsonNode header = root.path("response").path("header");
            String resultCode = header.path("resultCode").asText();
            String resultMsg = header.path("resultMsg").asText();

            if (!"00".equals(resultCode)) {
                log.warn("❌ 날씨 API 응답 오류 - 코드: {}, 메시지: {}", resultCode, resultMsg);
                return createMockWeatherData();
            }

            JsonNode itemsNode = root.path("response").path("body").path("items").path("item");
            if (!itemsNode.isArray() || itemsNode.size() == 0) {
                log.warn("날씨 API 응답에 아이템이 없거나 형식이 올바르지 않습니다.");
                return createMockWeatherData();
            }

            WeatherVO weatherVO = new WeatherVO();
            Integer latestTmp = null;
            String latestSky = null;
            String latestFcstTime = null;

            // 각 아이템을 순회하며 날씨 정보 추출
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
                    case "SKY": // 하늘 상태
                        if (latestFcstTime == null || fcstTime.compareTo(latestFcstTime) >= 0) {
                            latestFcstTime = fcstTime;
                            try {
                                latestSky = mapSkyCodeToCondition(Integer.parseInt(fcstValue));
                            } catch (NumberFormatException e) {
                                log.warn("SKY 값 파싱 오류: {}", fcstValue);
                            }
                        }
                        break;
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

            // 파싱된 값들을 WeatherVO에 설정
            if (latestTmp != null) weatherVO.setTemperature(latestTmp);
            if (latestSky != null) weatherVO.setSkyCondition(latestSky);

            // 필수 값이 없으면 mock 데이터로 보완
            if (weatherVO.getTemperature() == null) weatherVO.setTemperature(22);
            if (weatherVO.getMaxTemperature() == null) weatherVO.setMaxTemperature(25);
            if (weatherVO.getMinTemperature() == null) weatherVO.setMinTemperature(18);
            if (weatherVO.getSkyCondition() == null) weatherVO.setSkyCondition("맑음");

            log.info("날씨 데이터 파싱 완료: {}", weatherVO);
            return weatherVO;

        } catch (JsonProcessingException e) {
            log.error("❌ 날씨 정보 JSON 파싱 실패: {}", e.getMessage(), e);
            return createMockWeatherData();
        } catch (Exception e) {
            log.error("❌ 날씨 정보 파싱 중 예기치 않은 오류 발생: {}", e.getMessage(), e);
            return createMockWeatherData();
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

    private String getBaseTimeForShortTermForecast() {
        LocalTime now = LocalTime.now();

        // KMA API 데이터 제공 시간: 02:00, 05:00, 08:00, 11:00, 14:00, 17:00, 20:00, 23:00
        if (now.isBefore(LocalTime.of(2, 10))) {
            return "2300"; // 전날 23:00
        } else if (now.isBefore(LocalTime.of(5, 10))) {
            return "0200";
        } else if (now.isBefore(LocalTime.of(8, 10))) {
            return "0500";
        } else if (now.isBefore(LocalTime.of(11, 10))) {
            return "0800";
        } else if (now.isBefore(LocalTime.of(14, 10))) {
            return "1100";
        } else if (now.isBefore(LocalTime.of(17, 10))) {
            return "1400";
        } else if (now.isBefore(LocalTime.of(20, 10))) {
            return "1700";
        } else if (now.isBefore(LocalTime.of(23, 10))) {
            return "2000";
        } else {
            return "2300";
        }
    }

    private String getBaseDateForShortTermForecast(String baseTime) {
        LocalDate today = LocalDate.now();

        // 23:00인 경우 전날 데이터 사용
        if ("2300".equals(baseTime)) {
            return today.minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        } else {
            return today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        }
    }

    private WeatherVO createMockWeatherData() {
        WeatherVO mockWeather = new WeatherVO();
        mockWeather.setTemperature(22);
        mockWeather.setMaxTemperature(25);
        mockWeather.setMinTemperature(18);
        mockWeather.setSkyCondition("맑음");
        log.info("Mock 날씨 데이터 생성: {}", mockWeather);
        return mockWeather;
    }
}