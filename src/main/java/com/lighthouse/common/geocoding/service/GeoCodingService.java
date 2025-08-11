package com.lighthouse.common.geocoding.service;

import java.util.Map;
import java.util.HashMap;
import org.springframework.stereotype.Service;
import com.lighthouse.common.external.naver.NaverMapClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeoCodingService {
    private final NaverMapClient naverMapClient;

    //주소에서 좌표로 변환
    public Map<String, Double> getCoordinateFromAddress(String address) {
        try {
            double lat = Double.parseDouble((String) naverMapClient.getInfoOfAddress(address).get("y"));
            double lng = Double.parseDouble((String) naverMapClient.getInfoOfAddress(address).get("x"));

            return Map.of("lat", lat, "lng", lng);
        }
        catch(Exception e) {
            throw e;
        }
    }

    // 위도/경도를 도로명 주소로 변환
    public String getRoadAddressFromCoordinates(double lat, double lng) {
        try {
            // naverMapClient를 통해 역지오코딩 API 호출
            Map<String, Object> result = naverMapClient.getAddressFromCoordinates(lat, lng, "roadaddr");
            
            if (result == null) {
                return null;
            }

            Map<String, Object> region = (Map<String, Object>) result.get("region");
            Map<String, Object> land = (Map<String, Object>) result.get("land");

            // 도로명 주소 구성
            StringBuilder roadAddress = new StringBuilder();
            
            // 시도
            if (region.containsKey("area1") && region.get("area1") != null) {
                Map<String, Object> area1 = (Map<String, Object>) region.get("area1");
                if (area1.containsKey("name")) {
                    roadAddress.append(area1.get("name"));
                }
            }
            
            // 시군구
            if (region.containsKey("area2") && region.get("area2") != null) {
                Map<String, Object> area2 = (Map<String, Object>) region.get("area2");
                if (area2.containsKey("name")) {
                    roadAddress.append(" ").append(area2.get("name"));
                }
            }
            
            // 도로명
            if (land != null && land.containsKey("name")) {
                roadAddress.append(" ").append(land.get("name"));
            }
            
            // 건물번호
            if (land != null && land.containsKey("number1")) {
                roadAddress.append(" ").append(land.get("number1"));
            }
            
            // 상세번호
            if (land != null && land.containsKey("number2")) {
                roadAddress.append("-").append(land.get("number2"));
            }

            String resultAddress = roadAddress.toString().trim();
            
            return resultAddress.isEmpty() ? null : resultAddress;

        } catch (Exception e) {
            log.warn("역지오코딩 실패 - lat: {}, lng: {}: {}", lat, lng, e.getMessage());
            return null;
        }
    }

    // 위도/경도로 상세 주소 정보 가져오기 (건물명, 지번주소, 법정동명, 시군구코드 등)
    public Map<String, String> getDetailedAddressFromCoordinates(double lat, double lng) {
        try {
            // naverMapClient를 통해 역지오코딩 API 호출 (법정동 주소 포함)
            Map<String, Object> result = naverMapClient.getAddressFromCoordinates(lat, lng, "legalcode,admcode,addr,roadaddr");
            
            if (result == null) {
                return null;
            }

            Map<String, Object> region = (Map<String, Object>) result.get("region");
            Map<String, Object> land = (Map<String, Object>) result.get("land");

            Map<String, String> addressInfo = new HashMap<>();
            
            // 건물명 (도로명 주소)
            StringBuilder buildingName = new StringBuilder();
            if (region.containsKey("area1") && region.get("area1") != null) {
                Map<String, Object> area1 = (Map<String, Object>) region.get("area1");
                if (area1.containsKey("name")) {
                    buildingName.append(area1.get("name"));
                }
            }
            if (region.containsKey("area2") && region.get("area2") != null) {
                Map<String, Object> area2 = (Map<String, Object>) region.get("area2");
                if (area2.containsKey("name")) {
                    buildingName.append(" ").append(area2.get("name"));
                }
            }
            if (land != null && land.containsKey("name")) {
                buildingName.append(" ").append(land.get("name"));
            }
            if (land != null && land.containsKey("number1")) {
                buildingName.append(" ").append(land.get("number1"));
            }
            if (land != null && land.containsKey("number2")) {
                buildingName.append("-").append(land.get("number2"));
            }
            addressInfo.put("buildingName", buildingName.toString().trim());
            
            // 지번주소 (법정동 주소)
            StringBuilder jibunAddress = new StringBuilder();
            if (region.containsKey("area1") && region.get("area1") != null) {
                Map<String, Object> area1 = (Map<String, Object>) region.get("area1");
                if (area1.containsKey("name")) {
                    jibunAddress.append(area1.get("name"));
                }
            }
            if (region.containsKey("area2") && region.get("area2") != null) {
                Map<String, Object> area2 = (Map<String, Object>) region.get("area2");
                if (area2.containsKey("name")) {
                    jibunAddress.append(" ").append(area2.get("name"));
                }
            }
            if (region.containsKey("area3") && region.get("area3") != null) {
                Map<String, Object> area3 = (Map<String, Object>) region.get("area3");
                if (area3.containsKey("name")) {
                    jibunAddress.append(" ").append(area3.get("name"));
                }
            }
            if (land != null && land.containsKey("number1")) {
                jibunAddress.append(" ").append(land.get("number1"));
            }
            if (land != null && land.containsKey("number2")) {
                jibunAddress.append("-").append(land.get("number2"));
            }
            addressInfo.put("jibunAddress", jibunAddress.toString().trim());
            
            // 법정동명 (읍면동)
            if (region.containsKey("area3") && region.get("area3") != null) {
                Map<String, Object> area3 = (Map<String, Object>) region.get("area3");
                if (area3.containsKey("name")) {
                    addressInfo.put("umdNm", (String) area3.get("name"));
                } else {
                    addressInfo.put("umdNm", "");
                }
            } else {
                addressInfo.put("umdNm", "");
            }
            
            // 시군구코드 (시군구의 코드)
            if (region.containsKey("area2") && region.get("area2") != null) {
                Map<String, Object> area2 = (Map<String, Object>) region.get("area2");
                if (area2.containsKey("coords") && area2.get("coords") != null) {
                    Map<String, Object> coords = (Map<String, Object>) area2.get("coords");
                    if (coords.containsKey("center")) {
                        // 시군구 코드는 API에서 직접 제공하지 않으므로 빈 값으로 설정
                        addressInfo.put("sggCd", "");
                    } else {
                        addressInfo.put("sggCd", "");
                    }
                } else {
                    addressInfo.put("sggCd", "");
                }
            } else {
                addressInfo.put("sggCd", "");
            }

            return addressInfo;

        } catch (Exception e) {
            log.warn("상세 주소 정보 조회 실패 - lat: {}, lng: {}: {}", lat, lng, e.getMessage());
            return null;
        }
    }
}