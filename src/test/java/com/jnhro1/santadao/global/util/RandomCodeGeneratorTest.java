package com.jnhro1.santadao.global.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RandomCodeGeneratorTest {

    private final RandomCodeGenerator randomCodeGenerator = new RandomCodeGenerator();

    @Test
    @DisplayName("지정한 길이의 영문 대소문자·숫자 코드를 만든다")
    void alphanumeric() {
        String code = randomCodeGenerator.alphanumeric(32);

        assertThat(code).hasSize(32).matches("[A-Za-z0-9]+");
    }

    @Test
    @DisplayName("PIN은 앞자리 0을 포함한 숫자 4자리다")
    void pin() {
        IntStream.range(0, 1_000).forEach(i ->
                assertThat(randomCodeGenerator.pin()).matches("\\d{4}"));
    }
}
