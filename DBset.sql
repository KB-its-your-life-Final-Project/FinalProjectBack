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


# 토지대장 관련 데이터 저장하기
# building_registry 테이블은 건물에 대한 정보

DROP TABLE IF EXISTS building_registry;
CREATE TABLE building_registry (
                                 id BIGINT PRIMARY KEY COMMENT '기본 키',
                                 type CHAR(2) NOT NULL COMMENT '일반/집합',
                                 res_addr_dong VARCHAR(100) COMMENT '동명',
                                 res_number VARCHAR(100) COMMENT '호수 (매수)',
                                 res_user_addr VARCHAR(100) COMMENT '시군구법정동',
                                 comm_addr_lot_number VARCHAR(100) COMMENT '지번',
                                 comm_addr_road_name VARCHAR(100) COMMENT '도로명 주소',
                                 res_violation_status VARCHAR(50) COMMENT '위반 건축물인 경우 "위만 건축물"라고 제공',
                                 req_dong VARCHAR(30) COMMENT '요청한 동',
                                 req_ho VARCHAR(30) COMMENT 'N호/N가구/N세대'
);

INSERT INTO building_registry (
    id, type, res_addr_dong, res_number, res_user_addr,
    comm_addr_lot_number, comm_addr_road_name, res_violation_status,
    req_dong, req_ho
)

SELECT
    id, type, res_addr_dong, res_number, res_user_addr,
    comm_addr_lot_number, comm_addr_road_name, res_violation_status,
    req_dong, req_ho
FROM api_building_register;

# building_registry_for 는 용도에 대한 정보
DROP TABLE IF EXISTS building_registry_use_for;
CREATE TABLE building_registry_use_for(
                                      id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '기본 키',
                                      register_id BIGINT NOT NULL COMMENT 'building_registry에서의 인덱스',
                                      res_use_type VARCHAR(100) COMMENT '용도'
);

INSERT INTO building_registry_use_for(
                                  register_id, res_use_type
)
SELECT register_id, res_use_type
FROM api_building_register_building_status;

# 지번 속성 추가하기
ALTER TABLE api_building_register
    ADD COLUMN jibun_addr VARCHAR(200) COMMENT '지번 주소';

UPDATE api_building_register
SET jibun_addr = CONCAT(res_user_addr, ' ', COALESCE(comm_addr_lot_number, ''));

# 위도, 경도 속성 추가하기
ALTER TABLE api_building_register
    ADD COLUMN latitude DOUBLE COMMENT '위도',
    ADD COLUMN longitude DOUBLE COMMENT '경도';


DELETE FROM api_building_register WHERE id = 37;
DELETE FROM api_building_register_building_status
WHERE register_id=37;
DELETE FROM api_building_register_auth_status WHERE ID=37;
DELETE FROM api_building_register_change WHERE ID=37;
DELETE FROM api_building_register_detail WHERE ID=37;
DELETE FROM api_building_register_license_class WHERE ID=37;
DELETE FROM api_building_register_owner WHERE ID=37;
DELETE FROM api_building_register_parking_lot_status WHERE ID=37;

ALTER TABLE api_building_register DROP COLUMN jibun_addr;

SELECT * FROM estate_api_integration_tbl WHERE latitude="자양동 127-7";

# UNIQUE KEY 설정
ALTER TABLE `estate_api_integration_tbl`
ADD CONSTRAINT unique_combination
UNIQUE (mhouse_type, shouse_type, build_year, building_type, jibun_addr);

DROP TABLE IF EXISTS safe_report_tbl;
CREATE TABLE safe_report_tbl (
                                    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '기본 키',
                                    user_id INT NOT NULL COMMENT '조회한 사용자',
                                    estate_id INT NOT NULL COMMENT 'estate_api_integration_tbl의 id',
                                    budget INT NOT NULL COMMENT '예산',
                                    result_grade VARCHAR(50) NOT NULL COMMENT '레포트 결과 등급',
                                    is_delete TINYINT(1) DEFAULT 0 COMMENT '삭제 여부 (0: 활성, 1: 삭제)',
                                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '조회 날짜',
                                    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 날짜',

    -- 인덱스
                                    INDEX idx_user_created (user_id, created_at DESC),
                                    INDEX idx_user_delete (user_id, is_delete),
                                    INDEX idx_estate (estate_id)
);

