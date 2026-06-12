package org.twins.core.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
public class EntityDuplicate<E> {
    private UUID originalEntityId;
    private String newKey;
    private E originalEntity;
    private E newEntity;
}
