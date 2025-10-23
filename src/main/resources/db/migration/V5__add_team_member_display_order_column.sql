-- V5: team_member 테이블에 display_order 컬럼 추가
-- 그룹 목록 정렬 순서를 사용자가 지정할 수 있도록 display_order 컬럼 추가

ALTER TABLE team_member ADD COLUMN display_order INT NOT NULL COMMENT '그룹 표시 순서';

-- 기존 데이터에 대한 display_order 값 설정 (0으로 초기화)
UPDATE team_member SET display_order = 0 WHERE display_order IS NULL;
