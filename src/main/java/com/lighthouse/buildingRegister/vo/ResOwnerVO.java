package com.lighthouse.buildingRegister.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResOwnerVO {
    private Long registerId;
    private String resOwner;              // 소유자
    private String resIdentityNo;         // 주민번호
    private String resUserAddr;           // 주소
    private String resOwnershipStake;     // 소유권 지분
    private String resChangeDate;         // 변경일자
    private String resChangeReason;       // 변경사유
}
