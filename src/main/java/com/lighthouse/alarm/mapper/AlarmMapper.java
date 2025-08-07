package com.lighthouse.alarm.mapper;

import com.lighthouse.alarm.dto.AlarmResponseDto;
import com.lighthouse.alarm.entity.Alarms;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface AlarmMapper {
    // 알람 설정 변경
    void updateAlarmSetting(@Param("memberId") Integer memberId,
    @Param("alarmType") Integer alarmType,
    @Param("getAlarm") Integer getAlarm                                );

    // 제공 받을 알림 리스트 조회
    List<AlarmResponseDto> getAlarmList(@Param("memberId") Integer memberId);
    
    // 모든 알림 조회 (디버깅용)
    List<AlarmResponseDto> getAllAlarmsByMember(@Param("memberId") Integer memberId);

    // 알림 읽음 처리
    int setAlarmRead(@Param("memberId")Integer memberId, @Param("alarmId")Integer alarmId);
    
    // 새로운 알림 삽입
    void insertAlarm(@Param("alarm") Alarms alarm);
    
    // 특정 사용자의 만료 예정 계약 조회
    List<Map<String, Object>> getExpiringContractsByUser(@Param("memberId") Integer memberId, @Param("daysLeft") int daysLeft);
    

    
    // 디버깅용: 사용자의 모든 집 정보 조회
    List<Map<String, Object>> getAllUserHomes(@Param("memberId") Integer memberId);
    
    // 동일한 사용자의 동일한 유형 알림 업데이트
    int updateAlarmText(@Param("memberId") Integer memberId, @Param("type") Integer type, @Param("text") String text);
    
    // 동일한 사용자의 동일한 유형 모든 알림 업데이트 (집 정보 수정 시)
    int updateAllAlarmsByMemberAndType(@Param("memberId") Integer memberId, @Param("type") Integer type, @Param("text") String text);
    
    // 동일한 사용자의 동일한 유형 알림 존재 여부 확인
    boolean existsAlarmByMemberAndType(@Param("memberId") Integer memberId, @Param("type") Integer type);
    
    // 사용자의 type 1 알림 중 미확인 알림 개수 조회
    int getUnreadType1AlarmCount(@Param("memberId") Integer memberId);
    
    // 사용자의 모든 type 1 알림 삭제
    void deleteAllType1AlarmsByMember(@Param("memberId") Integer memberId);
    
    // 사용자가 관심을 갖는 건물의 시세 변동 감지
    List<Map<String, Object>> getPriceChangesForLikedEstates(@Param("memberId") Integer memberId);
    
    // 특정 건물의 이전 거래 정보 조회 (관심 설정 시점 이전의 가장 최근 거래)
    List<Map<String, Object>> getPreviousPriceForEstate(@Param("estateId") Integer estateId,
                                                        @Param("likedYear") int likedYear,
                                                        @Param("likedMonth") int likedMonth,
                                                        @Param("likedDay") int likedDay);
}
