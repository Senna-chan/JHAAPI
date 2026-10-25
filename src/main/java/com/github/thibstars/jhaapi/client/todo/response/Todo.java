package com.github.thibstars.jhaapi.client.todo.response;

import java.time.OffsetDateTime;

public record Todo(
        String entity_id,
        String name
) {
}
