package com.lighthouse.buildingRegister.service;

import com.lighthouse.buildingRegister.dto.BuildingResponseDTO;
import com.lighthouse.buildingRegister.mapper.BuildingRegisterMapper;
import com.lighthouse.buildingRegister.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BuildingRegisterPersistence {
    private final BuildingRegisterMapper mapper;

    @Transactional
    public void insertBuildingRegister(BuildingResponseDTO buildingResponseDTO){
        BuildingRegisterVO vo = buildingResponseDTO.getBuildingRegisterVO();
        mapper.insertBuildingRegister(vo);
        Long id = vo.getId();
        List<ResDetailVO> resDetailVOList = buildingResponseDTO.getResDetailList();
        if(resDetailVOList != null) {
            for (ResDetailVO resDetailVO : resDetailVOList) {
                resDetailVO.setRegisterId(id);
                mapper.insertBuildingRegisterDetail(resDetailVO);
            }
        }
        List<ResBuildingStatusVO> resBuildingStatusList = buildingResponseDTO.getResBuildingStatusList();
        if(resBuildingStatusList != null) {
            for (ResBuildingStatusVO resBuildingStatusVO : resBuildingStatusList) {
                resBuildingStatusVO.setRegisterId(id);
                mapper.insertBuildingRegisterStatus(resBuildingStatusVO);
            }
        }
        List<ResLicenseClassVO> resLicenseClassList = buildingResponseDTO.getResLicenseClassList();
        if(resLicenseClassList != null) {
            for (ResLicenseClassVO resLicenseClassVO : resLicenseClassList) {
                resLicenseClassVO.setRegisterId(id);
                mapper.insertBuildingRegisterLicense(resLicenseClassVO);
            }
        }
        List<ResParkingLotStatusVO> resParkingLotStatusList = buildingResponseDTO.getResParkingLotStatusList();
        if(resParkingLotStatusList != null) {
            for (ResParkingLotStatusVO resParkingLotStatusVO : resParkingLotStatusList) {
                resParkingLotStatusVO.setRegisterId(id);
                mapper.insertBuildingRegisterParkingLot(resParkingLotStatusVO);
            }
        }
        List<ResAuthStatusVO> resAuthStatusList = buildingResponseDTO.getResAuthStatusList();
        if(resAuthStatusList != null) {
            for (ResAuthStatusVO resAuthStatusVO : resAuthStatusList) {
                resAuthStatusVO.setRegisterId(id);
                mapper.insertBuildingRegisterAuth(resAuthStatusVO);
            }
        }
        List<ResChangeVO> resChangeList = buildingResponseDTO.getResChangeList();
        if(resChangeList != null) {
            for (ResChangeVO resChangeVO : resChangeList) {
                resChangeVO.setRegisterId(id);
                mapper.insertBuildingRegisterChange(resChangeVO);
            }
        }
        List<ResOwnerVO> resOwnerList = buildingResponseDTO.getResOwnerList();
        if(resOwnerList != null) {
            for (ResOwnerVO resOwnerVO : resOwnerList) {
                resOwnerVO.setRegisterId(id);
                mapper.insertBuildingRegisterOwner(resOwnerVO);
            }
        }
    }
}
