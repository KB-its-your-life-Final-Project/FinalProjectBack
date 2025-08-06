# 계약 만료 알림 시스템 구현

## 개요
`myhome_tbl`의 계약 기간(`contract_end`)을 기준으로 계약 만료 30일 전과 7일 전에 자동으로 알림을 발송하는 시스템을 구현했습니다.

## 구현된 기능

### 1. 자동 알림 발송
- **30일 전 알림**: 계약 만료 30일 전에 자동 발송
- **7일 전 알림**: 계약 만료 7일 전에 자동 발송
- **스케줄링**: 매일 자정에 실행 (`@Scheduled(cron = "0 0 0 * * ?")`)
- **중복 방지**: 같은 날 같은 타입의 알림은 중복 발송되지 않음

### 2. 알림 관리 기능
- **알림 목록 조회**: 사용자별 알림 목록 조회
- **알림 확인 처리**: 알림 읽음 처리
- **미확인 알림 개수**: 미확인 알림 개수 조회

### 3. 테스트 기능
- **수동 알림 발송**: 테스트를 위한 수동 알림 발송 API

## 파일 구조

### Entity
- `Alarms.java`: 알림 엔티티

### DTO
- `ContractExpirationAlarmDto.java`: 계약 만료 알림 DTO
- `AlarmResponseDto.java`: 알림 응답 DTO

### Mapper
- `AlarmMapper.java`: 알림 데이터베이스 접근 인터페이스
- `AlarmMapper.xml`: SQL 쿼리 정의

### Service
- `ContractExpirationAlarmService.java`: 계약 만료 알림 처리 서비스
- `AlarmService.java`: 일반 알림 관리 서비스

### Controller
- `AlarmController.java`: 알림 API 컨트롤러

### Config
- `SchedulingConfig.java`: 스케줄링 활성화 설정

## API 엔드포인트

### 1. 알림 목록 조회
```
GET /api/alarms
```

### 2. 알림 확인 처리
```
PUT /api/alarms/{alarmId}/check
```

### 3. 미확인 알림 개수 조회
```
GET /api/alarms/unchecked-count
```

### 4. 테스트 알림 발송
```
POST /api/alarms/test/contract-expiration
?userId=1&buildingName=테스트빌딩&contractEnd=2024-12-31&daysUntilExpiration=30
```

## 데이터베이스

### alarms_tbl 테이블
```sql
CREATE TABLE alarms_tbl (
    alarm_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    type INT NOT NULL COMMENT '1: 계약만료 30일전, 2: 계약만료 7일전',
    text TEXT NOT NULL COMMENT '알림 내용',
    time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_checked INT NOT NULL DEFAULT 0 COMMENT '0: 미확인, 1: 확인'
);
```

## 알림 메시지 형식
```
[계약 만료 알림] {건물명} {동호수}에 거주 중인 {건물명}의 계약이 {30일/7일} 전 만료됩니다. (2024-12-31)
```

## 설정 사항

### 1. 스케줄링 활성화
`@EnableScheduling` 어노테이션으로 스케줄링 기능을 활성화했습니다.

### 2. 알림 타입
- `type = 1`: 계약 만료 30일 전
- `type = 2`: 계약 만료 7일 전

### 3. 확인 상태
- `is_checked = 0`: 미확인
- `is_checked = 1`: 확인

## 사용 방법

### 1. 데이터베이스 설정
```sql
-- alarms_tbl 테이블 생성
source alarms_tbl.sql;
```

### 2. 애플리케이션 실행
- Spring Boot 애플리케이션을 실행하면 자동으로 스케줄링이 시작됩니다.
- 매일 자정에 계약 만료 알림이 자동으로 발송됩니다.

### 3. 테스트
```bash
# 테스트 알림 발송
curl -X POST "http://localhost:8080/api/alarms/test/contract-expiration?userId=1&buildingName=테스트빌딩&contractEnd=2024-12-31&daysUntilExpiration=30"
```

## 주의사항

1. **JWT 토큰 처리**: 현재 `getUserId()` 메서드는 하드코딩되어 있습니다. 실제 JWT 토큰에서 사용자 ID를 추출하는 로직으로 교체해야 합니다.

2. **에러 처리**: 각 단계별로 적절한 에러 처리가 되어 있습니다.

3. **로깅**: 모든 주요 작업에 대한 로깅이 추가되어 있습니다.

4. **트랜잭션**: 데이터베이스 작업에 `@Transactional` 어노테이션이 적용되어 있습니다. 