DROP TABLE IF EXISTS sido;
-- 시/도 테이블
CREATE TABLE sido (
                      sido_cd VARCHAR(2) PRIMARY KEY,    -- 시/도 코드 (11, 26, 27, 28, 29, 30, 31, 36, 41, 42, 43, 44, 45, 46, 47, 48, 50)
                      sido_nm VARCHAR(20) NOT NULL       -- 시/도 이름 (서울, 부산, 대구, 인천, 광주, 대전, 울산, 세종, 경기, 강원, 충북, 충남, 전북, 전남, 경북, 경남, 제주)
);

-- 시/군/구 테이블
CREATE TABLE sigugun (
                         sigugun_cd VARCHAR(5) PRIMARY KEY, -- 시/군/구 코드 (11010, 11020, 11030, ...)
                         sido_cd VARCHAR(2) NOT NULL,       -- 시/도 코드 (FK)
                         sigugun_nm VARCHAR(20) NOT NULL,   -- 시/군/구 이름 (종로구, 중구, 용산구, ...)
                         FOREIGN KEY (sido_cd) REFERENCES sido(sido_cd)
);

-- 읍/면/동 테이블
CREATE TABLE dong (
                      dong_cd VARCHAR(10) PRIMARY KEY,   -- 읍/면/동 코드
                      sigugun_cd VARCHAR(5) NOT NULL,    -- 시/군/구 코드 (FK)
                      dong_nm VARCHAR(20) NOT NULL,      -- 읍/면/동 이름 (종로1가, 종로2가, ...)
                      FOREIGN KEY (sigugun_cd) REFERENCES sigugun(sigugun_cd)
);

INSERT INTO sido VALUES
                     ('11', '서울'),
                     ('26', '부산'),
                     ('27', '대구'),
                     ('28', '인천'),
                     ('29', '광주'),
                     ('30', '대전'),
                     ('31', '울산'),
                     ('36', '세종'),
                     ('41', '경기'),
                     ('42', '강원'),
                     ('43', '충북'),
                     ('44', '충남'),
                     ('45', '전북'),
                     ('46', '전남'),
                     ('47', '경북'),
                     ('48', '경남'),
                     ('50', '제주');

INSERT INTO sigugun (sigugun_cd, sido_cd, sigugun_nm) VALUES
                                                          ('11010', '11', '종로구'),
                                                          ('11020', '11', '중구'),
                                                          ('11030', '11', '용산구'),
                                                          ('11040', '11', '성동구'),
                                                          ('11050', '11', '광진구'),
                                                          ('11060', '11', '동대문구'),
                                                          ('11070', '11', '중랑구'),
                                                          ('11080', '11', '성북구'),
                                                          ('11090', '11', '강북구'),
                                                          ('11100', '11', '도봉구'),
                                                          ('11110', '11', '노원구'),
                                                          ('11120', '11', '은평구'),
                                                          ('11130', '11', '서대문구'),
                                                          ('11140', '11', '마포구'),
                                                          ('11150', '11', '양천구'),
                                                          ('11160', '11', '강서구'),
                                                          ('11170', '11', '구로구'),
                                                          ('11180', '11', '금천구'),
                                                          ('11190', '11', '영등포구'),
                                                          ('11200', '11', '동작구'),
                                                          ('11210', '11', '관악구'),
                                                          ('11220', '11', '서초구'),
                                                          ('11230', '11', '강남구'),
                                                          ('11240', '11', '송파구'),
                                                          ('11250', '11', '강동구');

INSERT INTO sigugun (sigugun_cd, sido_cd, sigugun_nm) VALUES
                                                          ('26010', '26', '중구'),
                                                          ('26020', '26', '서구'),
                                                          ('26030', '26', '동구'),
                                                          ('26040', '26', '영도구'),
                                                          ('26050', '26', '부산진구'),
                                                          ('26060', '26', '동래구'),
                                                          ('26070', '26', '남구'),
                                                          ('26080', '26', '북구'),
                                                          ('26090', '26', '해운대구'),
                                                          ('26100', '26', '사하구'),
                                                          ('26110', '26', '금정구'),
                                                          ('26120', '26', '강서구'),
                                                          ('26130', '26', '연제구'),
                                                          ('26140', '26', '수영구'),
                                                          ('26150', '26', '사상구'),
                                                          ('26710', '26', '기장군');

