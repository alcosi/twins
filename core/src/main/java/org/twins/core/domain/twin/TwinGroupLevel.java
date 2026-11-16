package org.twins.core.domain.twin;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.enums.twin.TwinGroupValueType;
import org.twins.core.enums.twin.TwinGroupSort;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorage;

import java.util.UUID;

@Data
@Accessors(chain = true)
public class TwinGroupLevel {
    private UUID fieldId;
    private TwinGroupSort sort;
    private UUID resolvedLinkId;
    private boolean forwardLink;
    private TwinGroupValueType valueType;
    private TwinEntity.BasicField basicField;
    private TwinClassFieldEntity dynamicField;
    private TwinFieldStorage storage;
    private TwinGroupOptionSource optionSource;
}
