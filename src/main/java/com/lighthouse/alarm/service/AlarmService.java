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
            log.info("알림 생성 시작: memberId={}, type={}, text={}, regIp={}", memberId, type, text, regIp);
            
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
            log.info("알림 DB 저장 완료: memberId={}, type={}", memberId, type);
            
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
            log.info("스마트 알림 생성 시작: memberId={}, type={}, text={}, forceUpdate={}", memberId, type, text, forceUpdate);
            
            if (forceUpdate) {
                // 집 정보 수정 시 강제 업데이트
                log.info("집 정보 수정으로 인한 강제 알림 업데이트: memberId={}, type={}", memberId, type);
                
                // 기존 알림이 있는지 확인
                boolean exists = alarmMapper.existsAlarmByMemberAndType(memberId, type);
                log.info("집 정보 수정 시 기존 알림 존재 여부: memberId={}, type={}, exists={}", memberId, type, exists);
                
                if (exists) {
                    // 기존 알림 업데이트
                    int updatedRows = alarmMapper.updateAlarmText(memberId, type, text);
                    log.info("집 정보 수정 기존 알림 업데이트 완료: memberId={}, type={}, updatedRows={}", memberId, type, updatedRows);
                } else {
                    // 기존 알림이 없으면 새로 생성
                    log.info("집 정보 수정 시 기존 알림이 없어서 새로 생성: memberId={}, type={}", memberId, type);
                    createAlarm(memberId, type, text, regIp);
                    log.info("집 정보 수정 새 알림 생성 완료: memberId={}, type={}", memberId, type);
                }
                return;
            }
            
            // 동일한 사용자의 동일한 유형 알림이 이미 존재하는지 확인
            boolean exists = alarmMapper.existsAlarmByMemberAndType(memberId, type);
            log.info("동일한 유형 알림 존재 여부: memberId={}, type={}, exists={}", memberId, type, exists);
            
            if (exists) {
                // 기존 알림 업데이트
                log.info("기존 알림 업데이트: memberId={}, type={}", memberId, type);
                int updatedRows = alarmMapper.updateAlarmText(memberId, type, text);
                log.info("알림 업데이트 완료: memberId={}, type={}, updatedRows={}", memberId, type, updatedRows);
            } else {
                // 새 알림 생성
                log.info("새 알림 생성: memberId={}, type={}", memberId, type);
                createAlarm(memberId, type, text, regIp);
                log.info("알림 생성 완료: memberId={}, type={}", memberId, type);
            }
        } catch (Exception e) {
            log.error("스마트 알림 생성 실패: memberId={}, type={}, text={}", memberId, type, text, e);
            throw e;
        }
    }
    
    // 계약 만료 알림 생성
    public void createContractExpirationNotification(Integer memberId, String propertyAddress, int daysLeft, String regIp) {
        String alarmText;
        
        if (daysLeft == 1) {
            alarmText = "현재 살고 있는 집 '" + propertyAddress + "'의 계약이 내일 만료됩니다!";
        } else if (daysLeft <= 7) {
            alarmText = "현재 살고 있는 집 '" + propertyAddress + "'의 계약이 " + daysLeft + "일 후 만료됩니다. (긴급)";
        } else {
            alarmText = "현재 살고 있는 집 '" + propertyAddress + "'의 계약이 " + daysLeft + "일 후 만료됩니다.";
        }
        
        // 집 정보 수정 시 강제 업데이트 (계약 종료일이 같든 다르든 항상 업데이트)
        createSmartAlarm(memberId, 3, alarmText, regIp, true);
    }
    
    // 시세 변화 알림 생성
    public void createPriceChangeNotification(Integer memberId, String areaName, String changeInfo, String regIp) {
        String text = String.format("관심 지역 '%s'의 시세가 %s", areaName, changeInfo);
        createSmartAlarm(memberId, 2, text, regIp); // type 2: 시세변화 알림
    }
    
    // 사용자 로그인 시 알림 조건 체크
    public void checkUserAlarmsOnLogin(Integer memberId, String regIp) {
        try {
            log.info("사용자 {} 로그인 시 알림 조건 체크 시작", memberId);
            
            // 사용자의 집 정보 확인
            log.info("사용자 {}의 집 정보 확인 중...", memberId);
            List<Map<String, Object>> userHomes = alarmMapper.getAllUserHomes(memberId);
            log.info("사용자 {}의 모든 집 정보: {}건", memberId, userHomes.size());
            
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
                
                // 시세 변화 알림 체크 (집이 있는 경우에만)
                try {
                    checkUserPriceChanges(memberId, regIp);
                } catch (Exception e) {
                    log.warn("사용자 {}의 시세 변화 알림 체크 중 오류 발생 (무시하고 계속 진행): {}", memberId, e.getMessage());
                }
            } else {
                log.info("사용자 {}의 등록된 집이 없어서 알림 체크를 건너뜁니다.", memberId);
            }
            
            log.info("사용자 {} 로그인 시 알림 조건 체크 완료", memberId);
        } catch (Exception e) {
            log.error("사용자 {} 로그인 시 알림 조건 체크 중 예상치 못한 오류 발생: {}", memberId, e.getMessage(), e);
        }
    }
    
    // 사용자 계약 만료 알림 체크
    public void checkUserContractExpiration(Integer memberId, String regIp) {
        log.info("사용자 {}의 계약 만료 알림 체크 시작", memberId);
        
        // 30일 전부터 1일 전까지 매일 체크
        for (int daysLeft = 30; daysLeft >= 1; daysLeft--) {
            log.info("사용자 {}의 {}일 전 만료 계약 체크 시작", memberId, daysLeft);
            checkUserExpiringContracts(memberId, daysLeft, regIp);
        }
        
        log.info("사용자 {}의 계약 만료 알림 체크 완료", memberId);
    }
    
    // 특정 일수 후 만료되는 계약 체크
    private void checkUserExpiringContracts(Integer memberId, int daysLeft, String regIp) {
        List<Map<String, Object>> expiringContracts = alarmMapper.getExpiringContractsByUser(memberId, daysLeft);
        log.info("사용자 {}의 {}일 전 만료 계약 조회 결과: {}건", memberId, daysLeft, expiringContracts.size());
        
        if (expiringContracts.isEmpty()) {
            log.info("사용자 {}의 {}일 전 만료 계약이 없습니다.", memberId, daysLeft);
            return;
        }
        
        for (Map<String, Object> contract : expiringContracts) {
            String propertyAddress = (String) contract.get("property_address");
            log.info("계약 정보: {}", contract);
            log.info("계약 만료 예정 매물: {}", propertyAddress);
            
            // 집 정보 수정 시 항상 알림 업데이트 (계약 종료일이 같든 다르든)
            log.info("집 정보 수정으로 인한 계약 만료 알림 업데이트: memberId={}, propertyAddress={}, daysLeft={}", memberId, propertyAddress, daysLeft);
            createContractExpirationNotification(memberId, propertyAddress, daysLeft, regIp);
            log.info("사용자 {}의 {}일 전 만료 계약 알림 업데이트 완료: {}건", memberId, daysLeft, 1);
        }
    }
    
    // 사용자 시세 변화 알림 체크
    public void checkUserPriceChanges(Integer memberId, String regIp) {
        try {
            log.info("사용자 {}의 시세 변화 알림 체크 시작", memberId);
            
            List<Map<String, Object>> priceChanges = alarmMapper.getInterestAreaPriceChangesByUser(memberId);
            log.info("사용자 {}의 시세 변화 조회 결과: {}건", memberId, priceChanges.size());
            
            for (Map<String, Object> change : priceChanges) {
                String areaName = (String) change.get("area_name");
                String changeInfo = (String) change.get("change_info");
                
                if (areaName != null && changeInfo != null) {
                    log.info("시세 변화 알림 생성: memberId={}, areaName={}, changeInfo={}", memberId, areaName, changeInfo);
                    createPriceChangeNotification(memberId, areaName, changeInfo, regIp);
                }
            }
            
            log.info("사용자 {}의 시세 변화 알림 체크 완료", memberId);
        } catch (Exception e) {
            log.warn("사용자 {}의 시세 변화 알림 체크 중 오류 발생 (무시하고 계속 진행): {}", memberId, e.getMessage());
            // 시세 변화 알림 오류는 전체 알림 체크를 중단시키지 않음
        }
    }
}