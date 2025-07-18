package com.lighthouse.buildingRegister.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
public class ResChangeVO {
    @Setter
    private Long registerId;
    private String resChangeDate;     // 변경일자
    @Setter
    private String resChangeReason;   // 변경사유
    @Setter
    private String resNote;           // 비고
    private String resIssueDate;      // 발급일자
    @Setter
    private String resIssueOgzNm;     // 발급기관
    @Setter
    private String resOriGinalData;   // PDF 파일 Base64값

    public void setResChangeDate(String resChangeDate) {
        this.resChangeDate = (resChangeDate == null || resChangeDate.trim().isEmpty()) ? null : resChangeDate;
    }

    public void setResIssueDate(String resIssueDate) {
        this.resIssueDate = (resIssueDate == null || resIssueDate.trim().isEmpty()) ? null : resIssueDate;
    }

}