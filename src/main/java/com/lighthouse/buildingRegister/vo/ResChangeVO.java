package com.lighthouse.buildingRegister.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResChangeVO {
    private Long registerId;
    private String resChangeDate;     // 변경일자
    private String resChangeReason;   // 변경사유
    private String resNote;           // 비고
    private String resIssueDate;      // 발급일자
    private String resIssueOgzNm;     // 발급기관
    private String resOriGinalData;   // PDF 파일 Base64값
}