package com.lighthouse.alarm.service;

import com.lighthouse.alarm.dto.AlarmResponseDto;
import com.lighthouse.alarm.entity.Alarms;
import com.lighthouse.alarm.mapper.AlarmMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlarmService {
    private final AlarmMapper alarmMapper;
    
    // 알림 설정 변경
    public void updateAlarmSetting(Integer memberId, Integer alarmType, Integer getAlarm) {
        alarmMapper.updateAlarmSetting(memberId, alarmType, getAlarm);
    }
    
    // 미확인 알림 목록 조회
    public List<AlarmResponseDto> getAlarmList(Integer memberId) {
        return alarmMapper.getAlarmList(memberId);
    }
    
    // 새로운 알림 생성
    public void createAlarm(Integer memberId, Integer type, String text, String regIp) {
        try {
            log.info("알림 생성 시작: memberId={}, type={}, text={}", memberId, type, text);
            Alarms alarm = Alarms.builder()
                    .memberId(memberId)
                    .type(type)
                    .text(text)
                    .regIp(regIp)
                    .regDate(java.time.LocalDateTime.now())
                    .isChecked(0)  // 0: 확인 안함
                    .getAlarm(1)   // 1: 수신함 (기본값)
                    .build();
            
            alarmMapper.insertAlarm(alarm);
            log.info("알림 생성 완료: memberId={}, type={}", memberId, type);
        } catch (Exception e) {
            log.error("알림 생성 실패: memberId={}, type={}", memberId, type, e);
            throw e;
        }
    }
    
    // 스마트 알림 생성 (기본값: forceUpdate=false)
    public void createSmartAlarm(Integer memberId, Integer type, String text, String regIp) {
        createSmartAlarm(memberId, type, text, regIp, false);
    }
    
    // 스마트 알림 생성 (집 정보 수정 시 강제 업데이트 옵션 포함)
    public void createSmartAlarm(Integer memberId, Integer type, String text, String regIp, boolean forceUpdate) {
        try {
            if (forceUpdate) {
                // 집 정보 수정 시 강제 업데이트
                boolean exists = alarmMapper.existsAlarmByMemberAndType(memberId, type);
                
                if (exists) {
                    alarmMapper.updateAllAlarmsByMemberAndType(memberId, type, text);
                } else {
                    createAlarm(memberId, type, text, regIp);
                }
                return;
            }
            
            // 로그인 시에는 미확인 알림만 확인하여 업데이트
            List<AlarmResponseDto> existingAlarms = alarmMapper.getAllAlarmsByMember(memberId);
            boolean hasUnreadAlarm = existingAlarms.stream()
                    .anyMatch(alarm -> alarm.getType().equals(type) && alarm.getIsChecked() == 0);
            
            if (hasUnreadAlarm) {
                alarmMapper.updateAlarmText(memberId, type, text);
            }
        } catch (Exception e) {
            log.error("스마트 알림 생성 실패: memberId={}, type={}", memberId, type, e);
            throw e;
        }
    }
    
    // 계약 만료 알림 생성 (로그인 시)
    public void createContractExpirationNotification(Integer memberId, String propertyAddress, int daysLeft, String regIp) {
        String alarmText = buildContractExpirationText(propertyAddress, daysLeft);
        createSmartAlarm(memberId, 3, alarmText, regIp, false);
    }
    
    // 계약 만료 알림 생성 (집 정보 수정 시)
    public void createContractExpirationNotificationForUpdate(Integer memberId, String propertyAddress, int daysLeft, String regIp) {
        String alarmText = buildContractExpirationText(propertyAddress, daysLeft);
        createSmartAlarm(memberId, 3, alarmText, regIp, true);
    }
    
    // 계약 만료 알림 텍스트 생성
    private String buildContractExpirationText(String propertyAddress, int daysLeft) {
        if (daysLeft == 1) {
            return "현재 살고 있는 집 '" + propertyAddress + "'의 계약이 내일 만료됩니다!";
        } else if (daysLeft <= 7) {
            return "현재 살고 있는 집 '" + propertyAddress + "'의 계약이 " + daysLeft + "일 후 만료됩니다. (긴급)";
        } else {
            return "현재 살고 있는 집 '" + propertyAddress + "'의 계약이 " + daysLeft + "일 후 만료됩니다.";
        }
    }
    
    // 사용자 로그인 시 알림 조건 체크
    public void checkUserAlarmsOnLogin(Integer memberId, String regIp) {
        try {
            List<Map<String, Object>> userHomes = alarmMapper.getAllUserHomes(memberId);
            
            if (!userHomes.isEmpty()) {
                checkUserContractExpiration(memberId, regIp);
                checkLikedEstatePriceChanges(memberId, regIp);
            }
        } catch (Exception e) {
            log.error("사용자 {} 로그인 시 알림 조건 체크 중 오류 발생: {}", memberId, e.getMessage(), e);
        }
    }
    
    // 사용자 집 정보 수정 시 알림 조건 체크
    public void checkUserAlarmsOnHomeUpdate(Integer memberId, String regIp) {
        try {
            List<Map<String, Object>> userHomes = alarmMapper.getAllUserHomes(memberId);
            if (!userHomes.isEmpty()) {
                checkUserContractExpirationForUpdate(memberId, regIp);
                checkLikedEstatePriceChanges(memberId, regIp);
            }
        } catch (Exception e) {
            log.error("사용자 {} 집 정보 수정 시 알림 조건 체크 중 오류 발생: {}", memberId, e.getMessage(), e);
        }
    }
    
    // 사용자 계약 만료 알림 체크
    public void checkUserContractExpiration(Integer memberId, String regIp) {
        for (int daysLeft = 30; daysLeft >= 1; daysLeft--) {
            checkUserExpiringContracts(memberId, daysLeft, regIp);
        }
    }
    
    // 사용자 계약 만료 알림 체크 (집 정보 수정 시)
    public void checkUserContractExpirationForUpdate(Integer memberId, String regIp) {
        for (int daysLeft = 30; daysLeft >= 1; daysLeft--) {
            checkUserExpiringContractsForUpdate(memberId, daysLeft, regIp);
        }
    }
    
    // 특정 일수 후 만료되는 계약 체크
    private void checkUserExpiringContracts(Integer memberId, int daysLeft, String regIp) {
        List<Map<String, Object>> expiringContracts = alarmMapper.getExpiringContractsByUser(memberId, daysLeft);
        
        for (Map<String, Object> contract : expiringContracts) {
            String propertyAddress = (String) contract.get("property_address");
            createContractExpirationNotification(memberId, propertyAddress, daysLeft, regIp);
        }
    }
    
    // 특정 일수 후 만료되는 계약 체크 (집 정보 수정 시)
    private void checkUserExpiringContractsForUpdate(Integer memberId, int daysLeft, String regIp) {
        List<Map<String, Object>> expiringContracts = alarmMapper.getExpiringContractsByUser(memberId, daysLeft);
        
        for (Map<String, Object> contract : expiringContracts) {
            String propertyAddress = (String) contract.get("property_address");
            createContractExpirationNotificationForUpdate(memberId, propertyAddress, daysLeft, regIp);
        }
    }
    
    // 관심 건물 시세 변동 알림 체크
    public void checkLikedEstatePriceChanges(Integer memberId, String regIp) {
        try {
            List<Map<String, Object>> allTrades = alarmMapper.getPriceChangesForLikedEstates(memberId);
            
            for (Map<String, Object> trade : allTrades) {
                try {
                    processTradeForPriceChange(trade, memberId, regIp);
                } catch (Exception e) {
                    log.error("개별 건물 시세 변동 체크 중 오류: {}", e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.error("사용자 {}의 관심 건물 시세 변동 알림 체크 중 오류 발생: {}", memberId, e.getMessage(), e);
        }
    }
    
    // 개별 거래 처리
    private void processTradeForPriceChange(Map<String, Object> trade, Integer memberId, String regIp) {
        Integer estateId = (Integer) trade.get("estate_id");
        String buildingName = (String) trade.get("building_name");
        if (buildingName == null || buildingName.isEmpty()) {
            buildingName = (String) trade.get("estate_building_name");
        }
        String likedDate = (String) trade.get("liked_date");
        
        if (estateId == null || buildingName == null || buildingName.isEmpty() || likedDate == null) {
            return;
        }
        
        LocalDate likedLocalDate = LocalDate.parse(likedDate);
        int likedYear = likedLocalDate.getYear();
        int likedMonth = likedLocalDate.getMonthValue();
        int likedDay = likedLocalDate.getDayOfMonth();
        
        Integer dealYear = (Integer) trade.get("deal_year");
        Integer dealMonth = (Integer) trade.get("deal_month");
        Integer dealDay = (Integer) trade.get("deal_day");
        
        if (dealYear == null || dealMonth == null || dealDay == null) {
            return;
        }
        
        boolean isAfterLikedDate = (dealYear > likedYear) ||
                (dealYear == likedYear && dealMonth > likedMonth) ||
                (dealYear == likedYear && dealMonth == likedMonth && dealDay > likedDay);
        
        if (isAfterLikedDate) {
            List<Map<String, Object>> previousTrades = alarmMapper.getPreviousPriceForEstate(estateId, likedYear, likedMonth, likedDay);
            
            if (!previousTrades.isEmpty()) {
                Map<String, Object> previousTrade = previousTrades.get(0);
                boolean priceChanged = comparePrices(trade, previousTrade);
                
                if (priceChanged) {
                    createEstatePriceChangeNotification(memberId, buildingName, regIp);
                }
            }
        }
    }
    
    // 거래 가격 비교
    private boolean comparePrices(Map<String, Object> currentTrade, Map<String, Object> previousTrade) {
        try {
            Integer currentTradeType = (Integer) currentTrade.get("trade_type");
            Integer previousTradeType = (Integer) previousTrade.get("trade_type");
            
            if (currentTradeType == null || previousTradeType == null || !currentTradeType.equals(previousTradeType)) {
                return false;
            }
            
            if (currentTradeType == 1) {
                // 매매 거래
                Long currentAmount = (Long) currentTrade.get("deal_amount");
                Long previousAmount = (Long) previousTrade.get("deal_amount");
                return currentAmount != null && previousAmount != null && !currentAmount.equals(previousAmount);
            } else if (currentTradeType == 2) {
                // 전세 거래
                Long currentDeposit = (Long) currentTrade.get("deposit");
                Long previousDeposit = (Long) previousTrade.get("deposit");
                return currentDeposit != null && previousDeposit != null && !currentDeposit.equals(previousDeposit);
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
        createAlarm(memberId, 2, alarmText, regIp);
    }
    
    // ===== 집 계약 관련 알림 생성 =====
    
    // 1단계: 집 정보 등록 시 첫 번째 알림 생성
    public void createInitialHouseContractAlarm(Integer memberId, String regIp) {
        String alarmText = "집을 계약하셨다면 권리 변동사항을 확인하세요";
        log.info("Type 1 알림 생성 시작: memberId={}, text={}", memberId, alarmText);
        createAlarm(memberId, 1, alarmText, regIp);
        log.info("Type 1 알림 생성 완료: memberId={}", memberId);
    }
    
    // 집 정보 수정 시 기존 Type 1 알림들을 모두 삭제하고 새로운 1단계 알림 생성
    public void resetHouseContractAlarms(Integer memberId, String regIp) {
        try {
            log.info("집 계약 알림 초기화 시작: memberId={}", memberId);
            alarmMapper.deleteAllType1AlarmsByMember(memberId);
            log.info("기존 Type 1 알림 삭제 완료: memberId={}", memberId);
            createInitialHouseContractAlarm(memberId, regIp);
            log.info("집 계약 알림 초기화 완료: memberId={}", memberId);
        } catch (Exception e) {
            log.error("집 계약 알림 초기화 중 오류 발생: memberId={}", memberId, e);
        }
    }
    
    // 2단계: 첫 번째 알림 확인 시 두 번째 알림 생성
    public void createSecondHouseContractAlarm(Integer memberId, String regIp) {
        String alarmText = "권리 변동사항을 확인하셨다면 정부24나 관할 주민센터에서 전입신고를 진행해주세요!";
        createAlarm(memberId, 1, alarmText, regIp);
    }
    
    // 3단계: 두 번째 알림 확인 시 세 번째 알림 생성
    public void createThirdHouseContractAlarm(Integer memberId, String regIp) {
        String alarmText = "전세 계약 기간이 1/2가 지나기 전에 전세보증보험을 신청하세요!";
        createAlarm(memberId, 1, alarmText, regIp);
    }
    
    // 4단계: 세 번째 알림 확인 시 네 번째 알림 생성
    public void createFourthHouseContractAlarm(Integer memberId, String regIp) {
        String alarmText = "관할 세무서나 주민센터에서 임대인 세금체납 여부를 확인하세요!";
        createAlarm(memberId, 1, alarmText, regIp);
    }
    
    // 알림 읽음 처리 시 다음 단계 알림 자동 생성
    @Transactional
    public void setAlarmRead(Integer memberId, Integer alarmId) {
        try {
            // 읽음 처리 전에 해당 알림 정보 조회
            List<AlarmResponseDto> allAlarms = alarmMapper.getAllAlarmsByMember(memberId);
            AlarmResponseDto targetAlarm = allAlarms.stream()
                    .filter(alarm -> alarm.getId().equals(alarmId))
                    .findFirst()
                    .orElse(null);
            
            // 기본 알림 읽음 처리
            int updatedRows = alarmMapper.setAlarmRead(memberId, alarmId);
            
            if (updatedRows == 0) {
                log.warn("알림 읽음 처리 실패: 업데이트된 행이 없음. memberId={}, alarmId={}", memberId, alarmId);
            }
            
            // Type 1 알림인 경우에만 다음 단계 알림 자동 생성
            if (targetAlarm != null && targetAlarm.getType() == 1) {
                checkAndCreateNextHouseContractAlarm(memberId, targetAlarm);
            }
        } catch (Exception e) {
            log.error("알림 읽음 처리 중 오류 발생: memberId={}, alarmId={}, error={}", 
                    memberId, alarmId, e.getMessage(), e);
            throw e;
        }
    }
    
    // Type 1 알림 읽음 처리 시 다음 단계 알림 자동 생성 로직
    private void checkAndCreateNextHouseContractAlarm(Integer memberId, AlarmResponseDto readAlarm) {
        try {
            if (readAlarm == null || readAlarm.getType() != 1) {
                return;
            }
            
            // 현재 미확인 Type 1 알림이 있는지 확인
            int unreadCount = alarmMapper.getUnreadType1AlarmCount(memberId);
            if (unreadCount > 0) {
                return;
            }
            
            // 읽은 알림의 텍스트에 따라 다음 단계 알림 생성
            String alarmText = readAlarm.getText();
            String regIp = "127.0.0.1";
            
            if (alarmText.contains("집을 계약하셨다면 권리 변동사항을 확인하세요")) {
                createSecondHouseContractAlarm(memberId, regIp);
            } else if (alarmText.contains("정부24나 관할 주민센터에서 전입신고를 진행해주세요")) {
                createThirdHouseContractAlarm(memberId, regIp);
            } else if (alarmText.contains("전세보증보험을 신청하세요")) {
                createFourthHouseContractAlarm(memberId, regIp);
            }
        } catch (Exception e) {
            log.error("다음 단계 알림 생성 체크 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}