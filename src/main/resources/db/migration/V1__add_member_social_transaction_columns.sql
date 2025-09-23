-- V1: 스키마 변경 - 컬럼 추가 및 수정
-- 1. member 테이블: ai_learning_agreement, last_login_at 컬럼 추가
-- 2. social_connection 테이블: social_email, deleted_at 컬럼 추가
-- 3. withdrawal 테이블: member_id 컬럼 삭제
-- 4. transaction 테이블: creator nullable 변경, data_retention_consent 컬럼 추가

-- 1. member 테이블 변경
ALTER TABLE member ADD COLUMN ai_learning_agreement BOOLEAN DEFAULT TRUE COMMENT 'AI 학습 동의 여부';
ALTER TABLE member ADD COLUMN last_login_at DATETIME COMMENT '마지막 로그인 시간';

-- 기존 데이터에 대한 ai_learning_agreement 값 설정
UPDATE member SET ai_learning_agreement = TRUE WHERE ai_learning_agreement IS NULL;

-- 2. social_connection 테이블 변경
ALTER TABLE social_connection ADD COLUMN social_email VARCHAR(255) NOT NULL COMMENT '소셜 이메일';
ALTER TABLE social_connection ADD COLUMN deleted_at DATETIME;

-- 기존 데이터에 대한 social_email 값 설정 (member 테이블의 email 값으로)
UPDATE social_connection sc
INNER JOIN member m ON sc.member_id = m.id
SET sc.social_email = m.email
WHERE sc.social_email IS NULL OR sc.social_email = '';

-- 3. withdrawal 테이블에서 member_id 컬럼 삭제
-- 외래키 제약조건이 있다면 먼저 삭제
-- ALTER TABLE withdrawal DROP FOREIGN KEY FK_WITHDRAWAL_MEMBER_ID; -- 필요시 실행
ALTER TABLE withdrawal DROP COLUMN member_id;

-- 4. transaction 테이블 변경
-- creator 컬럼을 nullable로 변경 (이미 nullable=true로 설정되어 있음)
ALTER TABLE transaction ADD COLUMN data_retention_consent BOOLEAN COMMENT '데이터 보관 동의 여부';

-- 기존 데이터에 대한 data_retention_consent 값은 NULL로 유지 (이미 nullable)