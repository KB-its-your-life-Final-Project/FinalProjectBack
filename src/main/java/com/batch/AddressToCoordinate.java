package com.batch;

import com.batch.config.BatchConfig;
import com.lighthouse.safereport.mapper.SafeReportMapper;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;
import java.util.Map;
// 스프링 애플리케이션하고 독립적으로 실행 가능한 진입점
// JVM에서 단독 실행 가능, 스프링 Bean 직접 로딩
public class AddressToCoordinate {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.getEnvironment().setActiveProfiles("local");
        context.register(BatchConfig.class);
        context.refresh();

        AddressGeocodeService geocodeService = context.getBean(AddressGeocodeService.class);
        SafeReportMapper safeReportMapper = context.getBean(SafeReportMapper.class);

        List<String> addresses = safeReportMapper.selectAllRoadAddr();

        for (String address : addresses) {
            try {
                Map<String, Double> coords = geocodeService.getCoordinates(address);
                safeReportMapper.updateCoordinates(address, coords.get("lat"), coords.get("lng"));
                System.out.println("✅ " + address + " → " + coords);
            } catch (Exception e) {
                System.err.println("❌ 변환 실패: " + address + "(" + e.getMessage() + ")");
            }
        }

        context.close(); // 자원 정리
    }
}
