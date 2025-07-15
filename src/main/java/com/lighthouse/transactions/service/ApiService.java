package com.lighthouse.transactions.service;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.lighthouse.transactions.dto.ApiDTO;
import com.lighthouse.transactions.mapper.TransactionMapper;
import com.lighthouse.transactions.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URL;


@Service
@RequiredArgsConstructor
@Slf4j
public class ApiService {
    @Value("${data.go.kr.api.key}")
    private String apiKey;
    final private TransactionMapper mapper;
    final String BASE_URL = "https://apis.data.go.kr/1613000/";
    private <T> ApiDTO<T> apiRequest(String url, int lawdCd, int dealYmd, Class<T> itemType) throws Exception {
        String urlStr = UriComponentsBuilder
                .fromHttpUrl(url)
                .queryParam("LAWD_CD", String.format("%05d", lawdCd))
                .queryParam("DEAL_YMD", dealYmd)
                .toUriString();
        urlStr += "&serviceKey=" + apiKey;
        XmlMapper xmlMapper = new XmlMapper();
        JavaType type = xmlMapper.getTypeFactory().constructParametricType(ApiDTO.class, itemType);
        return xmlMapper.readValue(new URL(urlStr), type);
    }
    private <T> void insertCommon(String endpoint, int lawdCd, int dealYmd, Class<T> clazz, SaveHandler<T> handler, String logPrefix) {
        final String url = BASE_URL + endpoint;
        try {
            ApiDTO<T> response = apiRequest(url, lawdCd, dealYmd, clazz);
            if (!"000".equals(response.getHeader().getResultCode())) {
                log.warn("❌ {} API 실패 - 코드: {}, 메시지: {}", logPrefix, response.getHeader().getResultCode(), response.getHeader().getResultMsg());
                return;
            }
            for (T item : response.getBody().getItems()) {
                handler.save(item);
            }
        } catch (Exception e) {
            log.error("❌ {} 데이터 요청 실패", logPrefix, e);
        }
    }

    public void insertApartmentTrades(int lawdCd, int dealYmd) {
        insertCommon("/RTMSDataSvcAptTrade/getRTMSDataSvcAptTrade",
                lawdCd, dealYmd, ApartmentTradeVO.class, mapper::insertApartmentTrade, "아파트 매매");
    }

    public void insertApartmentRentals(int lawdCd, int dealYmd) {
        insertCommon("/RTMSDataSvcAptRent/getRTMSDataSvcAptRent",
                lawdCd, dealYmd, ApartmentRentalVO.class, mapper::insertApartmentRental, "아파트 전월세");
    }

    public void insertMultiHouseTrades(int lawdCd, int dealYmd) {
        insertCommon("/RTMSDataSvcRHTrade/getRTMSDataSvcRHTrade",
                lawdCd, dealYmd, MultiHouseTradeVO.class, mapper::insertMultiHouseTrade, "연립다세대 매매");
    }

    public void insertMultiHouseRentals(int lawdCd, int dealYmd) {
        insertCommon("/RTMSDataSvcRHRent/getRTMSDataSvcRHRent",
                lawdCd, dealYmd, MultiHouseRentalVO.class, mapper::insertMultiHouseRental, "연립다세대 전월세");
    }

    public void insertOfficetelTrade(int lawdCd, int dealYmd) {
        insertCommon("/RTMSDataSvcOffiTrade/getRTMSDataSvcOffiTrade",
                lawdCd, dealYmd, OfficetelTradeVO.class, mapper::insertOfficetelTrade, "오피스텔 매매");
    }

    public void insertOfficetelRental(int lawdCd, int dealYmd) {
        insertCommon("/RTMSDataSvcOffiRent/getRTMSDataSvcOffiRent",
                lawdCd, dealYmd, OfficetelRentalVO.class, mapper::insertOfficetelRental, "오피스텔 전월세");
    }

    public void insertSingleHouseTrade(int lawdCd, int dealYmd) {
        insertCommon("/RTMSDataSvcSHTrade/getRTMSDataSvcSHTrade",
                lawdCd, dealYmd, SingleHouseTradeVO.class, mapper::insertSingleHouseTrade, "단독/다가구 매매");
    }

    public void insertSingleHouseRental(int lawdCd, int dealYmd) {
        insertCommon("/RTMSDataSvcSHRent/getRTMSDataSvcSHRent",
                lawdCd, dealYmd, SingleHouseRentalVO.class, mapper::insertSingleHouseRental, "단독/다가구 전월세");
    }
}

@FunctionalInterface
interface SaveHandler<T> {
    void save(T item);
}