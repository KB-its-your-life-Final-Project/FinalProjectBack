package com.lighthouse.estate.dto;


import lombok.Data;

/**
 * 부동산 건물 정보 DTO
 * estate_api_integration_tbl의 데이터를 담는 객체
 */
@Data
public class EstateDTO {
    
    private Integer id;
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
    
} 