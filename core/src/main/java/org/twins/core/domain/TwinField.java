package org.twins.core.domain;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.featurer.fieldtyper.value.FieldValue;

import java.util.UUID;

/**
 * Do not mix up with TwinFieldEntity
 */

@Data
@RequiredArgsConstructor
@Accessors(chain = true)
public class TwinField {
    private final TwinEntity twin;
    private final TwinClassFieldEntity twinClassField;
    private FieldValue value;

    public UUID twinId() {
        return twin.getId();
    }

    public UUID twinClassFieldId() {
        return twinClassField.getId();
    }
}
