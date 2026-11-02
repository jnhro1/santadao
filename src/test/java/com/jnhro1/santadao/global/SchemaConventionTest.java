package com.jnhro1.santadao.global;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Flyway 마이그레이션으로 만들어진 santadao 스키마가 프로젝트 규칙을 지키는지 검사한다. (.claude/rules/jpa.md)
 */
@SpringBootTest
class SchemaConventionTest {

    private static final String SCHEMA = "santadao";
    private static final String FLYWAY_HISTORY_TABLE = "flyway_schema_history";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("FK 제약조건이 없다")
    void noForeignKeyConstraints() {
        List<String> foreignKeys = jdbcTemplate.queryForList("""
                SELECT c.conrelid::regclass::text || '.' || c.conname
                FROM pg_constraint c
                JOIN pg_namespace n ON n.oid = c.connamespace
                WHERE n.nspname = ?
                  AND c.contype = 'f'
                """, String.class, SCHEMA);

        assertThat(foreignKeys)
                .as("FK 제약조건을 만들지 마세요. 마이그레이션 SQL과 @JoinColumn(foreignKey = @ForeignKey(NO_CONSTRAINT))을 확인하세요")
                .isEmpty();
    }

    @Test
    @DisplayName("모든 테이블에 한글 COMMENT가 있다")
    void everyTableHasComment() {
        List<String> tablesWithoutComment = jdbcTemplate.queryForList("""
                SELECT c.relname
                FROM pg_class c
                JOIN pg_namespace n ON n.oid = c.relnamespace
                WHERE n.nspname = ?
                  AND c.relkind = 'r'
                  AND c.relname <> ?
                  AND obj_description(c.oid, 'pg_class') IS NULL
                """, String.class, SCHEMA, FLYWAY_HISTORY_TABLE);

        assertThat(tablesWithoutComment)
                .as("마이그레이션 SQL에 COMMENT ON TABLE ... IS '...'를 추가하세요")
                .isEmpty();
    }

    @Test
    @DisplayName("모든 컬럼에 한글 COMMENT가 있다")
    void everyColumnHasComment() {
        List<String> columnsWithoutComment = jdbcTemplate.queryForList("""
                SELECT c.relname || '.' || a.attname
                FROM pg_attribute a
                JOIN pg_class c ON c.oid = a.attrelid
                JOIN pg_namespace n ON n.oid = c.relnamespace
                WHERE n.nspname = ?
                  AND c.relkind = 'r'
                  AND c.relname <> ?
                  AND a.attnum > 0
                  AND NOT a.attisdropped
                  AND col_description(a.attrelid, a.attnum) IS NULL
                """, String.class, SCHEMA, FLYWAY_HISTORY_TABLE);

        assertThat(columnsWithoutComment)
                .as("마이그레이션 SQL에 COMMENT ON COLUMN ... IS '...'를 추가하세요")
                .isEmpty();
    }

    @Test
    @DisplayName("모든 테이블에 RLS가 켜져 있다 (Supabase 공개 API 접근 차단)")
    void everyTableHasRowLevelSecurity() {
        List<String> tablesWithoutRls = jdbcTemplate.queryForList("""
                SELECT c.relname
                FROM pg_class c
                JOIN pg_namespace n ON n.oid = c.relnamespace
                WHERE n.nspname = ?
                  AND c.relkind = 'r'
                  AND c.relname <> ?
                  AND NOT c.relrowsecurity
                """, String.class, SCHEMA, FLYWAY_HISTORY_TABLE);

        assertThat(tablesWithoutRls)
                .as("마이그레이션 SQL에 ALTER TABLE ... ENABLE ROW LEVEL SECURITY를 추가하세요")
                .isEmpty();
    }
}
