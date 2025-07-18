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
            List<BuildingResponseDTO.ReqDongNum> dongNumList = responseDto.getExtraInfo().getReqDongNumList();
            String dong = requestDto.getDong();
            String dongNum = null;
            for(BuildingResponseDTO.ReqDongNum reqDongNum : dongNumList){
                if(reqDongNum.getReqDong().equals(dong)){
                    dongNum = reqDongNum.getCommDongNum();
                    break;
                }
            }
            Long twoWayTimestamp = responseDto.getTwoWayTimestamp();
            Integer jobIndex = responseDto.getJobIndex();
            Integer threadIndex = responseDto.getThreadIndex();
            String jti = responseDto.getJti();
            HashMap<String, Object> twoWayInfoMap = new HashMap<>();
            twoWayInfoMap.put("jobIndex", jobIndex);
            twoWayInfoMap.put("threadIndex", threadIndex);
            twoWayInfoMap.put("jti", jti);
            twoWayInfoMap.put("twoWayTimestamp", twoWayTimestamp);

            parameterMap.put("dongNum", dongNum);
            parameterMap.put("is2Way", true);
            parameterMap.put("twoWayInfo", twoWayInfoMap);
            result = codef.requestCertification(productUrl, EasyCodefServiceType.DEMO, parameterMap);
            responseMap = new ObjectMapper().readValue(result, new TypeReference<>() {});
            dataObj = responseMap.get("data");
            json = objectMapper.writeValueAsString(dataObj);
            finalResult = objectMapper.readValue(json, BuildingResponseDTO.class);
        } else {
            throw new RuntimeException("요청 실패: " + resultMap.get("code"));
        }
        System.out.println("reqDong " + finalResult.getBuildingRegisterVO().getReqDong());
        System.out.println("reqHo" + finalResult.getBuildingRegisterVO().getReqHo() );
        return finalResult;
    }
}
