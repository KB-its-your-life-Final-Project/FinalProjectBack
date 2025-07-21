drop table if exists safe_report_unified;
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
