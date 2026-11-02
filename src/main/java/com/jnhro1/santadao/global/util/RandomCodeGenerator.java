package com.jnhro1.santadao.global.util;

import java.security.SecureRandom;
import java.util.random.RandomGenerator;
import org.springframework.stereotype.Component;

@Component
public class RandomCodeGenerator {

    private static final String ALPHANUMERIC =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int PIN_BOUND = 10_000;

    private final RandomGenerator random;

    public RandomCodeGenerator() {
        this(new SecureRandom());
    }

    RandomCodeGenerator(RandomGenerator random) {
        this.random = random;
    }

    public String alphanumeric(int length) {
        StringBuilder code = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            code.append(ALPHANUMERIC.charAt(random.nextInt(ALPHANUMERIC.length())));
        }
        return code.toString();
    }

    /** "0000" ~ "9999" (앞자리 0 유지를 위해 문자열로 반환) */
    public String pin() {
        return String.format("%04d", random.nextInt(PIN_BOUND));
    }
}
