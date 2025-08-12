package com.lighthouse.transactions.mapper;

import com.lighthouse.transactions.entity.EstateApiIntegration;
import com.lighthouse.transactions.entity.EstateApiIntegrationSales;
import com.lighthouse.transactions.vo.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface TransactionMapper {
    // 단일 insert
    void insertApartmentTrade(ApartmentTradeVO item);

    void insertApartmentRental(ApartmentRentalVO item);

    void insertMultiHouseTrade(MultiHouseTradeVO item);

    void insertMultiHouseRental(MultiHouseRentalVO item);

    void insertOfficetelTrade(OfficetelTradeVO item);

    void insertOfficetelRental(OfficetelRentalVO item);

    void insertSingleHouseTrade(SingleHouseTradeVO item);

    void insertSingleHouseRental(SingleHouseRentalVO item);

    void insertLawdCd(LawdCdVO item);

    // bulk insert
    void insertApartmentTradeBatch(@Param("list") List<ApartmentTradeVO> apartmentTradeList);

    void insertApartmentRentalBatch(@Param("list") List<ApartmentRentalVO> apartmentRentalList);

    void insertMultiHouseTradeBatch(@Param("list") List<MultiHouseTradeVO> multiHouseTradeList);

    void insertMultiHouseRentalBatch(@Param("list") List<MultiHouseRentalVO> multiHouseRentalList);

    void insertOfficetelTradeBatch(@Param("list") List<OfficetelTradeVO> officetelTradeList);

    void insertOfficetelRentalBatch(@Param("list") List<OfficetelRentalVO> officetelRentalList);

    void insertSingleHouseTradeBatch(@Param("list") List<SingleHouseTradeVO> singleHouseTradeList);

    void insertSingleHouseRentalBatch(@Param("list") List<SingleHouseRentalVO> singleHouseRentalList);

    void insertLawdCdBatch(@Param("list") List<LawdCdVO> lawdCdList);

    // estate_api_integration_tbl 및 estate_api_integration_sales_tbl
    List<EstateApiIntegration> findAllByKeys(@Param("keys") List<String> keys);

    List<EstateApiIntegration> findAllByUniqueCombination(Map<String, Object> params);

    Integer findIdByUniqueCombination(Map<String, Object> params);

    Integer insertEstateApiIntegrationBatch(@Param("list") List<EstateApiIntegration> integrationList);

    Integer insertEstateApiIntegrationSalesBatch(@Param("list") List<EstateApiIntegrationSales> integrationSalesList);
}