package com.lighthouse.alarm.service;

import com.lighthouse.alarm.dto.ContractExpirationAlarmDto;
import com.lighthouse.alarm.entity.Alarms;
import com.lighthouse.alarm.mapper.AlarmMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContractExpirationAlarmService {

    private final AlarmMapper alarmMapper;
    
    private static final int ALARM_TYPE_30_DAYS = 1;
    private static final int ALARM_TYPE_7_DAYS = 2;

    /**
     * 계약 만료 30일 전 알림을 발송
     */
    @Transactional
    public void sendContractExpiration30DaysAlarm() {
        log.info("계약 만료 30일 전 알림 발송 시작");
        
        try {
            List<ContractExpirationAlarmDto> targets = alarmMapper.findContractExpiration30Days();
            
            for (ContractExpirationAlarmDto target : targets) {
                sendAlarm(target, ALARM_TYPE_30_DAYS, "30일 전");
            }
            
            log.info("계약 만료 30일 전 알림 발송 완료: {}건", targets.size());
        } catch (Exception e) {
            log.error("계약 만료 30일 전 알림 발송 중 오류 발생", e);
        }
    }

    /**
     * 계약 만료 7일 전 알림을 발송
     */
    @Transactional
    public void sendContractExpiration7DaysAlarm() {
        log.info("계약 만료 7일 전 알림 발송 시작");
        
        try {
            List<ContractExpirationAlarmDto> targets = alarmMapper.findContractExpiration7Days();
            
            for (ContractExpirationAlarmDto target : targets) {
                sendAlarm(target, ALARM_TYPE_7_DAYS, "7일 전");
            }
            
            log.info("계약 만료 7일 전 알림 발송 완료: {}건", targets.size());
        } catch (Exception e) {
            log.error("계약 만료 7일 전 알림 발송 중 오류 발생", e);
        }
    }

    /**
     * 개별 알림 발송 처리
     */
    private void sendAlarm(ContractExpirationAlarmDto target, int alarmType, String daysText) {
        try {
            // 중복 발송 방지 체크
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            boolean alreadySent = alarmMapper.existsAlarmByTypeAndDate(target.getUserId(), alarmType, today);
            
            if (alreadySent) {
                log.info("이미 발송된 알림입니다: userId={}, type={}, date={}", 
                    target.getUserId(), alarmType, today);
                return;
            }

            // 알림 메시지 생성
            String alarmMessage = createAlarmMessage(target, daysText);
            
            // 알림 저장
            Alarms alarm = Alarms.builder()
                .memberId(target.getUserId())
                .type(alarmType)
                .text(alarmMessage)
                .time(LocalDateTime.now())
                .isChecked(0)
                .build();
            
            alarmMapper.insertAlarm(alarm);
            
            log.info("계약 만료 알림 발송 완료: userId={}, building={}, days={}", 
                target.getUserId(), target.getBuildingName(), daysText);
                
        } catch (Exception e) {
            log.error("개별 알림 발송 실패: userId={}, type={}", target.getUserId(), alarmType, e);
        }
    }

    /**
     * 알림 메시지 생성
     */
    private String createAlarmMessage(ContractExpirationAlarmDto target, String daysText) {
        String buildingInfo = target.getBuildingNumber() != null && !target.getBuildingNumber().isEmpty() 
            ? target.getBuildingName() + " " + target.getBuildingNumber() 
            : target.getBuildingName();
            
        return String.format("[계약 만료 알림] %s에 거주 중인 %s의 계약이 %s 만료됩니다. (%s)", 
            buildingInfo, target.getBuildingName(), daysText, target.getContractEnd());
    }

    /**
     * 수동으로 알림 발송 (테스트용)
     */
    @Transactional
    public void sendManualAlarm(Integer userId, String buildingName, String buildingNumber, 
                               LocalDate contractEnd, int daysUntilExpiration) {
        ContractExpirationAlarmDto target = ContractExpirationAlarmDto.builder()
            .userId(userId)
            .buildingName(buildingName)
            .buildingNumber(buildingNumber)
            .contractEnd(contractEnd)
            .daysUntilExpiration(daysUntilExpiration)
            .build();
            
        int alarmType = daysUntilExpiration == 30 ? ALARM_TYPE_30_DAYS : ALARM_TYPE_7_DAYS;
        String daysText = daysUntilExpiration == 30 ? "30일 전" : "7일 전";
        
        sendAlarm(target, alarmType, daysText);
    }
} 