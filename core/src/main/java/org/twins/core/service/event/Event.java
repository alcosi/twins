package org.twins.core.service.event;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.HashMap;
import java.util.Map;

@Data
@Accessors(chain = true)
@RequiredArgsConstructor
public class Event {
    private final Events event;
    private Map<String, String> context = new HashMap<>();

    public Event addContext(String key, String value) {
        context.put(key, value);
        return this;
    }
}
