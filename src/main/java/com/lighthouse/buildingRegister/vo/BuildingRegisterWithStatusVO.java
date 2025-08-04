package com.lighthouse.buildingRegister.vo;

import lombok.Data;

@Data
public class BuildingRegisterWithStatusVO {
    // BuildingRegisterVO의 필드들
    private Integer id;
    private String type;
    private String resAddrDong;
    private String resNumber;
    private String resUserAddr;
    private String commAddrLotNumber;
    private String commAddrRoadName;
    private String resNote;
    private String resViolationStatus;
    private String reqDong;
    private String reqHo;
    private Double latitude;
    private Double longitude;
    private String jibunAddr;               // 지번 주소
    
    // ResBuildingStatusVO의 필드들
    private Integer registerId;
    private String resType;
    private String resFloor;
    private String resStructure;
    private String resUseType;
    private String resArea;
} 