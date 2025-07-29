package com.lighthouse.transactions.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.lighthouse.transactions.dto.TransactionApiDTO;
import com.lighthouse.transactions.mapper.TransactionMapper;
import com.lighthouse.transactions.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class ApiService {
    @Value("${DATA_GO_KR_API_KEY}")
    private String apiKey;
    final private TransactionMapper mapper;
    final String BASE_URL = "https://apis.data.go.kr/1613000/";
    private <T> TransactionApiDTO<T> apiRequest(String url, int lawdCd, int dealYmd, Class<T> itemType) throws Exception {
        String urlStr = UriComponentsBuilder
                .fromHttpUrl(url)
                .queryParam("LAWD_CD", String.format("%05d", lawdCd))
                .queryParam("DEAL_YMD", dealYmd)
                .toUriString();
        urlStr += "&serviceKey=" + apiKey;
        XmlMapper xmlMapper = new XmlMapper();
        JavaType type = xmlMapper.getTypeFactory().constructParametricType(TransactionApiDTO.class, itemType);
        return xmlMapper.readValue(new URL(urlStr), type);
    }
    private <T> void insertCommon(String endpoint, int lawdCd, int dealYmd, Class<T> clazz, SaveHandler<T> handler, String logPrefix) {
        final String url = BASE_URL + endpoint;
        try {
            TransactionApiDTO<T> response = apiRequest(url, lawdCd, dealYmd, clazz);
            if (!"000".equals(response.getHeader().getResultCode())) {
                log.warn("❌ {} API 실패 - 코드: {}, 메시지: {}", logPrefix, response.getHeader().getResultCode(), response.getHeader().getResultMsg());
                return;
            }
            handler.save(response.getBody().getItems());
        } catch (Exception e) {
            log.error("❌ {} 데이터 요청 실패", logPrefix, e);
        }
    }

    public void insertApartmentTrades(int lawdCd, int dealYmd) {
        insertCommon("/RTMSDataSvcAptTrade/getRTMSDataSvcAptTrade",
                lawdCd, dealYmd, ApartmentTradeVO.class, mapper::insertApartmentTradeBatch, "아파트 매매");
    }

    public void insertApartmentRentals(int lawdCd, int dealYmd) {
        insertCommon("/RTMSDataSvcAptRent/getRTMSDataSvcAptRent",
                lawdCd, dealYmd, ApartmentRentalVO.class, mapper::insertApartmentRentalBatch, "아파트 전월세");
    }

    public void insertMultiHouseTrades(int lawdCd, int dealYmd) {
        insertCommon("/RTMSDataSvcRHTrade/getRTMSDataSvcRHTrade",
                lawdCd, dealYmd, MultiHouseTradeVO.class, mapper::insertMultiHouseTradeBatch, "연립다세대 매매");
    }

    public void insertMultiHouseRentals(int lawdCd, int dealYmd) {
        insertCommon("/RTMSDataSvcRHRent/getRTMSDataSvcRHRent",
                lawdCd, dealYmd, MultiHouseRentalVO.class, mapper::insertMultiHouseRentalBatch, "연립다세대 전월세");
    }

    public void insertOfficetelTrade(int lawdCd, int dealYmd) {
        insertCommon("/RTMSDataSvcOffiTrade/getRTMSDataSvcOffiTrade",
                lawdCd, dealYmd, OfficetelTradeVO.class, mapper::insertOfficetelTradeBatch, "오피스텔 매매");
    }

    public void insertOfficetelRental(int lawdCd, int dealYmd) {
        insertCommon("/RTMSDataSvcOffiRent/getRTMSDataSvcOffiRent",
                lawdCd, dealYmd, OfficetelRentalVO.class, mapper::insertOfficetelRentalBatch, "오피스텔 전월세");
    }

    public void insertSingleHouseTrade(int lawdCd, int dealYmd) {
        insertCommon("/RTMSDataSvcSHTrade/getRTMSDataSvcSHTrade",
                lawdCd, dealYmd, SingleHouseTradeVO.class, mapper::insertSingleHouseTradeBatch, "단독/다가구 매매");
    }

    public void insertSingleHouseRental(int lawdCd, int dealYmd) {
        insertCommon("/RTMSDataSvcSHRent/getRTMSDataSvcSHRent",
                lawdCd, dealYmd, SingleHouseRentalVO.class, mapper::insertSingleHouseRentalBatch, "단독/다가구 전월세");
    }

    private List<LawdCdVO> parseRowFromJson(String jsonResponse) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode rows = root.path("StanReginCd").get(1).path("row");

            if (rows.isMissingNode() || !rows.isArray()) {
                return Collections.emptyList();
            }

            return objectMapper.readValue(rows.toString(), new TypeReference<>() {});

        } catch (Exception e) {
            log.error("❌ JSON 파싱 실패", e);
            return Collections.emptyList();
        }
    }

    public void insertLawdCd(int pageNo, int numOfRows) {
        String url = "http://apis.data.go.kr/1741000/StanReginCd/getStanReginCdList";
        URI uri = UriComponentsBuilder
                .fromHttpUrl(url)
                .queryParam("pageNo", pageNo)
                .queryParam("numOfRows", numOfRows)
                .queryParam("type", "json")
                .queryParam("ServiceKey", apiKey)
                .build(true) //자동 인코딩 안함
                .toUri();
        try {
            RestTemplate restTemplate = new RestTemplate();
            // GET 요청 - String 타입으로 받은 후 직접 파싱
            ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);
            // JSON에서 row 배열만 추출해서 매핑
            List<LawdCdVO> lawdCdList = parseRowFromJson(response.getBody());

            // 결과 처리
            if (lawdCdList != null) {
                // bulk 저장 - 1000개 기준 약 2초, 단일 저장 반복 시 2분 소요
                mapper.insertLawdCdBatch(lawdCdList);
            }

        } catch (Exception e) {
            log.error("❌ 법정동코드 데이터 요청 실패", e);
        }
    }

}

@FunctionalInterface
interface SaveHandler<T> {
    void save(List<T> item);
}