package com.lighthouse.buildingRegister.mapper;

import com.lighthouse.buildingRegister.vo.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BuildingRegisterMapper {
    void insertBuildingRegister(BuildingRegisterVO vo);
    void insertBuildingRegisterDetail(ResDetailVO vo);
    void insertBuildingRegisterAuth(ResAuthStatusVO vo);
    void insertBuildingRegisterChange(ResChangeVO vo);
    void insertBuildingRegisterLicense(ResLicenseClassVO vo);
    void insertBuildingRegisterOwner(ResOwnerVO vo);
    void insertBuildingRegisterParkingLot(ResParkingLotStatusVO vo);
    void insertBuildingRegisterStatus(ResBuildingStatusVO vo);

    // 위도/경도로 건물 조회 - 단건 조회
    BuildingRegisterVO getBuildingRegisterByLocation(@Param("lat") double lat, @Param("lng") double lng);
    
    // 위도/경도로 건물 정보와 상세 정보 조회 (건물 정보 + 건물 용도 + 건물 층수) - 다건 조회회
    List<BuildingRegisterWithStatusVO> getBuildingRegisterWithStatusByLocation(@Param("lat") double lat, @Param("lng") double lng);
}
