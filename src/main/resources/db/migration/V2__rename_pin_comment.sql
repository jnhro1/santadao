-- 사용자 용어 변경: PIN → 비밀 코드 (컬럼명 pin은 유지, 설명만 변경)
COMMENT ON COLUMN participants.pin IS '결과 확인용 비밀 코드 (숫자 4자리)';
