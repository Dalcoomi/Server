-- V2: social_connection 테이블에 social_refresh_token 컬럼 추가

-- social_connection 테이블에 social_refresh_token 컬럼 추가
ALTER TABLE social_connection ADD COLUMN social_refresh_token VARCHAR(500);

-- withdrawal 테이블에서 created, updated 컬럼 삭제
ALTER TABLE withdrawal DROP COLUMN created_at;
ALTER TABLE withdrawal DROP COLUMN updated_at;