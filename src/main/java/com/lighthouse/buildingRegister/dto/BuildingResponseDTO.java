package com.lighthouse.buildingRegister.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.lighthouse.buildingRegister.vo.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BuildingResponseDTO {
    @JsonUnwrapped //VO 필드를 풀어서 json에 매핑하는 어노테이션 - 중복 제거 위함
    BuildingRegisterVO buildingRegisterVO;

    private List<ResDetailVO> resDetailList;
    private List<ResBuildingStatusVO> resBuildingStatusList;
    private List<ResLicenseClassVO> resLicenseClassList;
    private List<ResParkingLotStatusVO> resParkingLotStatusList;
    private List<ResAuthStatusVO> resAuthStatusList;
    private List<ResChangeVO> resChangeList;
    private List<ResOwnerVO> resOwnerList;
    // 추가 인증 요청시 응답
    private Boolean continue2Way;
    private String method;
    private Integer jobIndex;
    private Integer threadIndex;
    private String jti;
    private Long twoWayTimestamp;
    private ExtraInfo extraInfo;

    @Getter
    @Setter
    public static class ExtraInfo {
        private String reqPlainTexts;          // 전자서명 원문 (BASE_64)
        private String reqSignType;            // 전자서명 방식 (0: CMS, 1: PKCS1)
        private String reqSignAlg;             // 전자서명 알고리즘 (0, 1)
        private String reqCMSssn;              // CMS_ssn (optional)
        private String reqCMStime;             // CMS_time (optional)
        private String reqCMSwithoutContent;   // CMS_withoutContent (optional, 0 or 1)
        private String reqPKCS1IncludeR;       // PKCS1_IncludeR (optional, 0 or 1)
        private String reqSignedData;          // 전자서명 전체 데이터
        private String commDongNum;            // 동번호 (optional)
        private List<ReqDongNum> reqDongNumList;   // 동번호 리스트 (optional)
        private String reqSecureNo;            // 보안문자
        private String reqSecureNoRefresh;     // 보안문자 새로고침
        private List<ReqAddr> reqAddrList;      // 주소 리스트 (optional)
        private String reqAddress;             // 도로명 주소 (optional)
        private String commSimpleAuth;         // 간편인증

    }

    @Getter
    @Setter
    public static class ReqDongNum{
        private String commDongNum;
        private String reqDong;
        private String reqArea;
    }

    @Getter
    @Setter
    public static class ReqAddr{
        private String commAddrLotNumber;
        private String commAddrRoadName;
    }
}
