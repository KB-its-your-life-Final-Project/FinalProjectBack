package com.lighthouse.transactions.mapper;

import com.lighthouse.transactions.vo.*;

public interface TransactionMapper {
    void insertApartmentTrade(ApartmentTradeVO item);
    void insertApartmentRental(ApartmentRentalVO item);
    void insertMultiHouseTrade(MultiHouseTradeVO item);
    void insertMultiHouseRental(MultiHouseRentalVO item);
    void insertOfficetelTrade(OfficetelTradeVO item);
    void insertOfficetelRental(OfficetelRentalVO item);
    void insertSingleHouseTrade(SingleHouseTradeVO item);
    void insertSingleHouseRental(SingleHouseRentalVO item);
}
