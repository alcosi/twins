package org.twins.core.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.cambium.common.kit.KitGrouped;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldAttributeEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.UUID;

/**
 * Do not mix up with TwinFieldEntity
 */

@Getter
@RequiredArgsConstructor
@Accessors(chain = true)
public class TwinField {
    private final TwinEntity twin;
    private final TwinClassFieldEntity twinClassField;
    @Setter
    private Boolean editable;
    @Setter
    private KitGrouped<TwinFieldAttributeEntity, UUID, UUID> attributes;

    public UUID getTwinId() {
        return twin.getId();
    }

    public UUID getTwinClassFieldId() {
        return twinClassField.getId();
    }

    public KitGrouped<TwinFieldAttributeEntity, UUID, UUID> getAttributes() {
        if (attributes != null)
            return attributes;
        else if (twin != null && twin.getTwinFieldAttributeKit() != null) {
            attributes = new KitGrouped<>(twin.getTwinFieldAttributeKit().getGrouped(twinClassField.getId()), TwinFieldAttributeEntity::getId, TwinFieldAttributeEntity::getTwinClassFieldAttributeId);
        }
        return attributes;
    }
}
