-- V5: birthday 컬럼 길이 수정
-- 암호화된 데이터를 저장하기 위해 충분한 길이로 확장

ALTER TABLE member
MODIFY COLUMN birthday VARCHAR(255) NULL;