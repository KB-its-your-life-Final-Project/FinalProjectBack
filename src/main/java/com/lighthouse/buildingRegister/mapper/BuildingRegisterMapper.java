package com.lighthouse.buildingRegister.mapper;

import com.lighthouse.buildingRegister.vo.*;

public interface BuildingRegisterMapper {
    void insertBuildingRegister(BuildingRegisterVO vo);
    void insertBuildingRegisterDetail(ResDetailVO vo);
    void insertBuildingRegisterAuth(ResAuthStatusVO vo);
    void insertBuildingRegisterChange(ResChangeVO vo);
    void insertBuildingRegisterLicense(ResLicenseClassVO vo);
    void insertBuildingRegisterOwner(ResOwnerVO vo);
    void insertBuildingRegisterParkingLot(ResParkingLotStatusVO vo);
    void insertBuildingRegisterStatus(ResBuildingStatusVO vo);
}
