package com.lighthouse.estate.entity;

import lombok.Data;

@Data
public class EstateSales {

  private Integer id;
  private Integer estateId;
  private Integer dealYear;
  private Integer dealMonth;
  private Integer dealDay;
  private Integer dealAmount;
  private Integer deposit;
  private Integer monthlyRent;
  private Integer tradeType;

}
