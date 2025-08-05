package com.lighthouse.transactions.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.lighthouse.transactions.dto.ApiNameCallDTO;
import com.lighthouse.transactions.dto.TransactionApiDTO;
import com.lighthouse.transactions.entity.EstateApiIntegration;
import com.lighthouse.transactions.entity.EstateApiIntegrationSales;
import com.lighthouse.transactions.mapper.TransactionMapper;
import com.lighthouse.transactions.util.AddressUtil;
import com.lighthouse.transactions.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URL;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.lighthouse.transactions.util.ParseUtil.getEstateParams;
import static com.lighthouse.transactions.util.ParseUtil.parseRowFromJson;


@Service
@RequiredArgsConstructor
@Slf4j
public class ApiService {
    @Value("${DATA_GO_KR_API_KEY}")
    private String apiKey;
    private final TransactionMapper mapper;
    private final AddressUtil addrUtils;
    private final String BASE_URL = "https://apis.data.go.kr/1613000/";
    // API Endpoint Constants
    private static final String APT_TRADE_ENDPOINT = "/RTMSDataSvcAptTrade/getRTMSDataSvcAptTrade";
    private static final String APT_RENT_ENDPOINT = "/RTMSDataSvcAptRent/getRTMSDataSvcAptRent";
    private static final String MULTI_TRADE_ENDPOINT = "/RTMSDataSvcRHTrade/getRTMSDataSvcRHTrade";
    private static final String MULTI_RENT_ENDPOINT = "/RTMSDataSvcRHRent/getRTMSDataSvcRHRent";
    private static final String OFFICETEL_TRADE_ENDPOINT = "/RTMSDataSvcOffiTrade/getRTMSDataSvcOffiTrade";
    private static final String OFFICETEL_RENT_ENDPOINT = "/RTMSDataSvcOffiRent/getRTMSDataSvcOffiRent";
    private static final String SINGLE_TRADE_ENDPOINT = "/RTMSDataSvcSHTrade/getRTMSDataSvcSHTrade";
    private static final String SINGLE_RENT_ENDPOINT = "/RTMSDataSvcSHRent/getRTMSDataSvcSHRent";

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
            handler.save(response.getBody().getItems()); // = mapper.insertApartmentTradeBatch(itemList);
            log.info("{} 데이터 저장: {}", logPrefix, response.getBody().getItems());
        } catch (Exception e) {
            log.error("❌ {} 데이터 요청 실패", logPrefix, e);
        }
    }

    public void insertApartmentTrades(int lawdCd, int dealYmd) {
        insertCommon(APT_TRADE_ENDPOINT,
                lawdCd, dealYmd, ApartmentTradeVO.class, mapper::insertApartmentTradeBatch, "아파트 매매");
    }

    public void insertApartmentRentals(int lawdCd, int dealYmd) {
        insertCommon(APT_RENT_ENDPOINT,
                lawdCd, dealYmd, ApartmentRentalVO.class, mapper::insertApartmentRentalBatch, "아파트 전월세");
    }

    public void insertOfficetelTrade(int lawdCd, int dealYmd) {
        insertCommon(OFFICETEL_TRADE_ENDPOINT,
                lawdCd, dealYmd, OfficetelTradeVO.class, mapper::insertOfficetelTradeBatch, "오피스텔 매매");
    }

    public void insertOfficetelRental(int lawdCd, int dealYmd) {
        insertCommon(OFFICETEL_RENT_ENDPOINT,
                lawdCd, dealYmd, OfficetelRentalVO.class, mapper::insertOfficetelRentalBatch, "오피스텔 전월세");
    }

    public void insertMultiHouseTrades(int lawdCd, int dealYmd) {
        insertCommon(MULTI_TRADE_ENDPOINT,
                lawdCd, dealYmd, MultiHouseTradeVO.class, mapper::insertMultiHouseTradeBatch, "연립다세대 매매");
    }

    public void insertMultiHouseRentals(int lawdCd, int dealYmd) {
        insertCommon(MULTI_RENT_ENDPOINT,
                lawdCd, dealYmd, MultiHouseRentalVO.class, mapper::insertMultiHouseRentalBatch, "연립다세대 전월세");
    }

    public void insertSingleHouseTrade(int lawdCd, int dealYmd) {
        insertCommon(SINGLE_TRADE_ENDPOINT,
                lawdCd, dealYmd, SingleHouseTradeVO.class, mapper::insertSingleHouseTradeBatch, "단독/다가구 매매");
    }

    public void insertSingleHouseRental(int lawdCd, int dealYmd) {
        insertCommon(SINGLE_RENT_ENDPOINT,
                lawdCd, dealYmd, SingleHouseRentalVO.class, mapper::insertSingleHouseRentalBatch, "단독/다가구 전월세");
    }

    // estate_api_integration_tbl, estate_api_integration_sales_tbl
    private <T> void insertEstateApiIntegrationCommon(
            String endpoint, int lawdCd, int dealYmd, Class<T> clazz,
            BiFunction<T, AddressUtil, EstateApiIntegration> mapperFunc,
            Function<T, EstateApiIntegrationSales> salesMapperFunc,
            String logPrefix) {
        insertCommon(endpoint, lawdCd, dealYmd, clazz, voList -> {
            Set<EstateApiIntegration> integrationSet = new HashSet<>();
            Set<EstateApiIntegrationSales> salesSet = new HashSet<>();
            // estate_api_integration_tbl 삽입
            for (T vo : voList) {
                EstateApiIntegration estate = mapperFunc.apply(vo, addrUtils);
                integrationSet.add(estate);
            }
            mapper.insertEstateApiIntegrationBatch(new ArrayList<>(integrationSet));
            // estate_api_integration_sales_tbl 삽입
            for (T vo : voList) {
                EstateApiIntegration estate = mapperFunc.apply(vo, addrUtils);
                EstateApiIntegrationSales salesEstate = salesMapperFunc.apply(vo);
                // estateId 추출
                int estateId = mapper.findIdByUniqueCombination(getEstateParams(estate));
                salesEstate.setEstateId(estateId);
                salesSet.add(salesEstate);
            }
            mapper.insertEstateApiIntegrationSalesBatch(new ArrayList<>(salesSet));
        }, logPrefix);
    }

    @Transactional
    public void insertAptTradesToEstApiIntg(int lawdCd, int dealYmd) {
        insertEstateApiIntegrationCommon(APT_TRADE_ENDPOINT,
                lawdCd, dealYmd, ApartmentTradeVO.class,
                ApartmentTradeVO::toEstateApiIntegration,
                ApartmentTradeVO::toEstateApiIntegrationSales,
                "아파트 매매");
    }

    @Transactional
    public void insertAptRentalsToEstApiIntg(int lawdCd, int dealYmd) {
        insertEstateApiIntegrationCommon(APT_RENT_ENDPOINT,
                lawdCd, dealYmd, ApartmentRentalVO.class,
                ApartmentRentalVO::toEstateApiIntegration,
                ApartmentRentalVO::toEstateApiIntegrationSales,
                "아파트 전월세");
    }

    @Transactional
    public void insertOffTradesToEstApiIntg(int lawdCd, int dealYmd) {
        insertEstateApiIntegrationCommon(OFFICETEL_TRADE_ENDPOINT,
                lawdCd, dealYmd, OfficetelTradeVO.class,
                OfficetelTradeVO::toEstateApiIntegration,
                OfficetelTradeVO::toEstateApiIntegrationSales,
                "오피스텔 매매");
    }

    @Transactional
    public void insertOffRentalsToEstApiIntg(int lawdCd, int dealYmd) {
        insertEstateApiIntegrationCommon(OFFICETEL_RENT_ENDPOINT,
                lawdCd, dealYmd, OfficetelRentalVO.class,
                OfficetelRentalVO::toEstateApiIntegration,
                OfficetelRentalVO::toEstateApiIntegrationSales,
                "오피스텔 전월세");
    }

    @Transactional
    public void insertMHTradesToEstApiIntg(int lawdCd, int dealYmd) {
        insertEstateApiIntegrationCommon(MULTI_TRADE_ENDPOINT,
                lawdCd, dealYmd, MultiHouseTradeVO.class,
                MultiHouseTradeVO::toEstateApiIntegration,
                MultiHouseTradeVO::toEstateApiIntegrationSales,
                "연립다세대 매매");
    }

    @Transactional
    public void insertMHRentalsToEstApiIntg(int lawdCd, int dealYmd) {
        insertEstateApiIntegrationCommon(MULTI_RENT_ENDPOINT,
                lawdCd, dealYmd, MultiHouseRentalVO.class,
                MultiHouseRentalVO::toEstateApiIntegration,
                MultiHouseRentalVO::toEstateApiIntegrationSales,
                "연립다세대 전월세");
    }

    @Transactional
    public void insertSHTradesToEstApiIntg(int lawdCd, int dealYmd) {
        insertEstateApiIntegrationCommon(SINGLE_TRADE_ENDPOINT,
                lawdCd, dealYmd, SingleHouseTradeVO.class,
                SingleHouseTradeVO::toEstateApiIntegration,
                SingleHouseTradeVO::toEstateApiIntegrationSales,
                "단독/다가구 매매");
    }

    @Transactional
    public void insertSHRentalsToEstApiIntg(int lawdCd, int dealYmd) {
        insertEstateApiIntegrationCommon(SINGLE_RENT_ENDPOINT,
                lawdCd, dealYmd, SingleHouseRentalVO.class,
                SingleHouseRentalVO::toEstateApiIntegration,
                SingleHouseRentalVO::toEstateApiIntegrationSales,
                "단독/다가구 전월세");
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

    /**
     * estate_api_integration_tbl 및 estate_api_integration_sales_tbl에 데이터 삽입
     * @param uniqueLawdCdList 시군구코드 List
     * @param startYmd 시작연월 (예: 202401)
     * @param endYmd 종료연월 (예: 202412)
     */
    public void insertEstateApiIntgAndSalesTbl(List<Integer> uniqueLawdCdList, int startYmd, int endYmd) throws InterruptedException {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM");
        YearMonth start = YearMonth.parse(String.valueOf(startYmd), formatter);
        YearMonth end = YearMonth.parse(String.valueOf(endYmd), formatter);

        // API 함수 목록 정의 (service는 EstateApiService 등으로 가정)
        List<ApiNameCallDTO> apiList = List.of(
                new ApiNameCallDTO("아파트 매매", this::insertAptTradesToEstApiIntg),
                new ApiNameCallDTO("아파트 전월세", this::insertAptRentalsToEstApiIntg),
                new ApiNameCallDTO("오피스텔 매매", this::insertOffTradesToEstApiIntg),
                new ApiNameCallDTO("오피스텔 전월세", this::insertOffRentalsToEstApiIntg),
                new ApiNameCallDTO("연립다세대 매매", this::insertMHTradesToEstApiIntg),
                new ApiNameCallDTO("연립다세대 전월세", this::insertMHRentalsToEstApiIntg),
                new ApiNameCallDTO("단독/다가구 매매", this::insertSHTradesToEstApiIntg),
                new ApiNameCallDTO("단독/다가구 전월세", this::insertSHRentalsToEstApiIntg)
        );

        log.info("=== 부동산 API 통합 데이터 삽입 시작 ===");
        log.info("대상 시군구 수: {}, 기간: {} ~ {}", uniqueLawdCdList.size(), startYmd, endYmd);

        int totalLawdCd = uniqueLawdCdList.size();
        int processedLawdCd = 0;

        // 각 시군구 코드별로 처리
        for (Integer lawdCd : uniqueLawdCdList) {
            processedLawdCd++;
            log.info("진행률: {}/{} - 법정동코드: {} 처리 시작", processedLawdCd, totalLawdCd, lawdCd);

            // 각 연월별로 처리
            for (YearMonth current = start; !current.isAfter(end); current = current.plusMonths(1)) {
                int dealYmd = Integer.parseInt(current.format(formatter));
                log.info("시군구코드: {}, 연월: {}", lawdCd, dealYmd);

                // 각 API별로 처리
                for (ApiNameCallDTO api : apiList) {
                    try {
                        log.debug("{} 호출 - 시군구코드: {}, 연월: {}", api.apiName, lawdCd, dealYmd);
                        api.apiCall.accept(lawdCd, dealYmd);
                        log.debug("{} 완료 - 시군구코드: {}, 연월: {}", api.apiName, lawdCd, dealYmd);
                    } catch (Exception e) {
                        log.error("{} 실패 - 시군구코드: {}, 연월: {}, 에러: {}",
                                api.apiName, lawdCd, dealYmd, e.getMessage(), e);
                        // 실패한 경우에도 계속 진행 (필요에 따라 중단하도록 변경 가능)
                         throw e; // 중단
                    }
                }
                log.info("시군구코드: {} - 연월: {} 완료", lawdCd, dealYmd);
            }
            log.info("시군구코드: {} 전체 처리 완료 ({}/{})", lawdCd, processedLawdCd, totalLawdCd);
        }
        log.info("=== 부동산 API 통합 데이터 삽입 완료 ===");
    }
}

@FunctionalInterface
interface SaveHandler<T> {
    void save(List<T> item);
}