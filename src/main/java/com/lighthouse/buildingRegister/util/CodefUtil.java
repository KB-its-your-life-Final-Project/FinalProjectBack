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
import java.util.concurrent.*;


public class CodefUtil {
    @Getter
    private final EasyCodef codef;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private boolean compareIgnoringSpaces(String a, String b) {
        String a1 = a.replaceAll("\\s+", "");
        String b1 = b.replaceAll("\\s+", "");
        System.out.println("a1: " + a1);
        System.out.println("b1: " + b1);
        
        // 정확히 일치하는 경우
        if (a1.equals(b1)) {
            return true;
        }
        
        // 요청 주소가 제안 주소에 포함되는지 확인
        if (b1.contains(a1)) {
            System.out.println("요청 주소가 제안 주소에 포함됨: " + a1 + " -> " + b1);
            return true;
        }
        
        // 제안 주소가 요청 주소에 포함되는지 확인 (역방향)
        if (a1.contains(b1)) {
            System.out.println("제안 주소가 요청 주소에 포함됨: " + b1 + " -> " + a1);
            return true;
        }
        
        return false;
    }

    public CodefUtil(String id, String password, String publicKey) {
        codef = new EasyCodef();
        codef.setClientInfoForDemo(id, password);
        codef.setPublicKey(publicKey);
    }

    public BuildingResponseDTO request(String productUrl, BuildingRequestDTO requestDto) throws Exception {
        return requestWithTimeout(productUrl, requestDto, 15); // 30초 타임아웃
    }
    
