package org.twins.core.domain;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.enums.EntityRelinkOperationStrategy;

import java.util.Map;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class EntityRelinkOperation {
    private UUID newId;
    private EntityRelinkOperationStrategy strategy = EntityRelinkOperationStrategy.delete;
    private Map<UUID, UUID> replaceMap;

}
