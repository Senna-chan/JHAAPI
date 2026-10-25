package com.github.thibstars.jhaapi.client.todo.response;

import java.time.OffsetDateTime;

public record TodoItem(
        String summary,
        String uid,
        String status,
        String description,
        OffsetDateTime due
) {
}
