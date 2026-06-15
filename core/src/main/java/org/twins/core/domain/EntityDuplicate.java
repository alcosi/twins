package org.twins.core.domain;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
public class EntityDuplicate<E> {
    private UUID originalEntityId;
    private UUID duplicateParentEntityId; //optional - target parent when copying to a different parent
    private String newKey; //optional
    private E originalEntity;
    private E newEntity;
}
