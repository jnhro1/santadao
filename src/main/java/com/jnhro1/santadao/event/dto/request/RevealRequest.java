package com.jnhro1.santadao.event.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** participantId가 있으면 우선 사용하고, 없으면 같은 이벤트 안에서 name으로 찾는다. */
public record RevealRequest(
        Long participantId,

        String name,

        @NotBlank(message = "비밀 코드를 입력해 주세요")
        @Pattern(regexp = "\\d{4}", message = "비밀 코드는 숫자 4자리예요")
        String pin
) {
}
