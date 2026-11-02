package com.jnhro1.santadao.event.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashSet;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ManitoDrawerTest {

    private static final int REPEAT = 1_000;

    private final ManitoDrawer manitoDrawer = new ManitoDrawer();

    @ParameterizedTest(name = "{0}명")
    @ValueSource(ints = {3, 4, 5, 10, 20})
    @DisplayName("누구도 자기 자신을 뽑지 않고, 모두 한 명에게 주고 한 명에게 받는다")
    void draw_isDerangement(int size) {
        for (int i = 0; i < REPEAT; i++) {
            // when
            List<Integer> assignment = manitoDrawer.draw(size);

            // then
            assertThat(assignment).hasSize(size);
            assertThat(new HashSet<>(assignment)).hasSize(size);
            assertThat(IntStream.range(0, size)).noneMatch(giver -> assignment.get(giver) == giver);
        }
    }

    @Test
    @DisplayName("상호 배정(A↔B)도 우연히 나올 수 있다")
    void draw_allowsMutualPairs() {
        boolean hasMutualPair = IntStream.range(0, REPEAT)
                .mapToObj(i -> manitoDrawer.draw(4))
                .anyMatch(this::containsMutualPair);

        assertThat(hasMutualPair).isTrue();
    }

    @Test
    @DisplayName("결과는 매번 달라질 수 있다")
    void draw_isRandom() {
        long distinctResults = IntStream.range(0, 100)
                .mapToObj(i -> manitoDrawer.draw(5))
                .distinct()
                .count();

        assertThat(distinctResults).isGreaterThan(1);
    }

    @Test
    @DisplayName("참가자가 2명 미만이면 예외가 발생한다")
    void draw_tooSmall() {
        assertThatThrownBy(() -> manitoDrawer.draw(1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private boolean containsMutualPair(List<Integer> assignment) {
        return IntStream.range(0, assignment.size())
                .anyMatch(giver -> assignment.get(assignment.get(giver)) == giver);
    }
}
