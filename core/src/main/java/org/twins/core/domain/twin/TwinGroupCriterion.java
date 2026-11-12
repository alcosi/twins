package org.twins.core.domain.twin;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.enums.twin.TwinGroupCriterionType;
import org.twins.core.enums.twin.TwinGroupValueType;
import org.twins.core.enums.twin.TwinGroupSort;
import org.twins.core.enums.twin.TwinLinkNodeEnd;
import org.twins.core.featurer.fieldtyper.storage.TwinFieldStorage;

import java.util.UUID;

@Data
@Accessors(chain = true)
public class TwinGroupCriterion {
    private TwinGroupCriterionType type;
    private UUID fieldId;
    private UUID linkId;
    private TwinLinkNodeEnd nodeEnd;
    private TwinGroupSort sort;
    private TwinGroupKeyKind keyKind;
    private TwinGroupValueType valueType;
    private TwinEntity.BasicField basicField;
    private TwinClassFieldEntity dynamicField;
    private TwinFieldStorage storage;
    private boolean dataListOption;
}