INSERT INTO sigugun (sigugun_cd, sido_cd, sigugun_nm) VALUES
                                                          ('27010', '27', '중구'),
                                                          ('27020', '27', '동구'),
                                                          ('27030', '27', '서구'),
                                                          ('27040', '27', '남구'),
                                                          ('27050', '27', '북구'),
                                                          ('27060', '27', '수성구'),
                                                          ('27070', '27', '달서구'),
                                                          ('27710', '27', '달성군');

INSERT INTO sigugun (sigugun_cd, sido_cd, sigugun_nm) VALUES
                                                          ('28010', '28', '중구'),
                                                          ('28020', '28', '동구'),
                                                          ('28030', '28', '미추홀구'),
                                                          ('28040', '28', '연수구'),
                                                          ('28050', '28', '남동구'),
                                                          ('28060', '28', '부평구'),
                                                          ('28070', '28', '계양구'),
                                                          ('28080', '28', '서구'),
                                                          ('28710', '28', '강화군'),
                                                          ('28720', '28', '옹진군');

INSERT INTO sigugun (sigugun_cd, sido_cd, sigugun_nm) VALUES
                                                          ('29010', '29', '동구'),
                                                          ('29020', '29', '서구'),
                                                          ('29030', '29', '남구'),
                                                          ('29040', '29', '북구'),
                                                          ('29050', '29', '광산구');

INSERT INTO sigugun (sigugun_cd, sido_cd, sigugun_nm) VALUES
                                                          ('30010', '30', '동구'),
                                                          ('30020', '30', '중구'),
                                                          ('30030', '30', '서구'),
                                                          ('30040', '30', '유성구'),
                                                          ('30050', '30', '대덕구');

INSERT INTO sigugun (sigugun_cd, sido_cd, sigugun_nm) VALUES
                                                          ('31010', '31', '중구'),
                                                          ('31020', '31', '남구'),
                                                          ('31030', '31', '동구'),
                                                          ('31040', '31', '북구'),
                                                          ('31710', '31', '울주군');

INSERT INTO sigugun (sigugun_cd, sido_cd, sigugun_nm) VALUES
    ('36000', '36', '세종특별자치시');

INSERT INTO sigugun (sigugun_cd, sido_cd, sigugun_nm) VALUES
                                                          ('41110', '41', '수원시'),
                                                          ('41111', '41', '수원시 장안구'),
                                                          ('41113', '41', '수원시 권선구'),
                                                          ('41115', '41', '수원시 팔달구'),
                                                          ('41117', '41', '수원시 영통구'),
                                                          ('41130', '41', '성남시'),
                                                          ('41131', '41', '성남시 수정구'),
                                                          ('41133', '41', '성남시 중원구'),
                                                          ('41135', '41', '성남시 분당구'),
                                                          ('41150', '41', '의정부시'),
                                                          ('41170', '41', '안양시'),
                                                          ('41171', '41', '안양시 만안구'),
                                                          ('41173', '41', '안양시 동안구'),
                                                          ('41190', '41', '부천시'),
                                                          ('41210', '41', '광명시'),
                                                          ('41220', '41', '평택시'),
                                                          ('41250', '41', '동두천시'),
                                                          ('41270', '41', '안산시'),
                                                          ('41271', '41', '안산시 상록구'),
                                                          ('41273', '41', '안산시 단원구'),
                                                          ('41280', '41', '고양시'),
                                                          ('41281', '41', '고양시 덕양구'),
                                                          ('41285', '41', '고양시 일산동구'),
                                                          ('41287', '41', '고양시 일산서구'),
                                                          ('41290', '41', '과천시'),
                                                          ('41310', '41', '구리시'),
                                                          ('41360', '41', '남양주시'),
                                                          ('41370', '41', '오산시'),
                                                          ('41390', '41', '시흥시'),
                                                          ('41410', '41', '군포시'),
                                                          ('41430', '41', '의왕시'),
                                                          ('41450', '41', '하남시'),
                                                          ('41460', '41', '용인시'),
                                                          ('41461', '41', '용인시 처인구'),
                                                          ('41463', '41', '용인시 기흥구'),
                                                          ('41465', '41', '용인시 수지구'),
                                                          ('41480', '41', '파주시'),
                                                          ('41500', '41', '이천시'),
                                                          ('41550', '41', '안성시'),
                                                          ('41570', '41', '김포시'),
                                                          ('41590', '41', '화성시'),
                                                          ('41610', '41', '광주시'),
                                                          ('41630', '41', '여주시'),
                                                          ('41800', '41', '양평군'),
                                                          ('41820', '41', '고양군'),
                                                          ('41830', '41', '연천군'),
                                                          ('41850', '41', '가평군'),
                                                          ('41860', '41', '포천군');

