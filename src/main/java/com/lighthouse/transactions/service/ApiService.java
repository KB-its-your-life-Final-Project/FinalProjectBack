package com.lighthouse.transactions.service;

import com.fasterxml.jackson.databind.JavaType;
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
import java.util.stream.Collectors;

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

    /**
     * 지정된 부동산 API 엔드포인트를 호출하여 데이터 조회
     * @param url API 엔드포인트 URL
     * @param lawdCd 시군구코드
     * @param dealYmd 거래 연월(yyyyMM)
     * @param itemType 응답 데이터 매핑 클래스
     * @return TransactionApiDTO<T> API 응답 DTO
     * @throws Exception API 호출 실패 또는 XML 파싱 실패 시
     */
    private <T> TransactionApiDTO<T> apiRequest(String url, int lawdCd, int dealYmd, Class<T> itemType) throws Exception {
        String urlStr = UriComponentsBuilder
                .fromHttpUrl(url)
                .queryParam("LAWD_CD", String.format("%05d", lawdCd))
                .queryParam("DEAL_YMD", dealYmd)
                .toUriString();
        urlStr += "&serviceKey=" + apiKey;
        urlStr += "&pageNo=" + "1";
        urlStr += "&numOfRows=" + "1000"; // 데이터가 1000개 넘어가는 것은 별로 없음 (최대 1000까지 확인: 광진구 11215 202305 - 단독/다가구 전월세)
        log.debug("API 요청 URL: {}", urlStr);
        XmlMapper xmlMapper = new XmlMapper();
        JavaType type = xmlMapper.getTypeFactory().constructParametricType(TransactionApiDTO.class, itemType);
        return xmlMapper.readValue(new URL(urlStr), type);
    }

    /**
     * API 데이터를 조회하여 트랜잭션 내에서 DB에 저장
     * @param endpoint API 엔드포인트
     * @param lawdCd 시군구코드
     * @param dealYmd 거래 연월(yyyyMM)
     * @param clazz VO 클래스 타입
     * @param handler 저장 로직 핸들러
     * @param logPrefix 로그용 데이터 유형명 (예: "아파트 매매", "아파트 전월세")
     * @throws Exception API 호출 실패 또는 DB 저장 실패 시
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
     * 재시도 로직을 포함하여 API 데이터를 공통 방식으로 DB에 삽입
     * @param endpoint API 엔드포인트
     * @param lawdCd 시군구코드
     * @param dealYmd 거래 연월(yyyyMM)
     * @param clazz VO 클래스 타입
     * @param handler 저장 로직 핸들러
     * @param logPrefix 로그용 데이터 유형명 (예: "아파트 매매", "아파트 전월세")
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
     * Estate API Integration 데이터 및 Sales 데이터 처리
     * - 중복 제거 후 신규 데이터만 삽입
     * - 매핑, 키 생성, 기존 데이터 조회, 중복 필터링, 배치 저장 수행
     * @param endpoint API 엔드포인트
     * @param lawdCd 시군구코드
     * @param dealYmd 거래 연월(yyyyMM)
     * @param clazz VO 클래스 타입
     * @param mapperFunc VO → EstateApiIntegration 변환 함수
     * @param salesMapperFunc VO → EstateApiIntegrationSales 변환 함수
     * @param logPrefix 로그용 데이터 유형명 (예: "아파트 매매", "아파트 전월세")
     * @throws Exception API 호출 실패 또는 DB 처리 실패 시
     */
    @Transactional
    public <T> void executeEstateApiIntegrationInsert(
            String endpoint, int lawdCd, int dealYmd, Class<T> clazz,
            BiFunction<T, AddressUtil, EstateApiIntegration> mapperFunc,
            Function<T, EstateApiIntegrationSales> salesMapperFunc,
            String logPrefix) throws Exception {
        final String url = BASE_URL + endpoint;

        // API 호출 시간 측정
        long apiStart = System.currentTimeMillis();
        TransactionApiDTO<T> response = apiRequest(url, lawdCd, dealYmd, clazz);
        long apiEnd = System.currentTimeMillis();
        long apiElapsedMs = apiEnd - apiStart;
        long apiMinutes = apiElapsedMs / 60000; // 1분 = 60000ms
        long apiSeconds = (apiElapsedMs % 60000) / 1000; // 남은 ms를 초로 변환
        log.info("⏱ {} API 호출 소요 시간: {}분 {}초 ({}ms)", logPrefix, apiMinutes, apiSeconds, apiElapsedMs);

        if (!"000".equals(response.getHeader().getResultCode())) {
            log.warn("❌ {} API 실패 - 코드: {}, 메시지: {}", logPrefix, response.getHeader().getResultCode(), response.getHeader().getResultMsg());
            throw new Exception("API 응답 오류: " + response.getHeader().getResultMsg());
        }
        List<T> voList = response.getBody().getItems();
        if (voList == null || voList.isEmpty()) {
            log.info("📋 {} 데이터 없음", logPrefix);
            return;
        }
        log.info("📊 {} API 응답 데이터: {} 건", logPrefix, voList.size());

        // API 데이터를 EstateApiIntegration 객체로 변환
        // stream 처리
        long conversionStart_stream = System.currentTimeMillis();
        List<EstateApiIntegration> apiIntegrationList_stream = voList.stream()
                .map(vo -> mapperFunc.apply(vo, addrUtils))
                .collect(Collectors.toList());
        long conversionEnd_stream = System.currentTimeMillis();
        long conversionElapsedMs_stream = conversionEnd_stream - conversionStart_stream;
        long conversionMinutes_stream = conversionElapsedMs_stream / 60000;
        long conversionSeconds_stream = (conversionElapsedMs_stream % 60000) / 1000;
        log.info("🔄 {} 데이터 변환 (stream 처리) 완료: {}분 {}초 ({}ms) ({} 건)", logPrefix, conversionMinutes_stream, conversionSeconds_stream, conversionElapsedMs_stream, apiIntegrationList_stream.size());

        // 모든 고유 키를 한 번에 수집
        long collectKeysStart = System.currentTimeMillis();
        Set<String> apiDataKeys = apiIntegrationList_stream.stream()
                .map(this::generateEstateIntegrationKey)
                .collect(Collectors.toSet());
        log.info("📝 {} API 데이터 고유 키 수집: {} 건", logPrefix, apiDataKeys.size());

        // IN 쿼리로 한 번에 기존 데이터 조회
        Set<String> existingKeys = new HashSet<>();
        if (!apiDataKeys.isEmpty()) {
            // 한 번의 쿼리로 모든 기존 데이터 조회
            List<EstateApiIntegration> existingList = mapper.findAllByKeys(new ArrayList<>(apiDataKeys));
            existingKeys = existingList.stream()
                    .map(this::generateEstateIntegrationKey)
                    .collect(Collectors.toSet());
        }
        long collectKeysEnd = System.currentTimeMillis();
        long collectKeysElapsedMs = collectKeysEnd - collectKeysStart;
        long collectKeysMinutes = collectKeysElapsedMs / 60000;
        long collectKeysSeconds = (collectKeysElapsedMs % 60000) / 1000;
        log.info("🔍 {} 기존 데이터 조회 완료: {}분 {}초 ({}ms) ({} 건)",
                logPrefix, collectKeysMinutes, collectKeysSeconds, collectKeysElapsedMs, existingKeys.size());

        // Stream으로 중복 제거 + 신규 데이터 내 중복도 제거
        long filterStart = System.currentTimeMillis();
        final Set<String> finalExistingKeys = existingKeys; // final로 만들어서 람다에서 사용
        List<EstateApiIntegration> newIntegrationList = apiIntegrationList_stream.stream()
                .collect(Collectors.toMap(
                        this::generateEstateIntegrationKey,  // 키 생성
                        Function.identity(),                 // 값은 그대로
                        (existing, duplicate) -> existing    // 중복시 기존 값 유지 (신규 데이터 내 중복 제거)
                ))
                .entrySet().stream()
                .filter(entry -> !finalExistingKeys.contains(entry.getKey())) // 기존 DB 데이터와 중복 제거
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
        long filterEnd = System.currentTimeMillis();
        long filterElapseMs = filterEnd - filterStart;
        long filterMinutes = filterElapseMs / 60000;
        long filterSeconds = (filterElapseMs % 60000) / 1000;
        log.info("🔍 {} 중복 제거 완료: {}분 {}초 ({}ms) (전체: {} 건, 기존(이미 있는 주소): {} 건, 신규: {} 건)",
                logPrefix, filterMinutes, filterSeconds, filterElapseMs, apiIntegrationList_stream.size(), existingKeys.size(), newIntegrationList.size());

        // estate_api_integration_tbl 삽입 (신규 데이터만)
        long insertIntegrationStart = System.currentTimeMillis();
        if (!newIntegrationList.isEmpty()) {
            int insertedRowNm = mapper.insertEstateApiIntegrationBatch(newIntegrationList);
            log.info("✅ {} integration_tbl 신규 데이터 저장: {} 건", logPrefix, insertedRowNm);
        } else {
            log.info("📋 {} integration_tbl 신규 데이터 없음 (모두 중복)", logPrefix);
        }

        // estate_api_integration_tbl 삽입 (받아온 모든 데이터)
        // long insertIntegrationStart = System.currentTimeMillis();
        // if (!apiIntegrationList_stream.isEmpty()) {
        //     int insertedRowNm = mapper.insertEstateApiIntegrationBatch(apiIntegrationList_stream);
        //     log.info("✅ {} integration_tbl 신규 데이터 저장: {} 건", logPrefix, insertedRowNm);
        // } else {
        //     log.info("📋 {} integration_tbl 신규 데이터 없음 (모두 중복)", logPrefix);
        // }

        long insertIntegrationEnd = System.currentTimeMillis();
        long insertIntegrationElapsedMs = insertIntegrationEnd - insertIntegrationStart;
        long insertIntegrationMinutes = insertIntegrationElapsedMs / 60000;
        long insertIntegrationSeconds = (insertIntegrationElapsedMs % 60000) / 1000;
        log.info("⏱ {} integration_tbl Insert 소요 시간: {}분 {}초 ({}ms)", logPrefix,
                insertIntegrationMinutes, insertIntegrationSeconds, insertIntegrationElapsedMs);

        // estate_api_integration_sales_tbl 삽입
        Set<EstateApiIntegrationSales> salesSet = new HashSet<>();

        long findIdTotalTime = 0; // findIdByUniqueCombination 전체 소요시간 누적 변수
        long setEstateIdAndAddTotalTime = 0; // setEstateIdAndAdd 전체 소요시간 누적 변수
        // estateId 추출
        long extractAndSetEstateIdStart = System.currentTimeMillis();
        for (T vo : voList) {
            EstateApiIntegration estate = mapperFunc.apply(vo, addrUtils);
            EstateApiIntegrationSales salesEstate = salesMapperFunc.apply(vo);
            long findIdStart = System.currentTimeMillis();
            int estateId = mapper.findIdByUniqueCombination(getEstateParams(estate));
            long findIdEnd = System.currentTimeMillis();
            findIdTotalTime += (findIdEnd - findIdStart);

            if (estateId <= 0) {
                log.warn("⚠️ {} estateId를 찾을 수 없음: {}", logPrefix, estate);
                continue;
            }
            long setEstateIdAndAddStart = System.currentTimeMillis();
            salesEstate.setEstateId(estateId);
            salesSet.add(salesEstate);
            long setEstateIdAndAddEnd = System.currentTimeMillis();
            setEstateIdAndAddTotalTime += (setEstateIdAndAddEnd - setEstateIdAndAddStart);
        }
        long extractAndSetEstateIdEnd = System.currentTimeMillis();


        long insertSalesStart = System.currentTimeMillis();
        if (!salesSet.isEmpty()) {
            int insertedRowNm = mapper.insertEstateApiIntegrationSalesBatch(new ArrayList<>(salesSet));
            log.info("✅ {} integration_sales_tbl 데이터 저장: {} 건", logPrefix, insertedRowNm);
        } else {
            log.info("📋 {} integration_sales_tbl 신규 데이터 없음 (모두 중복)", logPrefix);
        }
        long insertSalesEnd = System.currentTimeMillis();

        long insertSalesElapsedMs = insertSalesEnd - insertSalesStart;
        long insertSalesMinutes = insertSalesElapsedMs / 60000;
        long insertSalesSeconds = (insertSalesElapsedMs % 60000) / 1000;
        long findIdMinutes = findIdTotalTime / 60000;
        long findIdSeconds = (findIdTotalTime % 60000) / 1000;
        long setEstateIdAndAddMinutes = setEstateIdAndAddTotalTime / 60000;
        long setEstateIdAndAddSeconds = (setEstateIdAndAddTotalTime % 60000) / 1000;
        long extractAndSetEstateIdElapsedMs = extractAndSetEstateIdEnd - extractAndSetEstateIdStart;
        long extractAndSetEstateIdMinutes = extractAndSetEstateIdElapsedMs / 60000;
        long extractAndSetEstateIdSeconds = (extractAndSetEstateIdElapsedMs % 60000) / 1000;
        log.info("⏱ {} findIdByUniqueCombination 총 소요 시간: {}분 {}초 ({}ms)", logPrefix, findIdMinutes, findIdSeconds, findIdTotalTime);
        log.info("⏱ {} setEstateIdAndAdd 총 소요 시간: {}분 {}초 ({}ms)", logPrefix, setEstateIdAndAddMinutes, setEstateIdAndAddSeconds, setEstateIdAndAddTotalTime);
        log.debug("⏱ {} estateId 추출 소요 시간: {}분 {}초 ({}ms)", logPrefix, extractAndSetEstateIdMinutes, extractAndSetEstateIdSeconds, extractAndSetEstateIdElapsedMs);
        log.info("⏱ {} integration_sales_tbl Insert 소요 시간: {}분 {}초 ({}ms)", logPrefix, insertSalesMinutes, insertSalesSeconds, insertSalesElapsedMs);
    }

    /**
     * EstateApiIntegration 중복 검사용 고유 키 생성
     * @param estate EstateApiIntegration 객체
     * @return 고유 키 문자열
     */
    private String generateEstateIntegrationKey(EstateApiIntegration estate) {
        return String.format("%s_%s_%s_%s_%s",
                estate.getMhouseType(),
                estate.getShouseType(),
                estate.getBuildYear(),
                estate.getBuildingType(),
                estate.getJibunAddr());
    }

    /**
     * 재시도 로직을 포함한 Estate API Integration 처리
     * @param endpoint API 엔드포인트
     * @param lawdCd 시군구코드
     * @param dealYmd 거래 연월(yyyyMM)
     * @param clazz VO 클래스 타입
     * @param mapperFunc VO → EstateApiIntegration 변환 함수
     * @param salesMapperFunc VO → EstateApiIntegrationSales 변환 함수
     * @param logPrefix 로그용 데이터 유형명 (예: "아파트 매매", "아파트 전월세")
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

    // Estate API Integration/Sales 삽입 메소드들
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

    /**
     * 법정동코드 데이터 API 호출 및 DB 저장
     * @param pageNo 페이지 번호
     * @param numOfRows 페이지당 데이터 개수
     */
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
     * 지정된 기간 동안 모든 시군구에 대해
     * Estate API Integration 및 Sales 데이터 일괄 삽입
     * - API별, 월별, 시군구별 반복 호출
     * - 실패 건은 기록 후 계속 진행
     * @param uniqueLawdCdList 시군구코드 List
     * @param startYmd 시작 연월 (예: 202401)
     * @param endYmd 종료 연월 (예: 202412)
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
        log.info("   - 대상 시군구: {} 개, 기간: {} ~ {}", uniqueLawdCdList.size(), startYmd, endYmd);
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