package com.lighthouse.toCoord.service;
//토지 대장 DB인 building_registry의 지번 주소 -> 위도/경도
import com.lighthouse.config.RootConfig;
import com.lighthouse.toCoord.mapper.buildingToCoordMapper;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;
import java.util.Map;

public class BuildingAddressToCoordinate {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.getEnvironment().setActiveProfiles("local");
        context.register(RootConfig.class);
        context.refresh();

        AddressGeocodeService geocodeService = context.getBean(AddressGeocodeService.class);
        buildingToCoordMapper mapper = context.getBean(buildingToCoordMapper.class);

        List<String> addresses = mapper.selectAlljibunAddr();

        for (String address : addresses) {
            try {
                Map<String, Double> coords = geocodeService.getCoordinates(address);
                mapper.updateCoordinates(address, coords.get("lat"), coords.get("lng"));
                System.out.println("✅ " + address + " → " + coords);
            } catch (Exception e) {
                System.err.println("❌ 변환 실패: " + address + "(" + e.getMessage() + ")");
            }
        }
        context.close();
    }
}
