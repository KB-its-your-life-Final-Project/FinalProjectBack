package com.lighthouse.homeregister.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class HomeRegister {
    private Integer id;
    private Integer memberId;
    private Integer estateId;
    private String umdNm;
    private Integer sggCd;
    private String buildingName;
    private String buildingNumber;
    private String jibun;
    private LocalDateTime regDate;
    private String regIp;
    private Integer isDelete;
    private LocalDate contractStart;
    private LocalDate contractEnd;
    private Integer rentType;
    private Integer jeonseAmount;
    private Integer monthlyDeposit;
    private Integer monthlyRent;
}
