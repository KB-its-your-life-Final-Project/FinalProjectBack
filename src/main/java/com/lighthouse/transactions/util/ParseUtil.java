package com.lighthouse.transactions.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lighthouse.transactions.vo.LawdCdVO;
import com.lighthouse.transactions.entity.EstateApiIntegration;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParseUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 금액 string을 int로 변환 (예: "10,000" -> 10000)
     */
    public static int safeParseInt(String value) {
        try {
            return Integer.parseInt(value.replaceAll(",", "").trim());
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * EstateApiIntegration 객체에서 UNIQUE 파라미터 추출
     */
    public static Map<String, Object> getEstateParams(EstateApiIntegration estate) {
        Map<String, Object> params = new HashMap<>();
        params.put("mhouseType", estate.getMhouseType());
        params.put("shouseType", estate.getShouseType());
        params.put("buildYear", estate.getBuildYear());
        params.put("buildingType", estate.getBuildingType());
        params.put("jibunAddr", estate.getJibunAddr());
        return params;
    }

    /**
     * JSON에서 LawdCdVO 리스트로 변환
     */
    public static List<LawdCdVO> parseRowFromJson(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            JsonNode rows = root.path("StanReginCd").get(1).path("row");
            if (rows.isMissingNode() || !rows.isArray()) {
                return Collections.emptyList();
            }
            return objectMapper.readValue(rows.toString(), new TypeReference<List<LawdCdVO>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
