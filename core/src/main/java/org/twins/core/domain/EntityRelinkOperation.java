package org.twins.core.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class EntityRelinkOperation {
    private UUID newId;
    private Strategy strategy = Strategy.delete;
    private Map<UUID, UUID> replaceMap;

    public enum Strategy {
        delete,
        restrict
    }
}
