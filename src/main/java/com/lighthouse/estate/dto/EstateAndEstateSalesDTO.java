package com.lighthouse.estate.dto;

import lombok.Data;

@Data
public class EstateAndEstateSalesDTO {

    //EstateDTO
    private Integer sggCd;
    private String sggNm;
    private String umdNm;
    private String jibun;
    private String buildingName;
    private String mhouseType;
    private String shouseType;
    private Integer buildYear;
    private Integer buildingType;
    private Integer sourceTable;
    private Integer originalId;
    private String jibunAddr;
    private Double latitude;
    private Double longitude;

    //EstateSalesDTO
    private Integer estateId;
    private Integer dealYear;
    private Integer dealMonth;
    private Integer dealDay;
    private Integer dealAmount;
    private Integer deposit;
    private Integer monthlyRent;
    private Integer tradeType;
}
