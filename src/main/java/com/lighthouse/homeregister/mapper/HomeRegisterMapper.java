package com.lighthouse.homeregister.mapper;

import com.lighthouse.homeregister.entity.HomeRegisterEntity;

public interface HomeRegisterMapper {
    HomeRegisterEntity selectSalesWithEstateInfo(String estateId);
    void addHome(HomeRegisterEntity homeEntity);
}
