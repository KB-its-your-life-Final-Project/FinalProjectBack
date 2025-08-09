package com.lighthouse.buildingRegister.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BuildingRegisterVO {
    private Long id;
    private String type;                    // (일반/집합) 건축물 표기
    //private String resDocNo;                // 문서확인번호
    //private String commUniqeNo;             // 고유번호
    private String resAddrDong;             // 동명
    private String resNumber;               // 호수 (매수)
    private String resUserAddr;             // 대지위치
    private String commAddrLotNumber;       // 지번
    private String commAddrRoadName;        // 도로명주소
    private String resNote;                 // 비고
    //private String resIssueDate;            // 발급일자 (YYYYMMDD)
    //private String resIssueOgzNm;           // 발급기관
    //private String resOriGinalData;         // PDF파일 Base64 (선택 요청시)

    private String resViolationStatus;      //위반건축물일 경우 "위반건축물"라고 제공

    // 위도/경도 정보
    private Double latitude;                // 위도
    private Double longitude;               // 경도

    // 여기부터는 집합 건축물에서만 해당
    private String reqDong;                 // 요청한 동
    private String reqHo;                   // N호/N가구/N세대
    
    // // 지번 주소 (res_user_addr + commAddrLotNumber 조합)
    // private String jibunAddr;               // 지번 주소
}

