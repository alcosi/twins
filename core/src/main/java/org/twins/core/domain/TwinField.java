package org.twins.core.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.twins.core.dao.twin.TwinEntity;
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

    public UUID getTwinId() {
        return twin.getId();
    }

    public UUID getTwinClassFieldId() {
        return twinClassField.getId();
    }
}
