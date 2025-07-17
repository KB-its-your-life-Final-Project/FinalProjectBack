package com.lighthouse.buildingRegister.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lighthouse.buildingRegister.dto.BuildingRequestDTO;
import com.lighthouse.buildingRegister.dto.BuildingResponseDTO;
import io.codef.api.EasyCodef;
import io.codef.api.EasyCodefServiceType;
import io.codef.api.EasyCodefUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;


@Service
@Slf4j
@RequiredArgsConstructor
public class BuildingRegisterService {
    @Value("${CODEF_DEMO_ID}") private String id;
    @Value("${CODEF_DEMO_PW}") private String password;
    @Value("${CODEF_PUBLIC_KEY}") private String publicKey;
    @Value("${PRIVATE_ID}") private String privateId;
    @Value("${PRIVATE_PW}") private String privatePassword;

    private final BuildingRegisterPersistence buildingRegisterPersistence;
    public void getBuildingRegisterCommon(String address, String type) {
        BuildingResponseDTO finalResult = null;
        try{
            EasyCodef codef = new EasyCodef();
            codef.setClientInfoForDemo(id, password);
            codef.setPublicKey(publicKey);

            /** 요청 파라미터 설정 - 각 상품별 파라미터를 설정(https://developer.codef.io/products) */
            BuildingRequestDTO buildingRequestDTO = BuildingRequestDTO.builder()
                    .address(address)
                    .userId(privateId)
                    .userPassword(EasyCodefUtil.encryptRSA(privatePassword,codef.getPublicKey()))
                    .type(type)
                    .build();
            HashMap<String, Object> parameterMap = buildingRequestDTO.toMap();

            /** 코드에프 정보 조회 요청 - 서비스타입(API:정식, DEMO:데모, SANDBOX:샌드박스) */
            String productUrl = "/v1/kr/public/lt/eais/general-buildings";
            String result = codef.requestProduct(productUrl, EasyCodefServiceType.DEMO, parameterMap);

            /**	#7.코드에프 정보 결과 매핑 후 확인	*/
            HashMap<String, Object> responseMap = new ObjectMapper().readValue(result, new TypeReference<>() {
            });


            ObjectMapper mapper = new ObjectMapper();
            Object dataObj = responseMap.get("data");
            Object resultObj = responseMap.get("result");
            String json = mapper.writeValueAsString(resultObj);
            HashMap<String, Object> resultMap = mapper.readValue(json, new TypeReference<>() {});
            if(resultMap.get("code").equals("CF-00000")){
                json = mapper.writeValueAsString(dataObj);
                finalResult = mapper.readValue(json, BuildingResponseDTO.class);
            }
            else if(resultMap.get("code").equals("CF-03002")){  // 추가 입력 요청을 처리하는 로직
                json = mapper.writeValueAsString(dataObj);
                BuildingResponseDTO responseDto = mapper.readValue(json, BuildingResponseDTO.class);
                String dongNum = responseDto.getExtraInfo().getReqDongNumList().get(0).getCommonDongNum();
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
                json = mapper.writeValueAsString(dataObj);
                finalResult = mapper.readValue(json, BuildingResponseDTO.class);
            }
        } catch (JsonProcessingException e) {
            log.error("응답 파싱 실패", e);
            throw new RuntimeException("응답 파싱 실패", e);
        } catch (IOException | InterruptedException e) {
            log.error("API 요청 실패", e);
            throw new RuntimeException("API 호출 실패", e);
        }  catch (GeneralSecurityException e) {
            log.error("암호화 처리 실패", e);
            throw new SecurityException("암호화 오류", e);
        }

        // DB 저장
        if(finalResult == null) return;
        buildingRegisterPersistence.insertBuildingRegister(finalResult);

    }
}
