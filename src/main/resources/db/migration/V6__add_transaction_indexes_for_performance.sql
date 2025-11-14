-- V6: 거래 내역 조회 성능 개선을 위한 인덱스 추가
-- 작성일: 2025-11-13
-- 목적: 거래 내역 조회 API의 쿼리 성능 최적화

-- 1. 개인 거래 조회 최적화 (creator_id + transaction_date)
CREATE INDEX idx_transaction_creator_date ON transaction (creator_id, transaction_date DESC);

-- 2. 팀 거래 조회 최적화 (team_id + transaction_date)
CREATE INDEX idx_transaction_team_date ON transaction (team_id, transaction_date DESC);
