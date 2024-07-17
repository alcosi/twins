package org.twins.core.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class TwinEraserTransactionScope {
    Set<UUID> twinIds = new HashSet<>();

    public TwinEraserTransactionScope addTwinId(UUID id) {
        twinIds.add(id);
        return this;
    }
}
