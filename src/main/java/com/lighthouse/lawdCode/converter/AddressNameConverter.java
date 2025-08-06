package com.lighthouse.lawdCode.converter;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AddressNameConverter {
    
    private static final Map<String, String> SIDO_NAME_MAP = Map.ofEntries(
        Map.entry("11", "서울"),
        Map.entry("26", "부산"),
        Map.entry("27", "대구"),
        Map.entry("28", "인천"),
        Map.entry("29", "광주"),
        Map.entry("30", "대전"),
        Map.entry("31", "울산"),
        Map.entry("36", "세종"),
        Map.entry("41", "경기"),
        Map.entry("42", "강원"),
        Map.entry("43", "충북"),
        Map.entry("44", "충남"),
        Map.entry("45", "전북"),
        Map.entry("46", "전남"),
        Map.entry("47", "경북"),
        Map.entry("48", "경남"),
        Map.entry("50", "제주"),
        Map.entry("52", "전북")
    );
    
    /**
     * 시도 코드를 시도명으로 변환
     */
    public String convertSidoName(String sidoCd) {
        return SIDO_NAME_MAP.getOrDefault(sidoCd, "기타");
    }
    
    /**
     * 시도 코드와 전체 주소명을 기반으로 시군구명 추출
     */
    public String convertSggName(String sidoCd, String locataddNm) {
        if (locataddNm == null || locataddNm.trim().isEmpty()) {
            return locataddNm;
        }
        
        switch(sidoCd) {
            case "11": // 서울
            case "26": // 부산
            case "27": // 대구
            case "28": // 인천
            case "29": // 광주
            case "30": // 대전
            case "31": // 울산
            case "36": // 세종
                return extractSecondWord(locataddNm);
                
            case "41": // 경기도
                return extractGyeonggiSgg(locataddNm);
                
            case "43": // 충북
            case "44": // 충남
            case "47": // 경북
            case "48": // 경남
                return extractProvinceSgg(locataddNm);
                
            case "42": // 강원
            case "45": // 전북 (기존 45)
                return extractLastWord(locataddNm);
                
            case "46": // 전남
            case "50": // 제주
            case "52": // 전북 (새로운 52)
                return extractSpecialProvinceSgg(locataddNm);
                
            default:
                return locataddNm;
        }
    }
    
    /**
     * 두 번째 단어 추출
     * "서울특별시 강남구 역삼동" -> "강남구"
     */
    private String extractSecondWord(String locataddNm) {
        String[] parts = locataddNm.split(" ");
        return parts.length >= 2 ? parts[1] : locataddNm;
    }
    
    /**
     * 마지막 단어 추출
     * "강원도 춘천시 동내면" -> "동내면"
     */
    private String extractLastWord(String locataddNm) {
        String[] parts = locataddNm.split(" ");
        return parts.length > 0 ? parts[parts.length - 1] : locataddNm;
    }
    
    /**
     * 경기도 시군구 추출
     * "경기도 성남시 분당구" -> "성남시 분당구"
     * "경기도 구리시 갈매동" -> "구리시"
     */
    private String extractGyeonggiSgg(String locataddNm) {
        if (locataddNm.contains("구")) {
            String[] parts = locataddNm.split(" ");
            if (parts.length >= 3) {
                return parts[1] + " " + parts[2];
            }
        }
        return extractSecondWord(locataddNm);
    }
    
    /**
     * 도 단위 시군구 추출 (충북, 충남, 경북, 경남)
     * "충청북도 청주시 상당구" -> "청주시 상당구"
     * "충청북도 제천시 청풍면" -> "제천시"
     */
    private String extractProvinceSgg(String locataddNm) {
        if (locataddNm.contains("구")) {
            String[] parts = locataddNm.split(" ");
            if (parts.length >= 3) {
                return parts[1] + " " + parts[2];
            }
        }
        return extractSecondWord(locataddNm);
    }
    
    /**
     * 특별도/자치도 시군구 추출 (전남, 제주, 전북특별자치도)
     * "전라남도 목포시 용당동" -> "목포시"
     * "제주특별자치도 제주시 일도일동" -> "제주시"
     * "전북특별자치도 전주시 완산구" -> "전주시 완산구"
     */
    private String extractSpecialProvinceSgg(String locataddNm) {
        if (locataddNm.contains("구")) {
            String[] parts = locataddNm.split(" ");
            if (parts.length >= 3) {
                return parts[1] + " " + parts[2];
            }
        }
        return extractSecondWord(locataddNm);
    }
}