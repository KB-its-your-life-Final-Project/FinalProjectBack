# 알림 시스템 (Polling 방식)

## 개요

이 프로젝트는 서버에서 주기적으로 알림을 생성하고, 프론트엔드에서 polling 방식으로 알림을 조회하는 시스템입니다. 사용자가 등록한 집의 계약 만료일과 관심 지역의 시세 변화에 대한 알림을 자동으로 생성합니다.

## 주요 기능

### 1. 자동 알림 생성
- **계약 만료 알림**: 30일 전, 7일 전, 1일 전
- **시세 변화 알림**: 관심 지역의 시세 변화 시
- **스케줄링**: 매일 정해진 시간에 자동 실행

### 2. 알림 관리
- 알림 설정 (활성화/비활성화)
- 알림 읽음 처리
- 알림 목록 조회
- 미확인 알림 개수 조회

### 3. 로그인 시 알림 체크
- 사용자가 로그인할 때마다 해당 사용자의 알림 조건 체크
- 계약 만료 알림: 30일 전, 7일 전, 1일 전
- 시세 변화 알림: 관심 지역의 시세 변화율 5% 이상

## 기술 스택

- **Backend**: Spring Framework 5.3.38, MyBatis
- **스케줄링**: Spring @Scheduled
- **데이터베이스**: MySQL 8.4.0

## 아키텍처

```
사용자 로그인 → 알림 조건 체크 → 알림 생성 → 데이터베이스 저장
    ↓
프론트엔드 (Polling) → API 호출 → 사용자별 알림 조회
```

### 컴포넌트 구조

1. **AlarmSchedulerService**: 로그인 시 알림 체크 서비스
2. **AlarmService**: 알림 생성 및 관리 (로그인 시 알림 조건 체크 포함)
3. **AlarmController**: REST API 엔드포인트
4. **AlarmMapper**: 데이터베이스 접근
5. **MemberController**: 로그인 시 알림 체크 호출

## API 엔드포인트

### 알림 관리
- `GET /api/alarm/list`: 알림 목록 조회
- `GET /api/alarm/count`: 미확인 알림 개수 조회
- `PUT /api/alarm/settings`: 알림 설정 변경
- `PUT /api/alarm/{alarmId}/read`: 알림 읽음 처리

## 데이터베이스 스키마

### alarms_tbl (알림 테이블)
```sql
CREATE TABLE alarms_tbl (
    alarm_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    type INT NOT NULL COMMENT '1: 계약만료, 2: 시세변화',
    text TEXT NOT NULL COMMENT '알림 내용',
    time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_checked INT NOT NULL DEFAULT 0 COMMENT '0: 미확인, 1: 확인'
);
```

### alarm_settings_tbl (알림 설정 테이블)
```sql
CREATE TABLE alarm_settings_tbl (
    setting_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    alarm_type INT NOT NULL COMMENT '1: 계약만료, 2: 시세변화',
    get_alarm INT NOT NULL DEFAULT 1 COMMENT '0: 비활성화, 1: 활성화'
);
```

## 설치 및 설정

### 1. 데이터베이스 설정
```sql
-- 알림 테이블 생성
source alarms_tbl.sql;

-- 알림 설정 테이블 생성
source alarm_settings_tbl.sql;
```

### 2. 스케줄링 활성화
`SchedulingConfig.java`에서 `@EnableScheduling` 어노테이션이 활성화되어 있는지 확인

## 사용법

### 1. 프론트엔드에서 알림 조회
```javascript
// 주기적으로 알림 개수 확인 (30초마다)
setInterval(async () => {
    try {
        const response = await fetch('/api/alarm/count', {
            credentials: 'include'
        });
        const data = await response.json();
        
        if (data.success && data.data > 0) {
            // 새로운 알림이 있으면 알림 표시
            showNotification(data.data);
        }
    } catch (error) {
        console.error('알림 개수 조회 실패:', error);
    }
}, 30000);

// 알림 목록 조회
async function getAlarmList() {
    const response = await fetch('/api/alarm/list', {
        credentials: 'include'
    });
    const data = await response.json();
    return data.data;
}
```

### 2. 알림 읽음 처리
```javascript
async function markAsRead(alarmId) {
    await fetch(`/api/alarm/${alarmId}/read`, {
        method: 'PUT',
        credentials: 'include'
    });
}
```

## 로그인 시 알림 체크 설정

### 계약 만료 알림
- **시점**: 사용자 로그인 시
- **조건**: 해당 사용자의 계약 종료일이 30일, 7일, 1일 전인 매물
- **쿼리**: `myhome_tbl`에서 `member_id`와 `contract_end` 기준
- **중복 방지**: 같은 날 같은 타입의 알림은 중복 발송되지 않음

### 시세 변화 알림
- **시점**: 사용자 로그인 시
- **조건**: 해당 사용자의 관심 지역의 시세 변화율이 5% 이상
- **쿼리**: `wishlist_tbl`과 `transactions_tbl` 조인 (사용자별 필터링)

## 알림 타입

1. **계약 만료 알림 (type=1)**
   - 30일 전: "등록하신 매물 'OOO'의 계약이 30일 후 만료됩니다."
   - 7일 전: "등록하신 매물 'OOO'의 계약이 7일 후 만료됩니다."
   - 1일 전: "등록하신 매물 'OOO'의 계약이 1일 후 만료됩니다."

2. **시세 변화 알림 (type=2)**
   - "관심 지역 'OOO'의 시세가 변화율: 5.2%"

## 모니터링 및 로깅

### 로그 레벨
- **INFO**: 스케줄러 실행, 알림 생성 성공
- **WARN**: 알림 생성 실패, 데이터 없음
- **ERROR**: 스케줄러 오류, 데이터베이스 오류

### 모니터링 지표
- 스케줄러 실행 상태
- 알림 생성 성공/실패율
- 데이터베이스 연결 상태

## 트러블슈팅

### 일반적인 문제

1. **알림이 생성되지 않음**
   - 스케줄러 실행 상태 확인
   - 데이터베이스 연결 확인
   - 관련 테이블 데이터 존재 여부 확인

2. **스케줄러가 실행되지 않음**
   - `@EnableScheduling` 어노테이션 확인
   - 애플리케이션 로그에서 스케줄러 실행 로그 확인

3. **알림 설정이 적용되지 않음**
   - `alarm_settings_tbl` 테이블 데이터 확인
   - 사용자별 알림 설정 데이터 존재 여부 확인

## 성능 최적화

1. **인덱싱**: 자주 조회되는 컬럼에 인덱스 추가
2. **배치 처리**: 대량 알림 생성 시 배치 처리
3. **캐싱**: 자주 조회되는 데이터 캐싱
4. **쿼리 최적화**: 복잡한 조인 쿼리 최적화

## 확장 가능성

1. **다중 채널 지원**: 이메일, SMS, 푸시 알림 추가
2. **알림 템플릿**: 다양한 알림 템플릿 지원
3. **실시간 통신**: WebSocket이나 SSE로 실시간 알림 전송
4. **알림 우선순위**: 중요도에 따른 알림 우선순위 설정 