package com.github.thibstars.jhaapi.client.todo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.thibstars.jhaapi.Configuration;
import com.github.thibstars.jhaapi.client.calendars.CalendarService;
import com.github.thibstars.jhaapi.client.calendars.CalendarServiceImpl;
import com.github.thibstars.jhaapi.client.calendars.response.Calendar;
import com.github.thibstars.jhaapi.client.calendars.response.CalendarEvent;
import com.github.thibstars.jhaapi.client.services.ServiceService;
import com.github.thibstars.jhaapi.client.services.ServiceServiceImpl;
import com.github.thibstars.jhaapi.client.states.StatesServiceImpl;
import com.github.thibstars.jhaapi.client.todo.response.Todo;
import com.github.thibstars.jhaapi.client.todo.response.TodoItem;
import com.github.thibstars.jhaapi.internal.BaseService;
import org.apache.hc.core5.net.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.sql.Array;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TodoServiceImpl extends BaseService<Todo> implements TodoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(com.github.thibstars.jhaapi.client.todo.TodoServiceImpl.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static ServiceServiceImpl serviceService;
    private static StatesServiceImpl statesService;
    private Configuration configuration;
    public TodoServiceImpl(Configuration configuration) {
        super(configuration, "services",null);
        MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        MAPPER.registerModule(new JavaTimeModule());
        this.configuration = configuration;
        serviceService = new ServiceServiceImpl(configuration);
        statesService = new StatesServiceImpl(configuration);
    }

    @Override
    public List<Todo> getTodos() {
        LOGGER.info("Getting todos");
        var states = statesService.getStates();
        var todoService = states.stream().filter(s -> s.entityId().startsWith("todo")).toList();
        List<Todo> todos = new ArrayList<>();
        todoService.forEach((ts) -> {
            todos.add(new Todo(ts.entityId(), ts.attributes().friendlyName()));
        });
        return todos;
    }

    @Override
    public List<TodoItem> getTodoItems(String entityId, OffsetDateTime start, OffsetDateTime end) {
        LOGGER.info("Getting todo items for {}", entityId);

        String data = "{\"entity_id\": \"%s\"}".formatted(entityId);
        var response = post("todo/get_items?return_response", data);

        if(response.isEmpty()) {
            return new ArrayList<>();
        }
        String jsonResponse = response.get();
        List<TodoItem> todos = new ArrayList<>();
        try {
            JsonNode items = MAPPER.readTree(jsonResponse)
                    .path("service_response")
                    .path(entityId)
                    .path("items");
            todos = MAPPER.readerForListOf(TodoItem.class).readValue(items);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if(start != null){
            todos = todos.stream().filter(t -> {
                if(t.due() == null) return false;
                return t.due().isAfter(start);
            }).toList();
        }
        if(end != null){
            todos = todos.stream().filter(t -> {
                if(t.due() == null) return false;
                return t.due().isBefore(end);
            }).toList();
        }
        return todos;
    }
}
