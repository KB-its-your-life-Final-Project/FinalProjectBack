# FinalProjectBack
___

## 사용 언어 및 기술

- **언어**: Java 17
- **빌드 툴**: Gradle (Wrapper 사용 권장)

- **웹 프레임워크**: Spring Framework 5.3.38
  - Spring MVC (웹 개발)
  - Spring TX (트랜잭션 관리)
  - Spring JDBC (JdbcTemplate 지원)
  - Spring Context Support (메일, 스케줄링 등 부가 기능)

- **보안**:
  - Spring Security 5.8.13
  - JWT (jjwt 0.11.5) — 인증 및 인가용 JSON Web Token 처리

- **데이터베이스**:
  - MySQL 8.4.0
  - HikariCP 2.7.4 (고성능 커넥션 풀)

- **ORM / 매퍼**:
  - MyBatis 3.5.19, MyBatis-Spring 2.0.6 (SQL 매퍼 및 스프링 통합)
  - MapStruct 1.5.5 (컴파일 타임 타입 안전 DTO 매핑 자동 생성)

- **API 문서화**: Swagger 2.9.2 (Springfox)

- **HTTP 통신**:
  - Apache HttpClient 4.5.13, HttpCore 4.4.15 (REST API 호출용)

- **로깅**:
  - Logback 1.5.18 (기본 로깅 프레임워크)
  - log4jdbc-log4j2 1.16 (JDBC SQL 로그 출력)

- **의존성 주입**:
  - javax.inject 1 (표준 DI 어노테이션 지원)

- **AOP**:
  - AspectJ 1.9.20 (관점 지향 프로그래밍 지원)

- **JSON 처리**:
  - Jackson Databind 2.15.2 (JSON 직렬화/역직렬화)
  - Jackson Datatype JSR310 (Java 8 Date/Time 지원)

- **XML 처리**:
  - Jackson-dataformat-xml 2.15.2 (XML 직렬화/역직렬화)
  - Xerces 2.12.2 (XML 파서)

- **메일 관련**:
  - javax.mail 1.4.7, com.sun.mail:jakarta.mail 1.6.7, javax.activation 1.1.1 (메일 전송 및 첨부 파일 지원)
  - Spring Context Support (메일 및 스케줄링 관련 빈 지원)

- **테스트**:
  - JUnit 5.11.0
  - Spring Test 5.3.38 (통합 테스트 지원)
  - Mockito 5.11.0 (모킹 라이브러리)
  - AssertJ 3.24.2 (테스트 어설션 유틸)

- **코드 자동 생성 및 보조**:
  - Lombok 1.18.30 (getter/setter, 빌더 등 자동 생성)
  - Lombok MapStruct Binding (MapStruct와 Lombok 연동 지원)

- **환경 변수 관리**:
  - dotenv-java 3.0.0 (.env 파일 기반 환경변수 관리)

- **기타**:
  - easycodef-java 1.0.6 (CODEF API 연동용 라이브러리)

---


## 디렉토리 구조
```aiignore
java/com/lighthouse/
├── common/       # 공통 유틸리티 및 상수 정의
├── config/       # 스프링 설정 전반(RootConfig, ServletConfig, SwaggerConfig 등)
│   ├── RootConfig.java    # 데이터베이스, MyBatis, 트랜잭션 등 핵심 Bean 설정. Service, Mapper, Repository Bean 등록
│   ├── ServletConfig.java # 웹 관련 MVC 설정 및 정적 리소스 핸들링. Contoroller Bean 등록
│   ├── WebConfig.java     # DispatcherServlet 초기화 및 매핑 설정
│   └── SwaggerConfig.java # Swagger API 문서 자동 생성 설정
├── security/     # 보안 관련 설정 및 클래스
├── exception/    # 예외 처리 관련 클래스
└── domain/ # 도메인별 기능 패키지. 기능에 따라 이름 정의
  ├── controller/ # REST API 컨트롤러
  ├── converter/ # DTO <-> Entity 간의 전환 지원
  ├── service/ # 비즈니스 로직 서비스
  ├── mapper/ # MyBatis 매퍼 인터페이스
  ├── dto/ # 데이터 전송 객체 (DTO)
  └── entity/ # 값 객체 (Entity)

resources/            # 리소스 파일
├── com/lighthouse/   # MyBatis 매퍼 XML 파일 (mapper 인터페이스 경로와 일치)
├── application-local.properties  # 로컬 개발용 설정 파일. profiles=local 시 사용
├── application-prod.properties   # 프로덕션용 설정 파일. profiles=prod 시 사용
├── logback.xml                    # 로깅 설정
├── mybatis-config.xml             # MyBatis 설정
└── log4jdbc.log4j2.properties    # JDBC 로깅 설정
```
---
## 환경 설정

### JDK 버전
- JDK 17 이상

### 프로파일 설정
- 애플리케이션 실행 전 .env 파일에 `SPRING_PROFILE_ACTIVE={local, prod}` 설정
  - src/main/resources/application-local.properties : 개발 환경용 설정 
  - src/main/resources/application-prod.properties : 운영 환경용 설정
  - .env 포함 내용
    - 프로파일 설정값(local | prod)
    - database driver,username,password,url 
    - jwt secret 키
    - 각 API 키값
    - 계정, 비밀번호