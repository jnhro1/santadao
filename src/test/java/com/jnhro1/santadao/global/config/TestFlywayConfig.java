package com.jnhro1.santadao.global.config;

import org.springframework.boot.flyway.autoconfigure.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * 테스트 DB는 매 실행마다 비운 뒤 V1부터 다시 마이그레이션한다.
 * 이전 실행의 데이터가 남지 않고, 마이그레이션 SQL이 빈 DB에서 처음부터 적용되는지도 함께 검증된다.
 */
@Profile("test")
@Configuration
public class TestFlywayConfig {

    @Bean
    public FlywayMigrationStrategy cleanMigrateStrategy() {
        return flyway -> {
            flyway.clean();
            flyway.migrate();
        };
    }
}