INSERT INTO sigugun (sigugun_cd, sido_cd, sigugun_nm) VALUES
                                                          ('42110', '42', '춘천시'),
                                                          ('42130', '42', '원주시'),
                                                          ('42150', '42', '강릉시'),
                                                          ('42170', '42', '동해시'),
                                                          ('42190', '42', '태백시'),
                                                          ('42210', '42', '속초시'),
                                                          ('42230', '42', '삼척시'),
                                                          ('42720', '42', '홍천군'),
                                                          ('42730', '42', '횡성군'),
                                                          ('42750', '42', '영월군'),
                                                          ('42760', '42', '평창군'),
                                                          ('42770', '42', '정선군'),
                                                          ('42780', '42', '철원군'),
                                                          ('42790', '42', '화천군'),
                                                          ('42800', '42', '양구군'),
                                                          ('42810', '42', '인제군'),
                                                          ('42820', '42', '고성군'),
                                                          ('42830', '42', '양양군');

INSERT INTO sigugun (sigugun_cd, sido_cd, sigugun_nm) VALUES
                                                          ('43110', '43', '청주시'),
                                                          ('43111', '43', '청주시 상당구'),
                                                          ('43112', '43', '청주시 서원구'),
                                                          ('43113', '43', '청주시 흥덕구'),
                                                          ('43114', '43', '청주시 청원구'),
                                                          ('43130', '43', '충주시'),
                                                          ('43150', '43', '제천시'),
                                                          ('43720', '43', '보은군'),
                                                          ('43730', '43', '옥천군'),
                                                          ('43740', '43', '영동군'),
                                                          ('43745', '43', '증평군'),
                                                          ('43750', '43', '진천군'),
                                                          ('43760', '43', '괴산군'),
                                                          ('43770', '43', '음성군'),
                                                          ('43800', '43', '단양군');

INSERT INTO sigugun (sigugun_cd, sido_cd, sigugun_nm) VALUES
                                                          ('44130', '44', '천안시'),
                                                          ('44131', '44', '천안시 동남구'),
                                                          ('44133', '44', '천안시 서북구'),
                                                          ('44150', '44', '공주시'),
                                                          ('44180', '44', '보령시'),
                                                          ('44200', '44', '아산시'),
                                                          ('44210', '44', '서산시'),
                                                          ('44230', '44', '논산시'),
                                                          ('44250', '44', '계룡시'),
                                                          ('44270', '44', '당진시'),
                                                          ('44710', '44', '금산군'),
                                                          ('44760', '44', '부여군'),
                                                          ('44770', '44', '서천군'),
                                                          ('44790', '44', '청양군'),
                                                          ('44800', '44', '홍성군'),
                                                          ('44810', '44', '예산군'),
                                                          ('44825', '44', '태안군');

INSERT INTO sigugun (sigugun_cd, sido_cd, sigugun_nm) VALUES
                                                          ('45110', '45', '전주시'),
                                                          ('45111', '45', '전주시 완산구'),
                                                          ('45113', '45', '전주시 덕진구'),
                                                          ('45130', '45', '군산시'),
                                                          ('45140', '45', '익산시'),
                                                          ('45180', '45', '정읍시'),
                                                          ('45190', '45', '남원시'),
                                                          ('45210', '45', '김제시'),
                                                          ('45710', '45', '완주군'),
                                                          ('45720', '45', '진안군'),
                                                          ('45730', '45', '무주군'),
                                                          ('45740', '45', '장수군'),
                                                          ('45750', '45', '임실군'),
                                                          ('45760', '45', '순창군'),
                                                          ('45770', '45', '고창군'),
                                                          ('45790', '45', '부안군');

