package com.lighthouse.buildingRegister.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BuildingRegisterVO {
    private Long id;
    private String resDocNo;                // 문서확인번호
    private String commUniqeNo;             // 고유번호
    private String resAddrDong;             // 동명
    private String resNumber;               // 호수 (매수)
    private String resUserAddr;             // 대지위치
    private String commAddrLotNumber;       // 지번
    private String commAddrRoadName;        // 도로명주소
    private String resNote;                 // 비고
    private String resIssueDate;            // 발급일자 (YYYYMMDD)
    private String resIssueOgzNm;           // 발급기관
    private String resOriGinalData;         // PDF파일 Base64 (선택 요청시)

    private String resViolationStatus;      //위반건축물일 경우 "위반건축물"라고 제공
}
