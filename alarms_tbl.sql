-- 알림 테이블 생성
CREATE TABLE IF NOT EXISTS alarms_tbl (
    alarm_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    type INT NOT NULL COMMENT '1: 계약만료 30일전, 2: 계약만료 7일전',
    text TEXT NOT NULL COMMENT '알림 내용',
    time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '알림 생성 시간',
    is_checked INT NOT NULL DEFAULT 0 COMMENT '0: 미확인, 1: 확인',
    INDEX idx_user_id (user_id),
    INDEX idx_type (type),
    INDEX idx_time (time),
    INDEX idx_is_checked (is_checked)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='알림 테이블'; 