package com.jnhro1.santadao.event.service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.random.RandomGenerator;
import java.util.stream.IntStream;
import org.springframework.stereotype.Component;

/**
 * 마니또 추첨: 자기 자신을 뽑지 않는 랜덤 순열(derangement)을 만든다.
 * Fisher–Yates 셔플 후 고정점이 있으면 다시 섞는 rejection sampling으로 모든 완전순열이 같은 확률로 나온다.
 * (Sattolo 알고리즘은 하나의 원형 고리만 만들어 상호 배정이 불가능하므로 사용하지 않는다.)
 */
@Component
public class ManitoDrawer {

    private final RandomGenerator random;

    public ManitoDrawer() {
        this(new SecureRandom());
    }

    ManitoDrawer(RandomGenerator random) {
        this.random = random;
    }

    /**
     * @param size 참가자 수 (2 이상)
     * @return result[i] = i번째 참가자가 선물할 참가자의 인덱스
     */
    public List<Integer> draw(int size) {
        if (size < 2) {
            throw new IllegalArgumentException("추첨에는 2명 이상이 필요합니다.");
        }
        List<Integer> assignment = new ArrayList<>(IntStream.range(0, size).boxed().toList());
        do {
            shuffle(assignment);
        } while (hasFixedPoint(assignment));
        return List.copyOf(assignment);
    }

    private void shuffle(List<Integer> list) {
        for (int i = list.size() - 1; i > 0; i--) {
            Collections.swap(list, i, random.nextInt(i + 1));
        }
    }

    private boolean hasFixedPoint(List<Integer> assignment) {
        return IntStream.range(0, assignment.size()).anyMatch(i -> assignment.get(i) == i);
    }
}
