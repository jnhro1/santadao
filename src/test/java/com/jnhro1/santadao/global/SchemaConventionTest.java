package com.jnhro1.santadao.global;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Flyway 마이그레이션으로 만들어진 스키마가 프로젝트 규칙을 지키는지 검사한다. (.claude/rules/jpa.md)
 */
@SpringBootTest
class SchemaConventionTest {

    private static final String FLYWAY_HISTORY_TABLE = "flyway_schema_history";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("FK 제약조건이 없다")
    void noForeignKeyConstraints() {
        List<String> foreignKeys = jdbcTemplate.queryForList("""
                SELECT CONCAT(TABLE_NAME, '.', CONSTRAINT_NAME)
                FROM information_schema.REFERENTIAL_CONSTRAINTS
                WHERE CONSTRAINT_SCHEMA = DATABASE()
                """, String.class);

        assertThat(foreignKeys)
                .as("FK 제약조건을 만들지 마세요. 마이그레이션 SQL과 @JoinColumn(foreignKey = @ForeignKey(NO_CONSTRAINT))을 확인하세요")
                .isEmpty();
    }

    @Test
    @DisplayName("모든 테이블에 한글 COMMENT가 있다")
    void everyTableHasComment() {
        List<String> tablesWithoutComment = jdbcTemplate.queryForList("""
                SELECT TABLE_NAME
                FROM information_schema.TABLES
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME <> ?
                  AND TABLE_COMMENT = ''
                """, String.class, FLYWAY_HISTORY_TABLE);

        assertThat(tablesWithoutComment)
                .as("마이그레이션 SQL의 CREATE TABLE에 COMMENT = '...'를 추가하세요")
                .isEmpty();
    }

    @Test
    @DisplayName("모든 컬럼에 한글 COMMENT가 있다")
    void everyColumnHasComment() {
        List<String> columnsWithoutComment = jdbcTemplate.queryForList("""
                SELECT CONCAT(TABLE_NAME, '.', COLUMN_NAME)
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME <> ?
                  AND COLUMN_COMMENT = ''
                """, String.class, FLYWAY_HISTORY_TABLE);

        assertThat(columnsWithoutComment)
                .as("마이그레이션 SQL의 컬럼 정의에 COMMENT '...'를 추가하세요")
                .isEmpty();
    }
}
