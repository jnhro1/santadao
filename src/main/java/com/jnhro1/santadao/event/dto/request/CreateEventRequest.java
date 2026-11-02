package com.jnhro1.santadao.event.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateEventRequest(
        @NotBlank(message = "이벤트 이름을 입력해 주세요")
        @Size(max = 30, message = "이벤트 이름은 30자까지 쓸 수 있어요")
        String name,

        @Size(max = 200, message = "메모는 200자까지 쓸 수 있어요")
        String memo,

        @NotNull(message = "참가자를 입력해 주세요")
        @Size(min = 3, max = 20, message = "참가자는 3명 이상 20명 이하로 입력해 주세요")
        List<@NotBlank(message = "참가자 이름을 입력해 주세요") String> participants
) {
}
