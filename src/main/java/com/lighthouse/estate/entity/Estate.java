package com.lighthouse.estate.entity;

import lombok.Data;

@Data
public class Estate {
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