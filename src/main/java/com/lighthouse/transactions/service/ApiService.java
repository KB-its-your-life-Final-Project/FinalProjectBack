package com.lighthouse.transactions.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.lighthouse.transactions.dto.ApiNameCallDTO;
import com.lighthouse.transactions.dto.FailureLogDTO;
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
        log.debug("API 요청 URL: {}", urlStr);
        XmlMapper xmlMapper = new XmlMapper();
        JavaType type = xmlMapper.getTypeFactory().constructParametricType(TransactionApiDTO.class, itemType);
        return xmlMapper.readValue(new URL(urlStr), type);
    }

    /**
     * 트랜잭션 내에서 실행될 실제 데이터 처리 로직
     */
    @Transactional
    public <T> void executeTransactionalInsert(String endpoint, int lawdCd, int dealYmd, Class<T> clazz, SaveHandler<T> handler, String logPrefix) throws Exception {
        final String url = BASE_URL + endpoint;

        TransactionApiDTO<T> response = apiRequest(url, lawdCd, dealYmd, clazz);
        if (!"000".equals(response.getHeader().getResultCode())) {
            log.warn("❌ {} API 실패 - 코드: {}, 메시지: {}", logPrefix, response.getHeader().getResultCode(), response.getHeader().getResultMsg());
            throw new Exception("API 응답 오류: " + response.getHeader().getResultMsg());
        }

        handler.save(response.getBody().getItems());
        log.debug("{} 데이터 저장 완료: {} 건", logPrefix, response.getBody().getItems().size());
    }

    /**
     * 재시도 로직이 포함된 공통 insert 메소드
     */
    public <T> void insertCommon(String endpoint, int lawdCd, int dealYmd, Class<T> clazz, SaveHandler<T> handler, String logPrefix) {
        int maxRetries = 3;
        int attempts = 0;
        long baseDelayMs = 1000; // 기본 대기 시간 1초

        while (attempts < maxRetries) {
            attempts++;
            try {
                // 트랜잭션이 적용된 메소드 호출
                executeTransactionalInsert(endpoint, lawdCd, dealYmd, clazz, handler, logPrefix);
                log.info("✅ {} 성공 (시도 {}/{})", logPrefix, attempts, maxRetries);
                return;
            } catch (Exception e) {
                log.warn("⚠️ {} 실패 (시도 {}/{}): {}", logPrefix, attempts, maxRetries, e.getMessage());
                if (attempts >= maxRetries) {
                    log.error("❌ {} 최종 실패 - 모든 재시도 완료", logPrefix);
                    throw new RuntimeException(String.format("%s 처리 실패 (최대 재시도 횟수 초과)", logPrefix), e);
                }
                // 지수 백오프 적용
                long delayMs = baseDelayMs * (long) Math.pow(2, attempts - 1);
                try {
                    log.info("⏳ {} 재시도 대기 중... ({}ms)", logPrefix, delayMs);
                    Thread.sleep(delayMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.error("❌ {} 재시도 대기 중 인터럽트 발생", logPrefix);
                    throw new RuntimeException("처리 중단됨", ie);
                }
            }
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

    /**
     * 트랜잭션이 적용된 Estate API Integration 데이터 처리
     */
    @Transactional
    public <T> void executeEstateApiIntegrationInsert(
            String endpoint, int lawdCd, int dealYmd, Class<T> clazz,
            BiFunction<T, AddressUtil, EstateApiIntegration> mapperFunc,
            Function<T, EstateApiIntegrationSales> salesMapperFunc,
            String logPrefix) throws Exception {
        final String url = BASE_URL + endpoint;
        TransactionApiDTO<T> response = apiRequest(url, lawdCd, dealYmd, clazz);
        if (!"000".equals(response.getHeader().getResultCode())) {
            log.warn("❌ {} API 실패 - 코드: {}, 메시지: {}", logPrefix, response.getHeader().getResultCode(), response.getHeader().getResultMsg());
            throw new Exception("API 응답 오류: " + response.getHeader().getResultMsg());
        }
        List<T> voList = response.getBody().getItems();
        if (voList == null || voList.isEmpty()) {
            log.info("📋 {} 데이터 없음", logPrefix);
            return;
        }

        // estate_api_integration_tbl 삽입
        Set<EstateApiIntegration> integrationSet = new HashSet<>();
        for (T vo : voList) {
            EstateApiIntegration estate = mapperFunc.apply(vo, addrUtils);
            integrationSet.add(estate);
        }
        if (!integrationSet.isEmpty()) {
            int insertedRowNm = mapper.insertEstateApiIntegrationBatch(new ArrayList<>(integrationSet));
            log.debug("✅ {} integration_tbl 데이터 저장: {} 건", logPrefix, insertedRowNm);
        }

        // estate_api_integration_sales_tbl 삽입
        Set<EstateApiIntegrationSales> salesSet = new HashSet<>();
        for (T vo : voList) {
            EstateApiIntegration estate = mapperFunc.apply(vo, addrUtils);
            EstateApiIntegrationSales salesEstate = salesMapperFunc.apply(vo);
            // estateId 추출
            int estateId = mapper.findIdByUniqueCombination(getEstateParams(estate));
            if (estateId <= 0) {
                log.warn("⚠️ {} estateId를 찾을 수 없음: {}", logPrefix, estate);
                continue;
            }
            salesEstate.setEstateId(estateId);
            salesSet.add(salesEstate);
        }

        if (!salesSet.isEmpty()) {
            int insertedRowNm = mapper.insertEstateApiIntegrationSalesBatch(new ArrayList<>(salesSet));
            log.debug("✅ {} integration_sales_tbl 데이터 저장: {} 건", logPrefix, insertedRowNm);
        }
    }

    /**
     * 재시도 로직이 포함된 Estate API Integration 메소드
     */
    private <T> void insertEstateApiIntegrationCommon(
            String endpoint, int lawdCd, int dealYmd, Class<T> clazz,
            BiFunction<T, AddressUtil, EstateApiIntegration> mapperFunc,
            Function<T, EstateApiIntegrationSales> salesMapperFunc,
            String logPrefix) {

        int maxRetries = 3;
        int attempts = 0;
        long baseDelayMs = 1000;

        while (attempts < maxRetries) {
            attempts++;
            try {
                // 트랜잭션이 적용된 메소드 호출
                executeEstateApiIntegrationInsert(endpoint, lawdCd, dealYmd, clazz, mapperFunc, salesMapperFunc, logPrefix);
                log.info("✅ {} Estate Integration & Sales 데이터 삽입 성공 (시도 {}/{})", logPrefix, attempts, maxRetries);
                return;
            } catch (Exception e) {
                log.warn("⚠️ {} Estate Integration & Sales 데이터 삽입 실패 (시도 {}/{}): {}", logPrefix, attempts, maxRetries, e.getMessage());
                if (attempts >= maxRetries) {
                    log.error("❌ {} Estate Integration 최종 실패", logPrefix);
                    throw new RuntimeException(String.format("%s Estate Integration 처리 실패", logPrefix), e);
                }
                // 지수 백오프 적용
                long delayMs = baseDelayMs * (long) Math.pow(2, attempts - 1);
                try {
                    log.info("⏳ {} Estate Integration 재시도 대기 중... ({}ms)", logPrefix, delayMs);
                    Thread.sleep(delayMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.error("❌ {} Estate Integration 재시도 대기 중 인터럽트 발생", logPrefix);
                    throw new RuntimeException("처리 중단됨", ie);
                }
            }
        }
    }

    // Estate API Integration 메소드들
    public void insertAptTradesToEstApiIntg(int lawdCd, int dealYmd) {
        insertEstateApiIntegrationCommon(APT_TRADE_ENDPOINT,
                lawdCd, dealYmd, ApartmentTradeVO.class,
                ApartmentTradeVO::toEstateApiIntegration,
                ApartmentTradeVO::toEstateApiIntegrationSales,
                "아파트 매매");
    }

    public void insertAptRentalsToEstApiIntg(int lawdCd, int dealYmd) {
        insertEstateApiIntegrationCommon(APT_RENT_ENDPOINT,
                lawdCd, dealYmd, ApartmentRentalVO.class,
                ApartmentRentalVO::toEstateApiIntegration,
                ApartmentRentalVO::toEstateApiIntegrationSales,
                "아파트 전월세");
    }

    public void insertOffTradesToEstApiIntg(int lawdCd, int dealYmd) {
        insertEstateApiIntegrationCommon(OFFICETEL_TRADE_ENDPOINT,
                lawdCd, dealYmd, OfficetelTradeVO.class,
                OfficetelTradeVO::toEstateApiIntegration,
                OfficetelTradeVO::toEstateApiIntegrationSales,
                "오피스텔 매매");
    }

    public void insertOffRentalsToEstApiIntg(int lawdCd, int dealYmd) {
        insertEstateApiIntegrationCommon(OFFICETEL_RENT_ENDPOINT,
                lawdCd, dealYmd, OfficetelRentalVO.class,
                OfficetelRentalVO::toEstateApiIntegration,
                OfficetelRentalVO::toEstateApiIntegrationSales,
                "오피스텔 전월세");
    }

    public void insertMHTradesToEstApiIntg(int lawdCd, int dealYmd) {
        insertEstateApiIntegrationCommon(MULTI_TRADE_ENDPOINT,
                lawdCd, dealYmd, MultiHouseTradeVO.class,
                MultiHouseTradeVO::toEstateApiIntegration,
                MultiHouseTradeVO::toEstateApiIntegrationSales,
                "연립다세대 매매");
    }

    public void insertMHRentalsToEstApiIntg(int lawdCd, int dealYmd) {
        insertEstateApiIntegrationCommon(MULTI_RENT_ENDPOINT,
                lawdCd, dealYmd, MultiHouseRentalVO.class,
                MultiHouseRentalVO::toEstateApiIntegration,
                MultiHouseRentalVO::toEstateApiIntegrationSales,
                "연립다세대 전월세");
    }

    public void insertSHTradesToEstApiIntg(int lawdCd, int dealYmd) {
        insertEstateApiIntegrationCommon(SINGLE_TRADE_ENDPOINT,
                lawdCd, dealYmd, SingleHouseTradeVO.class,
                SingleHouseTradeVO::toEstateApiIntegration,
                SingleHouseTradeVO::toEstateApiIntegrationSales,
                "단독/다가구 매매");
    }

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
     * Estate API Integration 및 Sales 데이터 일괄 삽입
     * - 각 API 호출별로 트랜잭션 적용
     * - 개별 실패 시 해당 건만 건너뛰고 전체 작업 계속
     * @param uniqueLawdCdList 시군구코드 List
     * @param startYmd 시작연월 (예: 202401)
     * @param endYmd 종료연월 (예: 202412)
     */
    public void insertEstateApiIntgAndSalesTbl(List<Integer> uniqueLawdCdList, int startYmd, int endYmd) {
        if (uniqueLawdCdList == null || uniqueLawdCdList.isEmpty()) {
            log.warn("⚠️ 처리할 시군구코드가 없습니다.");
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM");
        YearMonth start = YearMonth.parse(String.valueOf(startYmd), formatter);
        YearMonth end = YearMonth.parse(String.valueOf(endYmd), formatter);

        // 날짜 범위 검증
        if (start.isAfter(end)) {
            log.error("❌ 잘못된 날짜 범위: 시작월({})이 종료월({})보다 큽니다.", startYmd, endYmd);
            throw new IllegalArgumentException("시작월이 종료월보다 클 수 없습니다.");
        }

        // API 함수 목록 정의
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
        log.info("🚀 부동산 API 통합 데이터 삽입 시작");
        log.info("📊 대상 시군구: {} 개, 기간: {} ~ {}", uniqueLawdCdList.size(), startYmd, endYmd);

        int totalTasks = uniqueLawdCdList.size() * (int) start.until(end.plusMonths(1), java.time.temporal.ChronoUnit.MONTHS) * apiList.size();
        int completedTasks = 0;
        int failedTasks = 0;
        List<FailureLogDTO> failedTaskDetails = new ArrayList<>();

        // 각 시군구코드별로 처리
        for (Integer lawdCd : uniqueLawdCdList) {
            log.info("🏘️ 시군구코드: {} 처리 시작", lawdCd);
            // 각 연월별로 처리
            for (YearMonth current = start; !current.isAfter(end); current = current.plusMonths(1)) {
                int dealYmd = Integer.parseInt(current.format(formatter));
                // 각 API별로 처리
                for (ApiNameCallDTO api : apiList) {
                    try {
                        api.apiCall.accept(lawdCd, dealYmd);
                        completedTasks++;
                        if (completedTasks % 10 == 0) { // 10건마다 진행상황 로그
                            double progress = (double) completedTasks / totalTasks * 100;
                            log.info("📈 진행률: {}% ({}/{}) - 실패: {} 건",
                                    String.format("%.1f", progress), completedTasks, totalTasks, failedTasks);
                        }
                    } catch (Exception e) {
                        failedTasks++;
                        log.error("❌ {} 처리 실패 - 시군구코드: {}, 연월: {}", api.apiName, lawdCd, dealYmd, e);
                        failedTaskDetails.add(new FailureLogDTO(lawdCd, dealYmd, api.apiName));
                        // 개별 실패는 전체 작업을 중단하지 않음
                    }
                }
            }
            log.info("✅ 시군구코드: {} 처리 완료", lawdCd);
        }
        log.info("🎉 부동산 API 통합 데이터 삽입 완료");
        // 최종 요약 로그
        log.info("📊 최종 결과 요약");
        log.info("   - 전체 작업 건수: {}", totalTasks);
        log.info("   - 성공: {} 건", completedTasks);
        log.info("   - 실패: {} 건", failedTasks);
        log.info("   - 성공률: {}%", String.format("%.2f", (completedTasks * 100.0) / totalTasks));
        if (failedTasks > 0) {
            log.warn("⚠️ 총 {} 건의 실패가 발생했습니다.", failedTasks);
            log.warn("📌 실패한 항목 목록 (시군구코드, 연월, API명):");
            for (FailureLogDTO  failure : failedTaskDetails) {
                log.warn("   - {}, {}, {}", failure.getLawdCd(), failure.getDealYmd(), failure.getApiName());
            }
        } else {
            log.info("✅ 모든 작업이 성공적으로 완료되었습니다. 실패 없음.");
        }
    }
}

@FunctionalInterface
interface SaveHandler<T> {
    void save(List<T> item);
}