    public BuildingResponseDTO requestWithTimeout(String productUrl, BuildingRequestDTO requestDto, int timeoutSeconds) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<BuildingResponseDTO> future = executor.submit(() -> {
            try {
                return requestInternal(productUrl, requestDto);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        
        try {
            System.out.println("CODEF API 호출 시작 (타임아웃: " + timeoutSeconds + "초): " + productUrl);
            return future.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            System.out.println("CODEF API 호출 타임아웃 (" + timeoutSeconds + "초 초과): " + productUrl);
            future.cancel(true);
            throw new RuntimeException("API 호출 타임아웃: " + timeoutSeconds + "초 초과", e);
        } catch (Exception e) {
            System.out.println("CODEF API 호출 실패: " + e.getMessage());
            throw e;
        } finally {
            executor.shutdownNow();
        }
    }
    
    private BuildingResponseDTO requestInternal(String productUrl, BuildingRequestDTO requestDto) throws Exception {
        HashMap<String, Object> parameterMap = requestDto.toMap();

        String result = codef.requestProduct(productUrl, EasyCodefServiceType.DEMO, parameterMap);
        
        System.out.println("CODEF API 응답 받음: " + (result != null ? result.substring(0, Math.min(200, result.length())) + "..." : "null"));

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
                if (dongNumList != null && !dongNumList.isEmpty()) {
                    if (dong != null) {
                        // dong이 있으면 매칭되는 동 찾기
                        for (BuildingResponseDTO.ReqDongNum reqDongNum : dongNumList) {
                            if (reqDongNum.getReqDong().equals(dong)) {
                                dongNum = reqDongNum.getCommDongNum();
                                break;
                            }
                        }
                    } else {
                        // dong이 null이면 첫 번째 후보 동 선택
                        dongNum = dongNumList.get(0).getCommDongNum();
                        System.out.println("dong이 null이므로 첫 번째 후보 동 선택: " + dongNumList.get(0).getReqDong());
                    }
                }
                parameterMap.put("dongNum", dongNum);
                
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
                System.out.println("dongNum 2차 인증 결과: " + result);
                
                HashMap<String, Object> tryResponseMap = objectMapper.readValue(result, new TypeReference<>() {});
                Object tryDataObj = tryResponseMap.get("data");
                Object tryResultObj = tryResponseMap.get("result");
                String tryJson = objectMapper.writeValueAsString(tryResultObj);
                HashMap<String, Object> tryResultMap = objectMapper.readValue(tryJson, new TypeReference<>() {});
                
                // 성공한 경우
                if ("CF-00000".equals(tryResultMap.get("code"))) {
                    System.out.println("dongNum 성공!");
                    json = objectMapper.writeValueAsString(tryDataObj);
                    BuildingResponseDTO dongNumResult = objectMapper.readValue(json, BuildingResponseDTO.class);
                    System.out.println("reqDong " + dongNumResult.getBuildingRegisterVO().getReqDong());
                    System.out.println("reqHo" + dongNumResult.getBuildingRegisterVO().getReqHo());
                    return dongNumResult;
                } else {
                    System.out.println("dongNum 실패: " + tryResultMap.get("message"));
                    // 2차 인증 취소인 경우 null 반환, 다른 실패는 예외 발생
                    if ("CF-12102".equals(tryResultMap.get("code"))) {
                        System.out.println("2차 인증이 취소되었습니다.");
                        return null;
                    } else {
                        throw new RuntimeException("동번호 매칭에 실패했습니다: " + tryResultMap.get("message"));
                    }
                }
            }else if(responseDto.getMethod().equals("etc")) {
                String reqAddress = null;
                List<BuildingResponseDTO.ReqAddr> addrList = responseDto.getExtraInfo().getReqAddrList();
                if (addrList != null && !addrList.isEmpty()) {
                    System.out.println("제안된 주소 목록:");
                    for (int i = 0; i < addrList.size(); i++) {
                        System.out.println("  " + (i+1) + ". " + addrList.get(i).getCommAddrRoadName());
                    }
                    
                    // 먼저 띄어쓰기 차이를 고려한 정확한 매칭 시도
                    for (BuildingResponseDTO.ReqAddr addr : addrList) {
                        if (compareIgnoringSpaces(requestDto.getAddress(), addr.getCommAddrRoadName())) {
                            reqAddress = addr.getCommAddrRoadName();
                            System.out.println("띄어쓰기 차이 고려하여 매칭된 주소: " + reqAddress);
                            break;
                        }
                    }
                    
                    // 정확한 매칭이 없으면 모든 주소를 순차적으로 시도
                    if (reqAddress == null) {
                        System.out.println("정확한 매칭 없음, 모든 주소 순차 시도");
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
                                BuildingResponseDTO addrResult = objectMapper.readValue(json, BuildingResponseDTO.class);
                                System.out.println("reqDong " + addrResult.getBuildingRegisterVO().getReqDong());
                                System.out.println("reqHo" + addrResult.getBuildingRegisterVO().getReqHo());
                                return addrResult;
                            } else if ("CF-03002".equals(tryResultMap.get("code"))) {
                                // 1차 성공, 2차 인증 필요 - 해당 주소로 2차 인증 처리
                                System.out.println("1차 성공, 2차 인증 필요: " + reqAddress);
                                
                                // 2차 인증 처리 (dongNum 방식)
                                try {
                                    // tryDataObj는 전체 응답 데이터이므로 BuildingResponseDTO로 파싱
                                    String tryDataJson = objectMapper.writeValueAsString(tryDataObj);
                                    BuildingResponseDTO tryResponse = objectMapper.readValue(tryDataJson, BuildingResponseDTO.class);
                                    BuildingResponseDTO.ExtraInfo extraInfo = tryResponse.getExtraInfo();
                                    
                                    if (extraInfo.getReqDongNumList() != null && !extraInfo.getReqDongNumList().isEmpty()) {
                                        // 첫 번째 후보 동 선택
                                        String dongNum = extraInfo.getReqDongNumList().get(0).getCommDongNum();
                                        System.out.println("첫 번째 후보 동 선택: " + extraInfo.getReqDongNumList().get(0).getReqDong());
                                        
                                        // 2차 인증 요청
                                        parameterMap.put("dongNum", dongNum);
                                        parameterMap.put("is2Way", true);
                                        parameterMap.put("twoWayInfo", twoWayInfoMap);
                                        
                                        result = codef.requestCertification(productUrl, EasyCodefServiceType.DEMO, parameterMap);
                                        System.out.println("2차 인증 결과: " + result);
                                        
                                        HashMap<String, Object> finalResponseMap = objectMapper.readValue(result, new TypeReference<>() {});
                                        Object finalDataObj = finalResponseMap.get("data");
                                        Object finalResultObj = finalResponseMap.get("result");
                                        String finalJson = objectMapper.writeValueAsString(finalResultObj);
                                        HashMap<String, Object> finalResultMap = objectMapper.readValue(finalJson, new TypeReference<>() {});
                                        
                                        if ("CF-00000".equals(finalResultMap.get("code"))) {
                                            System.out.println("2차 인증 성공! 주소: " + reqAddress);
                                            json = objectMapper.writeValueAsString(finalDataObj);
                                            BuildingResponseDTO finalResult = objectMapper.readValue(json, BuildingResponseDTO.class);
                                            return finalResult;
                                        } else {
                                            System.out.println("2차 인증 실패: " + finalResultMap.get("message"));
                                        }
                                    }
                                } catch (Exception e) {
                                    System.out.println("2차 인증 처리 중 오류: " + e.getMessage());
                                }
                                
                                // 2차 인증 실패 시 다음 주소로
                                continue;
                            }
                            
                            // 실패한 경우 계속 시도
                            System.out.println("실패: " + tryResultMap.get("message"));
                        }
                        
                        // 모든 주소 시도 실패
                        System.out.println("모든 주소 시도 실패");
                        return null; // 모든 주소 시도 실패 시 null 반환
                    } else {
                        // 정확한 매칭된 주소로 2차 인증 시도
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
                        System.out.println("정확한 매칭 주소로 2차 인증 결과: " );
                        
                        HashMap<String, Object> tryResponseMap = objectMapper.readValue(result, new TypeReference<>() {});
                        Object tryDataObj = tryResponseMap.get("data");
                        Object tryResultObj = tryResponseMap.get("result");
                        String tryJson = objectMapper.writeValueAsString(tryResultObj);
                        HashMap<String, Object> tryResultMap = objectMapper.readValue(tryJson, new TypeReference<>() {});
                        
                        // 성공한 경우
                        if ("CF-00000".equals(tryResultMap.get("code"))) {
                            System.out.println("성공! 정확한 매칭 주소: " + reqAddress);
                            json = objectMapper.writeValueAsString(tryDataObj);
                            BuildingResponseDTO exactMatchResult = objectMapper.readValue(json, BuildingResponseDTO.class);
                            System.out.println("reqDong " + exactMatchResult.getBuildingRegisterVO().getReqDong());
                            System.out.println("reqHo" + exactMatchResult.getBuildingRegisterVO().getReqHo());
                            return exactMatchResult;
                        } else {
                            System.out.println("정확한 매칭 주소로도 실패: ");
                            // 2차 인증 취소인 경우 null 반환, 다른 실패는 예외 발생
                            if ("CF-12102".equals(tryResultMap.get("code"))) {
                                System.out.println("2차 인증이 취소되었습니다.");
                                return null;
                            } else {
                                throw new RuntimeException("해당 주소에 대한 건축물 정보를 찾을 수 없습니다.");
                            }
                        }
                    }
                }
            }
        } else {
            // throw new RuntimeException("요청 실패: " + resultMap.get("code"));
            System.out.println("요청 실패: " + resultMap.get("code"));
            return null; // 예외 대신 null 반환
        }
        return null;
    }
}
