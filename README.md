# FinalProjectBack
___

## 사용 언어 및 기술
- 언어: Java 17
- 빌드 툴: Gradle
- 웹 프레임워크: Spring Framework 5.3.38 (Spring MVC, Spring TX, Spring JDBC)
- 보안: Spring Security 5.8.13, JWT (jjwt 0.11.5)
- 데이터베이스: MySQL 8.4.0, HikariCP 2.7.4 (커넥션 풀)
- ORM / 매퍼: MyBatis 3.5.19, MyBatis-Spring 2.0.6
- API 문서화: Swagger 2.9.2 (Springfox)
- HTTP 통신: Apache HttpClient 4.5.13, HttpCore 4.4.15
- 로깅: Logback 1.5.18, log4jdbc-log4j2 1.16 (SQL 로그)
- 의존성 주입: javax.inject 1
- AOP: AspectJ 1.9.20
- JSON 자동 매핑: Jackson 2.15.2
- XML 자동 매핑: Jackson-dataformat-xml 2.15.2
- XML 처리 엔진: Xerces 2.12.2
- 테스트: JUnit 5.11.0, Spring Test 5.3.38
- 코드 자동 생성 및 보조: Lombok 1.18.30
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
  ├── service/ # 비즈니스 로직 서비스
  ├── mapper/ # MyBatis 매퍼 인터페이스
  ├── dto/ # 데이터 전송 객체 (DTO)
  └── vo/ # 값 객체 (VO)

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
- JDK 17 이상 권장

### 프로파일 설정
- 애플리케이션 실행 시 JVM 옵션에 `-Dspring.profiles.active={local, prod}` 작성
  - Intellij의 경우 실행/디버그 구성 -> 구성편집 -> VM 옵션에 작성
  - src/main/resources/application-local.properties : 개발 환경용 설정 
  - src/main/resources/application-prod.properties : 운영 환경용 설정
  - properties 포함 내용 : database driver,username,password,url / jwt secret 키