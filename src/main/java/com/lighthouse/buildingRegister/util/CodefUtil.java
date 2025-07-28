package com.lighthouse.buildingRegister.util;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lighthouse.buildingRegister.dto.BuildingRequestDTO;
import com.lighthouse.buildingRegister.dto.BuildingResponseDTO;
import io.codef.api.EasyCodef;
import io.codef.api.EasyCodefServiceType;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;


public class CodefUtil {
    @Getter
    private final EasyCodef codef;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private boolean compareIgnoringSpaces(String a, String b) {
        String a1 = a.replaceAll("\\s+", "");
        String b1 = b.replaceAll("\\s+", "");
        System.out.println("a1: " + a1);
        System.out.println("b1: " + b1);
        return a1.equals(b1);
    }

    public CodefUtil(String id, String password, String publicKey) {
        codef = new EasyCodef();
        codef.setClientInfoForDemo(id, password);
        codef.setPublicKey(publicKey);
    }

    public BuildingResponseDTO request(String productUrl, BuildingRequestDTO requestDto) throws Exception {
        BuildingResponseDTO finalResult;
        HashMap<String, Object> parameterMap = requestDto.toMap();

        String result = codef.requestProduct(productUrl, EasyCodefServiceType.DEMO, parameterMap);

        HashMap<String, Object> responseMap = objectMapper.readValue(result, new TypeReference<>() {});

        Object dataObj = responseMap.get("data");
        Object resultObj = responseMap.get("result");
        System.out.println("resultObj: " + resultObj);
        System.out.println("dataObj: " + dataObj);
        String json = objectMapper.writeValueAsString(resultObj);
        HashMap<String, Object> resultMap = objectMapper.readValue(json, new TypeReference<>() {});

        if ("CF-00000".equals(resultMap.get("code"))) {
            json = objectMapper.writeValueAsString(dataObj);
            return objectMapper.readValue(json, BuildingResponseDTO.class);
        } else if ("CF-03002".equals(resultMap.get("code"))) {
            json = objectMapper.writeValueAsString(dataObj);
            BuildingResponseDTO responseDto = objectMapper.readValue(json, BuildingResponseDTO.class);

            if(responseDto.getMethod().equals("dongNum")) {
                List<BuildingResponseDTO.ReqDongNum> dongNumList = responseDto.getExtraInfo().getReqDongNumList();
                String dong = requestDto.getDong();
                String dongNum = null;
                if (dongNumList != null) {
                    for (BuildingResponseDTO.ReqDongNum reqDongNum : dongNumList) {
                        if (reqDongNum.getReqDong().equals(dong)) {
                            dongNum = reqDongNum.getCommDongNum();
                            break;
                        }
                    }
                }
                parameterMap.put("dongNum", dongNum);
            }else if(responseDto.getMethod().equals("etc")) {
                String reqAddress = null;
                List<BuildingResponseDTO.ReqAddr> addrList = responseDto.getExtraInfo().getReqAddrList();
                if (addrList != null && !addrList.isEmpty()) {
                    System.out.println("제안된 주소 목록:");
                    for (int i = 0; i < addrList.size(); i++) {
                        System.out.println("  " + (i+1) + ". " + addrList.get(i).getCommAddrRoadName());
                    }
                    
                    // 모든 주소를 순차적으로 시도
                    for (int i = 0; i < addrList.size(); i++) {
                        reqAddress = addrList.get(i).getCommAddrRoadName();
                        System.out.println("시도 " + (i+1) + ": " + reqAddress);
                        
                        parameterMap.put("reqAddress", reqAddress);
                        
                        // 2차 인증 요청
                        Long twoWayTimestamp = responseDto.getTwoWayTimestamp();
                        Integer jobIndex = responseDto.getJobIndex();
                        Integer threadIndex = responseDto.getThreadIndex();
                        String jti = responseDto.getJti();
                        HashMap<String, Object> twoWayInfoMap = new HashMap<>();
                        twoWayInfoMap.put("jobIndex", jobIndex);
                        twoWayInfoMap.put("threadIndex", threadIndex);
                        twoWayInfoMap.put("jti", jti);
                        twoWayInfoMap.put("twoWayTimestamp", twoWayTimestamp);
                        parameterMap.put("is2Way", true);
                        parameterMap.put("twoWayInfo", twoWayInfoMap);
                        
                        result = codef.requestCertification(productUrl, EasyCodefServiceType.DEMO, parameterMap);
                        System.out.println("시도 " + (i+1) + " 결과: " + result);
                        
                        HashMap<String, Object> tryResponseMap = objectMapper.readValue(result, new TypeReference<>() {});
                        Object tryDataObj = tryResponseMap.get("data");
                        Object tryResultObj = tryResponseMap.get("result");
                        String tryJson = objectMapper.writeValueAsString(tryResultObj);
                        HashMap<String, Object> tryResultMap = objectMapper.readValue(tryJson, new TypeReference<>() {});
                        
                        // 성공한 경우
                        if ("CF-00000".equals(tryResultMap.get("code"))) {
                            System.out.println("성공! 주소: " + reqAddress);
                            json = objectMapper.writeValueAsString(tryDataObj);
                            finalResult = objectMapper.readValue(json, BuildingResponseDTO.class);
                            System.out.println("reqDong " + finalResult.getBuildingRegisterVO().getReqDong());
                            System.out.println("reqHo" + finalResult.getBuildingRegisterVO().getReqHo());
                            return finalResult;
                        }
                        
                        // 실패한 경우 계속 시도
                        System.out.println("실패: " + tryResultMap.get("message"));
                    }
                    
                    // 모든 주소 시도 실패
                    System.out.println("모든 주소 시도 실패");
                    throw new RuntimeException("해당 주소에 대한 건축물 정보를 찾을 수 없습니다.");
                }
            }
            
            // 주소가 같은지 확인 - 발견된 추가 인증원인이 띄어쓰기 차이였으므로 띄어쓰기 없앤 상태에서 리스트에 같은 값이 있는 지 확인함.
            // else if(responseDto.getMethod().equals("etc")) {
            //     String reqAddress = null;
            //     List<BuildingResponseDTO.ReqAddr> addrList = responseDto.getExtraInfo().getReqAddrList();
            //     if (addrList != null) {
            //         for (BuildingResponseDTO.ReqAddr addr : addrList) {
            //             if (compareIgnoringSpaces(requestDto.getAddress(), addr.getCommAddrRoadName())) {
            //                 reqAddress = addr.getCommAddrRoadName();
            //                 break;
            //             }
            //         }
            //     }
            //     parameterMap.put("reqAddress", reqAddress);
            // }
            Long twoWayTimestamp = responseDto.getTwoWayTimestamp();
            Integer jobIndex = responseDto.getJobIndex();
            Integer threadIndex = responseDto.getThreadIndex();
            String jti = responseDto.getJti();
            HashMap<String, Object> twoWayInfoMap = new HashMap<>();
            twoWayInfoMap.put("jobIndex", jobIndex);
            twoWayInfoMap.put("threadIndex", threadIndex);
            twoWayInfoMap.put("jti", jti);
            twoWayInfoMap.put("twoWayTimestamp", twoWayTimestamp);
            parameterMap.put("is2Way", true);
            parameterMap.put("twoWayInfo", twoWayInfoMap);
            result = codef.requestCertification(productUrl, EasyCodefServiceType.DEMO, parameterMap);
            System.out.println("final result: " + result);
            responseMap = new ObjectMapper().readValue(result, new TypeReference<>() {});
            dataObj = responseMap.get("data");
            json = objectMapper.writeValueAsString(dataObj);
            finalResult = objectMapper.readValue(json, BuildingResponseDTO.class);
        } else {
            // throw new RuntimeException("요청 실패: " + resultMap.get("code"));
            System.out.println("요청 실패: " + resultMap.get("code"));
            return null; // 예외 대신 null 반환
        }
        System.out.println("reqDong " + finalResult.getBuildingRegisterVO().getReqDong());
        System.out.println("reqHo" + finalResult.getBuildingRegisterVO().getReqHo() );
        return finalResult;
    }
}
