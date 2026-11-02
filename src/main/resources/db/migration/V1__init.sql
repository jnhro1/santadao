-- 마니또 이벤트 초기 스키마
-- 규칙: FK 제약조건은 만들지 않는다, 모든 테이블·컬럼에 한글 COMMENT를 단다 (.claude/rules/jpa.md)

CREATE TABLE events
(
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '이벤트 ID',
    name        VARCHAR(30)  NOT NULL COMMENT '이벤트 이름',
    memo        VARCHAR(200) NULL COMMENT '메모 (선물 금액 등)',
    -- 대소문자를 구분하는 랜덤 코드라 binary collation으로 비교한다 (기본 ai_ci는 대소문자 무시)
    public_code VARCHAR(8)   NOT NULL COLLATE utf8mb4_bin COMMENT '공용 링크 코드',
    admin_token VARCHAR(32)  NOT NULL COLLATE utf8mb4_bin COMMENT '주최자 관리 링크 토큰',
    created_at  DATETIME(6)  NOT NULL COMMENT '생성 일시',
    updated_at  DATETIME(6)  NOT NULL COMMENT '수정 일시',
    PRIMARY KEY (id),
    UNIQUE KEY uk_events_public_code (public_code),
    UNIQUE KEY uk_events_admin_token (admin_token)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '마니또 이벤트';

CREATE TABLE participants
(
    id         BIGINT      NOT NULL AUTO_INCREMENT COMMENT '참가자 ID',
    event_id   BIGINT      NOT NULL COMMENT '이벤트 ID (events.id)',
    name       VARCHAR(10) NOT NULL COMMENT '참가자 이름',
    pin        VARCHAR(4)  NOT NULL COMMENT '결과 확인용 PIN (숫자 4자리)',
    manito_id  BIGINT      NULL COMMENT '선물할 상대 참가자 ID (participants.id)',
    created_at DATETIME(6) NOT NULL COMMENT '생성 일시',
    updated_at DATETIME(6) NOT NULL COMMENT '수정 일시',
    PRIMARY KEY (id),
    -- event_id로 시작하므로 이벤트별 참가자 조회 인덱스를 겸한다
    UNIQUE KEY uk_participants_event_name (event_id, name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci
  COMMENT = '마니또 참가자';
