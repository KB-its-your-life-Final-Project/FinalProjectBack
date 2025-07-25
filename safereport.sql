# 안심 레포트용 아파트 실거래가 생성
DROP TABLE IF EXISTS safe_report_apartment;
CREATE TABLE safe_report_apartment AS
    SELECT sgg_cd, umd_nm, jibun,apt_nm,apt_dong, build_year, deal_amount, deal_day, deal_month, deal_year
    FROM apartment_trade_0715;

# 안심 레포트용 다세대 실거래가 생성
DROP TABLE IF EXISTS safe_report_multihouse;
CREATE TABLE safe_report_multihouse AS
SELECT sgg_cd, umd_nm,jibun, mhouse_nm, build_year, deal_amount, deal_day, deal_month, deal_year
FROM multihouse_trade_0715;

# 안심 레포트용 오피스텔 실거래가 생성
DROP TABLE IF EXISTS safe_report_officetel;
CREATE TABLE safe_report_officetel AS
SELECT sgg_cd, sgg_nm, umd_nm,jibun, offi_nm, build_year, deal_amount, deal_day, deal_month, deal_year
FROM officetel_trade_0715;

# 안심 레포트용 다가구 실거래가 생성
DROP TABLE IF EXISTS safe_report_singlehouse;
CREATE TABLE safe_report_singlehouse AS
SELECT sgg_cd, umd_nm,jibun, build_year, deal_amount, deal_day, deal_month, deal_year
FROM singlehouse_trade_0715;

# 안심 레포트용 아파트 실거래가에 도로명주소 속성 추가
ALTER TABLE safe_report_apartment DROP COLUMN road_addr;
ALTER TABLE safe_report_apartment ADD COLUMN road_addr VARCHAR(200);
UPDATE safe_report_apartment
SET road_addr = CONCAT(umd_nm,'',jibun);

# 안심 레포트용 아파트 실거래가에 도로명주소 속성 추가
ALTER TABLE safe_report_multihouse DROP COLUMN road_addr;
ALTER TABLE safe_report_multihouse ADD COLUMN road_addr VARCHAR(200);
UPDATE safe_report_multihouse
SET road_addr = CONCAT(umd_nm,'',jibun);

SELECT umd_nm, jibun, road_addr
FROM safe_report_multihouse
LIMIT 3;
COMMIT;

# 위도, 경도 속성 추가하기
ALTER TABLE safe_report_apartment ADD COLUMN lat DOUBLE;
ALTER TABLE safe_report_apartment ADD COLUMN lng DOUBLE;


# 안심 레포트용 모든 건물 합치기
DROP TABLE IF EXISTS safe_report_unified;
#multihouse : 연립다세대 singlehouse : 단독/다가구
CREATE TABLE safe_report_unified (
                                     sgg_cd INTEGER COMMENT '지역코드',
                                     umd_nm VARCHAR(100) COMMENT '법정동',
                                     jibun VARCHAR(100) COMMENT '지번',
                                     build_year INTEGER COMMENT '건축년도',
                                     deal_year INTEGER COMMENT '계약년도',
                                     deal_month INTEGER COMMENT '계약월',
                                     deal_day INTEGER COMMENT '계약일',
                                     deal_amount BIGINT COMMENT '거래금액(만원)',
                                     trade_name VARCHAR(100) COMMENT '건물명',   -- 단지명/건물명/오피스텔명 등
                                     type_code INTEGER COMMENT '건물유형'   -- 'APT(1)', 'MULTIHOUSE(2)', 'OFFICETEL(3)', 'SINGLEHOUSE(4)'
);
# 아파트 테이블 -> 통합 테이블 삽입
INSERT INTO safe_report_unified (
    sgg_cd, umd_nm, jibun, build_year, deal_year,
    deal_month, deal_day, deal_amount, trade_name, type_code
)
SELECT
    sgg_cd, umd_nm, jibun, build_year, deal_year,
    deal_month, deal_day, CAST(REPLACE(deal_amount,',','') AS UNSIGNED) ,apt_nm AS trade_name, 1 AS type_code
FROM apartment_trade_0715;

# 다세대 주택 테이블 -> 통합 테이블 삽입
INSERT INTO safe_report_unified (
    sgg_cd, umd_nm, jibun, build_year, deal_year,
    deal_month, deal_day, deal_amount, trade_name, type_code
)
SELECT
    sgg_cd, umd_nm, jibun, build_year, deal_year,
    deal_month, deal_day, CAST(REPLACE(deal_amount,',','') AS UNSIGNED), mhouse_nm AS trade_name, 2 AS type_code
FROM multihouse_trade_0715;

# 오피스텔 -> 통합 테이블
INSERT INTO safe_report_unified (
    sgg_cd, umd_nm, jibun, build_year, deal_year,
    deal_month, deal_day, deal_amount, trade_name, type_code
)
SELECT
    sgg_cd, umd_nm, jibun, build_year, deal_year,
    deal_month, deal_day, CAST(REPLACE(deal_amount,',','') AS UNSIGNED), offi_nm AS trade_name, 3 AS type_code
FROM officetel_trade_0715;

INSERT INTO safe_report_unified (
    sgg_cd, umd_nm, jibun, build_year, deal_year,
    deal_month, deal_day, deal_amount, trade_name, type_code
)
SELECT
    sgg_cd, umd_nm, jibun, build_year, deal_year,
    deal_month, deal_day, CAST(REPLACE(deal_amount,',','') AS UNSIGNED), NULL AS trade_name, 4 AS type_code
FROM singlehouse_trade_0715;

ALTER TABLE safe_report_unified ADD FULLTEXT(trade_name);
