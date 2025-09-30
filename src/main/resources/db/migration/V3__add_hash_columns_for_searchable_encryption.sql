-- V4: 검색 가능한 암호화를 위한 해시 컬럼 추가
-- 하이브리드 암호화 방식: 원본은 AES-GCM으로 암호화, 검색용은 HMAC 해시

-- 컬럼 타입 변경 (JPA 엔티티 변경 사항 반영)

-- member 테이블: birthday 컬럼을 String(VARCHAR)으로 변경
ALTER TABLE member
MODIFY COLUMN birthday VARCHAR(10) NULL;

-- transaction 테이블: amount 컬럼을 String(VARCHAR)으로 변경
ALTER TABLE transaction
MODIFY COLUMN amount VARCHAR(255) NOT NULL;

-- Member 테이블에 해시 컬럼 추가
ALTER TABLE member
ADD COLUMN email_hash VARCHAR(255) NOT NULL DEFAULT '',
ADD COLUMN name_hash VARCHAR(255) NOT NULL DEFAULT '',
ADD COLUMN gender_hash VARCHAR(255) NULL,
ADD COLUMN birthday_hash VARCHAR(255) NULL;

-- SocialConnection 테이블에 해시 컬럼 추가
ALTER TABLE social_connection
ADD COLUMN social_email_hash VARCHAR(255) NOT NULL DEFAULT '',
ADD COLUMN social_id_hash VARCHAR(255) NOT NULL DEFAULT '';

-- 검색 성능을 위한 인덱스 생성
CREATE INDEX idx_member_email_hash ON member(email_hash);
CREATE INDEX idx_member_name_hash ON member(name_hash);
CREATE INDEX idx_member_birthday_hash ON member(birthday_hash);
CREATE INDEX idx_social_connection_email_hash ON social_connection(social_email_hash);
CREATE INDEX idx_social_connection_id_hash ON social_connection(social_id_hash);

-- 기존 데이터의 해시 값 업데이트는 애플리케이션에서 처리
-- (평문 데이터가 없으므로 SQL로는 불가능)