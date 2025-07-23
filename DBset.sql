DROP TABLE IF EXISTS all_real_estate;

CREATE TABLE all_real_estate (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '기본 키',
                                 sgg_cd INT NOT NULL COMMENT '지역코드',
                                 sgg_nm VARCHAR(100) COMMENT '시군구',
                                 umd_nm VARCHAR(100) NOT NULL COMMENT '법정동',
                                 jibun VARCHAR(100) COMMENT '지번',
                                 building_name VARCHAR(100) COMMENT '건물명',
                                 mhouse_type VARCHAR(100) COMMENT '연립/다세대 유형',
                                 shouse_type VARCHAR(100) COMMENT '다가구/단독 유형',
                                 build_year INT NOT NULL COMMENT '건축년도',
                                 deal_year INT NOT NULL COMMENT '계약년도',
                                 deal_month INT NOT NULL COMMENT '계약월',
                                 deal_day INT NOT NULL COMMENT '계약일',
                                 deal_amount BIGINT COMMENT '거래금액(만원)',
                                 deposit BIGINT DEFAULT NULL COMMENT '보증금액(만원)',
                                 monthly_rent BIGINT DEFAULT NULL COMMENT '월세금액(만원)',
                                 trade_type INT NULL COMMENT '거래유형(1:매매, 2:전월세)',
                                 building_type INT NULL COMMENT '건물유형(1:아파트, 2:오피스텔, 3:연립, 4:단독)',
                                 source_table INT NOT NULL COMMENT '원본 테이블(1: apartment_trade_0715,
2: apartment_rental_0715,
3: officetel_trade_0715,
4: officetel_rental_0715,
5: multihouse_trade_0715,
6: multihouse_rental_0715,
7: singlehouse_trade_0715,
8: singlehouse_rental_0715',
                                 original_id BIGINT NOT NULL COMMENT '원본 테이블에서의 id'
);

INSERT INTO all_real_estate (
    sgg_cd, sgg_nm, umd_nm, jibun,
    building_name, mhouse_type, shouse_type,
    build_year, deal_year, deal_month, deal_day,
    deal_amount, deposit, monthly_rent,
    trade_type, building_type,source_table, original_id
)

-- 1. 아파트 매매 (building_type = 1, trade_type = 1)
SELECT
    sgg_cd, NULL AS sgg_nm, umd_nm, jibun,
    apt_nm, NULL, NULL,
    build_year, deal_year, deal_month, deal_day,
    REPLACE(deal_amount, ',', '') + 0, NULL, NULL,
    1, 1, 1 AS source_table, id
FROM api_apartment_trade_0715

UNION ALL

-- 2. 아파트 전월세 (building_type = 1, trade_type = 2)
SELECT
    sgg_cd, NULL, umd_nm, jibun,
    apt_nm, NULL, NULL,
    build_year, deal_year, deal_month, deal_day,
    NULL, REPLACE(deposit, ',', '') + 0, REPLACE(monthly_rent, ',', '') + 0,
    2, 1, 2 AS source_table, id
FROM api_apartment_rental_0715

UNION ALL

-- 3. 오피스텔 매매 (building_type = 2, trade_type = 1)
SELECT
    sgg_cd, sgg_nm, umd_nm, jibun,
    offi_nm, NULL, NULL,
    build_year, deal_year, deal_month, deal_day,
    REPLACE(deal_amount, ',', '') + 0, NULL, NULL,
    1, 2, 3 AS source_table, id
FROM api_officetel_trade_0715

UNION ALL

-- 4. 오피스텔 전월세 (building_type = 2, trade_type = 2)
SELECT
    sgg_cd,  sgg_nm, umd_nm, jibun,
    offi_nm, NULL, NULL,
    build_year, deal_year, deal_month, deal_day,
    NULL, REPLACE(deposit, ',', '') + 0, REPLACE(monthly_rent, ',', '') + 0,
    2, 2, 4 AS source_table, id
FROM api_officetel_rental_0715

UNION ALL

-- 5. 연립/다세대 매매 (building_type = 3, trade_type = 1)
SELECT
    sgg_cd,  NULL, umd_nm, jibun,
    mhouse_nm, house_type, NULL,
    build_year, deal_year, deal_month, deal_day,
    REPLACE(deal_amount, ',', '') + 0, NULL, NULL,
    1, 3, 5 AS source_table, id
FROM api_multihouse_trade_0715

UNION ALL

-- 6. 연립/다세대 전월세 (building_type = 3, trade_type = 2)
SELECT
    sgg_cd,  NULL, umd_nm, jibun,
    mhouse_nm, house_type, NULL,
    build_year, deal_year, deal_month, deal_day,
    NULL, REPLACE(deposit, ',', '') + 0, REPLACE(monthly_rent, ',', '') + 0,
    2, 3, 6 AS source_table, id
FROM api_multihouse_rental_0715

UNION ALL

-- 7. 단독/다가구 매매 (building_type = 4, trade_type = 1)
SELECT
    sgg_cd,  NULL, umd_nm, jibun,
    NULL, NULL, house_type,
    build_year, deal_year, deal_month, deal_day,
    REPLACE(deal_amount, ',', '') + 0, NULL, NULL,
    1, 4, 7 AS source_table, id
FROM api_singlehouse_trade_0715

UNION ALL

-- 8. 단독/다가구 전월세 (building_type = 4, trade_type = 2)
SELECT
    sgg_cd,  NULL, umd_nm,
    NULL, -- 주의: 원래 NULL 처리된다고 하셨지만, jibun 컬럼 있으면 그대로 사용 가능
    NULL, NULL, house_type,
    build_year, deal_year, deal_month, deal_day,
    NULL, REPLACE(deposit, ',', '') + 0, REPLACE(monthly_rent, ',', '') + 0,
    2, 4, 8 AS source_table, id
FROM api_singlehouse_rental_0715;

ALTER TABLE all_real_estate
    ADD COLUMN jibun_addr VARCHAR(200) COMMENT '지번 주소';

UPDATE all_real_estate
SET jibun_addr = CONCAT(umd_nm, ' ', COALESCE(jibun, ''));

# 위도, 경도 속성 추가하기
ALTER TABLE all_real_estate
    ADD COLUMN latitude DOUBLE COMMENT '위도',
    ADD COLUMN longitude DOUBLE COMMENT '경도';

ALTER TABLE all_real_estate
    MODIFY COLUMN source_table BIGINT COMMENT '원본 테이블(1: api_apartment_trade_0715,
2: api_apartment_rental_0715,
3: api_officetel_trade_0715,
4: api_officetel_rental_0715,
5: api_multihouse_trade_0715,
6: api_multihouse_rental_0715,
7: api_singlehouse_trade_0715,
8: api_singlehouse_rental_0715';