INSERT INTO sigugun (sigugun_cd, sido_cd, sigugun_nm) VALUES
                                                          ('46110', '46', '목포시'),
                                                          ('46130', '46', '여수시'),
                                                          ('46150', '46', '순천시'),
                                                          ('46170', '46', '나주시'),
                                                          ('46230', '46', '광양시'),
                                                          ('46710', '46', '담양군'),
                                                          ('46720', '46', '곡성군'),
                                                          ('46730', '46', '구례군'),
                                                          ('46770', '46', '고흥군'),
                                                          ('46780', '46', '보성군'),
                                                          ('46790', '46', '화순군'),
                                                          ('46800', '46', '장흥군'),
                                                          ('46810', '46', '강진군'),
                                                          ('46820', '46', '해남군'),
                                                          ('46830', '46', '영암군'),
                                                          ('46840', '46', '무안군'),
                                                          ('46860', '46', '함평군'),
                                                          ('46870', '46', '영광군'),
                                                          ('46880', '46', '장성군'),
                                                          ('46890', '46', '완도군'),
                                                          ('46900', '46', '진도군'),
                                                          ('46910', '46', '신안군');

INSERT INTO sigugun (sigugun_cd, sido_cd, sigugun_nm) VALUES
                                                          ('47110', '47', '포항시'),
                                                          ('47111', '47', '포항시 남구'),
                                                          ('47113', '47', '포항시 북구'),
                                                          ('47130', '47', '경주시'),
                                                          ('47150', '47', '김천시'),
                                                          ('47170', '47', '안동시'),
                                                          ('47190', '47', '구미시'),
                                                          ('47210', '47', '영주시'),
                                                          ('47230', '47', '영천시'),
                                                          ('47250', '47', '상주시'),
                                                          ('47280', '47', '문경시'),
                                                          ('47290', '47', '경산시'),
                                                          ('47720', '47', '군위군'),
                                                          ('47730', '47', '의성군'),
                                                          ('47750', '47', '청송군'),
                                                          ('47760', '47', '영양군'),
                                                          ('47770', '47', '영덕군'),
                                                          ('47780', '47', '청도군'),
                                                          ('47790', '47', '고령군'),
                                                          ('47800', '47', '성주군'),
                                                          ('47820', '47', '칠곡군'),
                                                          ('47830', '47', '예천군'),
                                                          ('47840', '47', '봉화군'),
                                                          ('47850', '47', '울진군'),
                                                          ('47900', '47', '울릉군');

INSERT INTO sigugun (sigugun_cd, sido_cd, sigugun_nm) VALUES
                                                          ('48120', '48', '창원시'),
                                                          ('48121', '48', '창원시 의창구'),
                                                          ('48123', '48', '창원시 성산구'),
                                                          ('48125', '48', '창원시 마산합포구'),
                                                          ('48127', '48', '창원시 마산회원구'),
                                                          ('48129', '48', '창원시 진해구'),
                                                          ('48170', '48', '진주시'),
                                                          ('48220', '48', '통영시'),
                                                          ('48240', '48', '사천시'),
                                                          ('48250', '48', '김해시'),
                                                          ('48270', '48', '밀양시'),
                                                          ('48310', '48', '거제시'),
                                                          ('48330', '48', '양산시'),
                                                          ('48720', '48', '의령군'),
                                                          ('48730', '48', '함안군'),
                                                          ('48740', '48', '창녕군'),
                                                          ('48820', '48', '고성군'),
                                                          ('48840', '48', '남해군'),
                                                          ('48850', '48', '하동군'),
                                                          ('48860', '48', '산청군'),
                                                          ('48870', '48', '함양군'),
                                                          ('48880', '48', '거창군'),
                                                          ('48890', '48', '합천군');

INSERT INTO sigugun (sigugun_cd, sido_cd, sigugun_nm) VALUES
                                                          ('50110', '50', '제주시'),
                                                          ('50130', '50', '서귀포시');