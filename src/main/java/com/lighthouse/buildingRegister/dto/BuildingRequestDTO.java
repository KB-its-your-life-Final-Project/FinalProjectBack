package com.lighthouse.buildingRegister.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BuildingRequestDTO {
    @Builder.Default
    private String organization = "0008";   // 세움터
    @Builder.Default
    private String loginType = "1";         // 일반 로그인
    private String userId;                  // 세움터 ID
    private String userPassword;            // 세움터 암호화된 비밀번호
    private String address;                 // 검색할 주소 (정확한 도로명 주소여야함. 아니라면 여러개의 후보를 리턴해주고, 다시한번 요청)
    private String dong;                    // 동명 (선택사항, 있다면 표기)
    private String type;                    // 0 - 지상, 1 - 지하, 2- 공중
    // 추가 데이터 - 주소를 정확히 입력했다면 사용할 필요 없음 -> 사용자에게 후보 리스트 보여준 후 입력을 받아야함.
    private String dongNum;
    private String is2Way;
    private TwoWayInfo twoWayInfo;

    @Getter
    @Setter
    public static class TwoWayInfo {
        private int jobIndex;
        private int threadIndex;
        private String jti;
        private long twoWayTimestamp;

        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("jobIndex", jobIndex);
            map.put("threadIndex", threadIndex);
            map.put("jti", jti);
            map.put("twoWayTimestamp", twoWayTimestamp);
            return map;
        }
    }

    public HashMap<String, Object> toMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("organization", organization);
        map.put("loginType", loginType);
        if (userId != null) map.put("userId", userId);
        if (userPassword != null) map.put("userPassword", userPassword);
        if (address != null) map.put("address", address);
        if (dong != null) map.put("dong", dong);
        if (type != null) map.put("type", type);
        if (dongNum != null) map.put("dongNum", dongNum);
        if (is2Way != null) map.put("is2Way", is2Way);
        if (twoWayInfo != null) map.put("twoWayInfo", twoWayInfo.toMap());
        return map;
    }
}
