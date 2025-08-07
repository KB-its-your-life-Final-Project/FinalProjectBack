package com.lighthouse.alarm.service;

import com.lighthouse.alarm.dto.AlarmResponseDto;
import com.lighthouse.alarm.entity.Alarms;
import com.lighthouse.alarm.mapper.AlarmMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlarmService {
    private final AlarmMapper alarmMapper;
    
    // 알림 설정 변경 (get_alarm 업데이트)
    public void updateAlarmSetting(Integer memberId, Integer alarmType, Integer getAlarm){
        alarmMapper.updateAlarmSetting(memberId, alarmType, getAlarm);
        log.info("알림 설정 변경: memberId={}, alarmType={}, getAlarm={}", memberId, alarmType, getAlarm);
    }
    
    // 받아야 할 알림 목록 조회 (get_alarm=1이고 is_checked=0인 것들만)
    public List<AlarmResponseDto> getAlarmList(Integer memberId){
        List<AlarmResponseDto> alarmsList = alarmMapper.getAlarmList(memberId);
        return alarmsList;
    }
    
    // 알림 읽음 처리 (is_checked를 1로 변경)
    public void setAlarmRead(Integer memberId, Integer alarmId){
        alarmMapper.setAlarmRead(memberId, alarmId);
        log.info("알림 읽음 처리: memberId={}, alarmId={}", memberId, alarmId);
    }
    
    // 새로운 알림 생성
    public void createAlarm(Integer memberId, Integer type, String text, String regIp) {
        try {
            Alarms alarm = Alarms.builder()
                    .memberId(memberId)
                    .type(type)
                    .text(text)
                    .regIp(regIp)
                    .regDate(java.time.LocalDateTime.now())
                    .isChecked(0)  // 0: 확인 안함
                    .getAlarm(1)   // 1: 수신함 (기본값)
                    .build();
            
            log.info("알림 엔티티 생성 완료: {}", alarm);
            
            alarmMapper.insertAlarm(alarm);
            
        } catch (Exception e) {
            log.error("알림 생성 실패: memberId={}, type={}", memberId, type, e);
            throw e;
        }
    }
    
    // 알림 생성 (동일한 유형이 있으면 업데이트, 없으면 새로 생성)
    public void createSmartAlarm(Integer memberId, Integer type, String text, String regIp) {
        createSmartAlarm(memberId, type, text, regIp, false);
    }
    
    // 알림 생성 (집 정보 수정 시 강제 업데이트 옵션 포함)
    public void createSmartAlarm(Integer memberId, Integer type, String text, String regIp, boolean forceUpdate) {
        try {
            
            if (forceUpdate) {
                // 집 정보 수정 시 강제 업데이트
                
                // 기존 알림이 있는지 확인
                boolean exists = alarmMapper.existsAlarmByMemberAndType(memberId, type);
                
                if (exists) {
                    // 기존 알림 업데이트 (집 정보 수정 시에는 모든 같은 타입 알림 업데이트)
                    List<AlarmResponseDto> beforeAlarms = alarmMapper.getAllAlarmsByMember(memberId);
                    log.info("업데이트 전 모든 알림 목록: {}", beforeAlarms);
                    
                    int updatedRows = alarmMapper.updateAllAlarmsByMemberAndType(memberId, type, text);
                    
                    List<AlarmResponseDto> afterAlarms = alarmMapper.getAllAlarmsByMember(memberId);
                } else {
                    // 기존 알림이 없으면 새로 생성
                    createAlarm(memberId, type, text, regIp);
                }
                return;
            }
            
            // 동일한 사용자의 동일한 유형 알림이 이미 존재하는지 확인
            boolean exists = alarmMapper.existsAlarmByMemberAndType(memberId, type);
            
            if (exists) {
                // 기존 알림 업데이트
                int updatedRows = alarmMapper.updateAlarmText(memberId, type, text);
            } else {
                // 새 알림 생성
                createAlarm(memberId, type, text, regIp);
            }
        } catch (Exception e) {
            throw e;
        }
    }
    
    // 계약 만료 알림 생성 (로그인 시)
    public void createContractExpirationNotification(Integer memberId, String propertyAddress, int daysLeft, String regIp) {
        String alarmText;
        
        if (daysLeft == 1) {
            alarmText = "현재 살고 있는 집 '" + propertyAddress + "'의 계약이 내일 만료됩니다!";
        } else if (daysLeft <= 7) {
            alarmText = "현재 살고 있는 집 '" + propertyAddress + "'의 계약이 " + daysLeft + "일 후 만료됩니다. (긴급)";
        } else {
            alarmText = "현재 살고 있는 집 '" + propertyAddress + "'의 계약이 " + daysLeft + "일 후 만료됩니다.";
        }
        
        // 로그인 시에는 forceUpdate=false로 호출
        createSmartAlarm(memberId, 3, alarmText, regIp, false);
    }
    
    // 계약 만료 알림 생성 (집 정보 수정 시)
    public void createContractExpirationNotificationForUpdate(Integer memberId, String propertyAddress, int daysLeft, String regIp) {
        
        String alarmText;
        
        if (daysLeft == 1) {
            alarmText = "현재 살고 있는 집 '" + propertyAddress + "'의 계약이 내일 만료됩니다!";
        } else if (daysLeft <= 7) {
            alarmText = "현재 살고 있는 집 '" + propertyAddress + "'의 계약이 " + daysLeft + "일 후 만료됩니다. (긴급)";
        } else {
            alarmText = "현재 살고 있는 집 '" + propertyAddress + "'의 계약이 " + daysLeft + "일 후 만료됩니다.";
        }
        
        // 집 정보 수정 시에는 forceUpdate=true로 호출
        createSmartAlarm(memberId, 3, alarmText, regIp, true);
    }
    

    
    // 사용자 로그인 시 알림 조건 체크
    public void checkUserAlarmsOnLogin(Integer memberId, String regIp) {
        try {
            
            // 사용자의 집 정보 확인
            List<Map<String, Object>> userHomes = alarmMapper.getAllUserHomes(memberId);
            
            if (!userHomes.isEmpty()) {
                for (Map<String, Object> home : userHomes) {
                    log.info("집 정보: {}", home);
                }
                
                // 계약 만료 알림 체크 (집이 있는 경우에만)
                try {
                    checkUserContractExpiration(memberId, regIp);
                } catch (Exception e) {
                    log.error("사용자 {}의 계약 만료 알림 체크 중 오류 발생: {}", memberId, e.getMessage(), e);
                }
                
                // 관심 건물 시세 변동 알림 체크
                try {
                    checkLikedEstatePriceChanges(memberId, regIp);
                } catch (Exception e) {
                    log.warn("사용자 {}의 관심 건물 시세 변동 알림 체크 중 오류 발생 (무시하고 계속 진행): {}", memberId, e.getMessage());
                }
            } else {
                log.info("사용자 {}의 등록된 집이 없어서 알림 체크를 건너뜁니다.", memberId);
            }
            
            log.info("사용자 {} 로그인 시 알림 조건 체크 완료", memberId);
        } catch (Exception e) {
            log.error("사용자 {} 로그인 시 알림 조건 체크 중 예상치 못한 오류 발생: {}", memberId, e.getMessage(), e);
        }
    }
    
    // 사용자 집 정보 수정 시 알림 조건 체크
    public void checkUserAlarmsOnHomeUpdate(Integer memberId, String regIp) {
        try {
            
            // 사용자의 집 정보 확인
            List<Map<String, Object>> userHomes = alarmMapper.getAllUserHomes(memberId);
            if (!userHomes.isEmpty()) {
                for (Map<String, Object> home : userHomes) {
                    log.info("집 정보: {}", home);
                }
                
                // 계약 만료 알림 체크 (집이 있는 경우에만) - forceUpdate=true 사용
                try {
                    checkUserContractExpirationForUpdate(memberId, regIp);
                } catch (Exception e) {
                    log.error("사용자 {}의 계약 만료 알림 체크 중 오류 발생: {}", memberId, e.getMessage(), e);
                }
                
                // 관심 건물 시세 변동 알림 체크
                try {
                    checkLikedEstatePriceChanges(memberId, regIp);
                } catch (Exception e) {
                    log.warn("사용자 {}의 관심 건물 시세 변동 알림 체크 중 오류 발생 (무시하고 계속 진행): {}", memberId, e.getMessage());
                }
            } else {
                log.info("사용자 {}의 등록된 집이 없어서 알림 체크를 건너뜁니다.", memberId);
            }
            
        } catch (Exception e) {
            log.error("사용자 {} 집 정보 수정 시 알림 조건 체크 중 예상치 못한 오류 발생: {}", memberId, e.getMessage(), e);
        }
    }
    
    // 사용자 계약 만료 알림 체크
    public void checkUserContractExpiration(Integer memberId, String regIp) {
        // 30일 전부터 1일 전까지 매일 체크
        for (int daysLeft = 30; daysLeft >= 1; daysLeft--) {
            checkUserExpiringContracts(memberId, daysLeft, regIp);
        }
    }
    
    // 사용자 계약 만료 알림 체크 (집 정보 수정 시)
    public void checkUserContractExpirationForUpdate(Integer memberId, String regIp) {
        // 30일 전부터 1일 전까지 매일 체크
        for (int daysLeft = 30; daysLeft >= 1; daysLeft--) {
            checkUserExpiringContractsForUpdate(memberId, daysLeft, regIp);
        }
    }
    
    // 특정 일수 후 만료되는 계약 체크
    private void checkUserExpiringContracts(Integer memberId, int daysLeft, String regIp) {
        List<Map<String, Object>> expiringContracts = alarmMapper.getExpiringContractsByUser(memberId, daysLeft);
        
        if (expiringContracts.isEmpty()) {
            log.info("사용자 {}의 {}일 전 만료 계약이 없습니다.", memberId, daysLeft);
            return;
        }
        
        for (Map<String, Object> contract : expiringContracts) {
            String propertyAddress = (String) contract.get("property_address");
            
            createContractExpirationNotification(memberId, propertyAddress, daysLeft, regIp);
        }
    }
    
    // 특정 일수 후 만료되는 계약 체크 (집 정보 수정 시)
    private void checkUserExpiringContractsForUpdate(Integer memberId, int daysLeft, String regIp) {
        List<Map<String, Object>> expiringContracts = alarmMapper.getExpiringContractsByUser(memberId, daysLeft);
        
        if (expiringContracts.isEmpty()) {
            log.info("사용자 {}의 {}일 전 만료 계약이 없습니다.", memberId, daysLeft);
            log.info("=== 집 정보 수정 시 {}일 전 만료 계약 체크 완료 (계약 없음) ===", daysLeft);
            return;
        }
        
        for (Map<String, Object> contract : expiringContracts) {
            String propertyAddress = (String) contract.get("property_address");
            
            // 집 정보 수정 시 강제 알림 업데이트 (계약 종료일이 같든 다르든)
        
            createContractExpirationNotificationForUpdate(memberId, propertyAddress, daysLeft, regIp);
        }
        log.info("=== 집 정보 수정 시 {}일 전 만료 계약 체크 완료 ===", daysLeft);
    }
    
    // 관심 건물 시세 변동 알림 체크
    public void checkLikedEstatePriceChanges(Integer memberId, String regIp) {
        try {
            log.info("사용자 {}의 관심 건물 시세 변동 알림 체크 시작", memberId);
            
            // 사용자가 관심을 갖는 건물의 최근 거래 정보 조회 (관심 설정 시점 이후의 거래)
            List<Map<String, Object>> recentTrades = alarmMapper.getPriceChangesForLikedEstates(memberId);
            log.info("사용자 {}의 관심 건물 최근 거래 조회 결과: {}건", memberId, recentTrades.size());
            
            for (Map<String, Object> trade : recentTrades) {
                try {
                    Integer estateId = (Integer) trade.get("estate_id");
                    String buildingName = (String) trade.get("building_name");
                    if (buildingName == null || buildingName.isEmpty()) {
                        buildingName = (String) trade.get("estate_building_name");
                    }
                    String likedDate = (String) trade.get("liked_date");
                    
                    log.info("건물 정보: estateId={}, buildingName={}, likedDate={}", estateId, buildingName, likedDate);
                    
                    if (estateId != null && buildingName != null && !buildingName.isEmpty() && likedDate != null) {
                        // 관심 설정 시점 이전의 가장 최근 거래 정보 조회
                        List<Map<String, Object>> previousTrades = alarmMapper.getPreviousPriceForEstate(estateId, likedDate);
                        
                        if (!previousTrades.isEmpty()) {
                            Map<String, Object> previousTrade = previousTrades.get(0);
                            
                            // 현재 거래와 이전 거래 비교
                            boolean priceChanged = comparePrices(trade, previousTrade);
                            
                            if (priceChanged) {
                                log.info("시세 변동 감지: estateId={}, buildingName={}", estateId, buildingName);
                                createEstatePriceChangeNotification(memberId, buildingName, regIp);
                            }
                        } else {
                            log.info("이전 거래 정보가 없어서 시세 변동 비교 불가: estateId={}", estateId);
                        }
                    }
                } catch (Exception e) {
                    log.error("개별 건물 시세 변동 체크 중 오류: {}", e.getMessage(), e);
                }
            }
            
            log.info("사용자 {}의 관심 건물 시세 변동 알림 체크 완료", memberId);
        } catch (Exception e) {
            log.error("사용자 {}의 관심 건물 시세 변동 알림 체크 중 오류 발생: {}", memberId, e.getMessage(), e);
        }
    }
    
    // 거래 가격 비교 (매매/전세 구분)
    private boolean comparePrices(Map<String, Object> currentTrade, Map<String, Object> previousTrade) {
        try {
            Integer currentTradeType = (Integer) currentTrade.get("trade_type");
            Integer previousTradeType = (Integer) previousTrade.get("trade_type");
            
            // 거래 유형이 다르면 비교하지 않음
            if (currentTradeType == null || previousTradeType == null || !currentTradeType.equals(previousTradeType)) {
                log.info("거래 유형이 다르거나 null이어서 비교하지 않음: current={}, previous={}", currentTradeType, previousTradeType);
                return false;
            }
            
            // 매매 거래인 경우 (trade_type = 1)
            if (currentTradeType == 1) {
                Long currentAmount = (Long) currentTrade.get("deal_amount");
                Long previousAmount = (Long) previousTrade.get("deal_amount");
                
                if (currentAmount != null && previousAmount != null && !currentAmount.equals(previousAmount)) {
                    log.info("매매 가격 변동 감지: current={}, previous={}", currentAmount, previousAmount);
                    return true;
                }
            }
            // 전세 거래인 경우 (trade_type = 2)
            else if (currentTradeType == 2) {
                Long currentDeposit = (Long) currentTrade.get("deposit");
                Long previousDeposit = (Long) previousTrade.get("deposit");
                
                if (currentDeposit != null && previousDeposit != null && !currentDeposit.equals(previousDeposit)) {
                    log.info("전세 보증금 변동 감지: current={}, previous={}", currentDeposit, previousDeposit);
                    return true;
                }
            }
            
            return false;
        } catch (Exception e) {
            log.error("가격 비교 중 오류 발생: {}", e.getMessage(), e);
            return false;
        }
    }
    
    // 관심 건물 시세 변동 알림 생성
    public void createEstatePriceChangeNotification(Integer memberId, String buildingName, String regIp) {
        String alarmText = "관심 있는 '" + buildingName + "'의 최근 거래가가 변동되었습니다";
        
        // 시세 변동 알림은 type 2로 설정 (기존 시세 변화 알림 타입)
        // 기존 알림과 상관없이 항상 새로운 알림으로 생성
        createAlarm(memberId, 2, alarmText, regIp);
        
        log.info("관심 건물 시세 변동 알림 생성 완료: memberId={}, buildingName={}", memberId, buildingName);
    }
}