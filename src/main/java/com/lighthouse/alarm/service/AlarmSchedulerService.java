package com.lighthouse.alarm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlarmSchedulerService {
    
    private final AlarmService alarmService;
    
    /**
     * 사용자 로그인 시 알림 조건 체크
     * - 계약 만료 알림 체크
     * - 관심 건물 시세 변동 알림 체크
     */
    public void checkAlarmsOnLogin(Integer memberId, String regIp) {
        try {
            alarmService.checkUserAlarmsOnLogin(memberId, regIp);
        } catch (Exception e) {
            log.error("사용자 {} 로그인 시 알림 체크 중 오류 발생: {}", memberId, e.getMessage(), e);
        }
    }
    
    /**
     * 사용자 집 정보 등록/수정 시 알림 조건 체크
     * - 계약 만료 알림 체크 (강제 업데이트)
     * - 관심 건물 시세 변동 알림 체크
     * - 집 계약 관련 알림 생성 (Type 1)
     */
    public void checkAlarmsOnHomeUpdate(Integer memberId, String regIp) {
        try {
            alarmService.checkUserAlarmsOnHomeUpdate(memberId, regIp);
        } catch (Exception e) {
            log.error("사용자 {} 집 정보 등록/수정 시 알림 체크 중 오류 발생: {}", memberId, e.getMessage(), e);
        }
    }
    
    /**
     * 집 정보 등록 시 첫 번째 단계별 알림 생성
     */
    public void createInitialHouseContractAlarm(Integer memberId, String regIp) {
        try {
            log.info("첫 번째 단계별 알림 생성 시작: memberId={}, regIp={}", memberId, regIp);
            alarmService.createInitialHouseContractAlarm(memberId, regIp);
            log.info("첫 번째 단계별 알림 생성 완료: memberId={}", memberId);
        } catch (Exception e) {
            log.error("사용자 {} 집 정보 등록 시 첫 번째 단계별 알림 생성 중 오류 발생: {}", memberId, e.getMessage(), e);
        }
    }
    
    /**
     * 집 정보 수정 시 기존 Type 1 알림들을 모두 삭제하고 새로운 1단계 알림 생성
     */
    public void resetHouseContractAlarms(Integer memberId, String regIp) {
        try {
            log.info("집 계약 알림 초기화 시작: memberId={}, regIp={}", memberId, regIp);
            alarmService.resetHouseContractAlarms(memberId, regIp);
            log.info("집 계약 알림 초기화 완료: memberId={}", memberId);
        } catch (Exception e) {
            log.error("사용자 {} 집 정보 수정 시 알림 초기화 중 오류 발생: {}", memberId, e.getMessage(), e);
        }
    }
}
