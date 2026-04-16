package org.twins.core.domain.twinclass;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.UUID;

@Data
@Accessors(chain = true)
public class TwinClassFieldDuplicate {
    private UUID originalTwinClassFieldId;
    private UUID newTwinClassId;
    private UUID newTwinClassFieldId;
    private String newKey;
    private boolean duplicateRules = false;

    private TwinClassFieldEntity originalTwinClassField;
    private TwinClassEntity newTwinClass;
    private TwinClassFieldEntity newTwinClassField;
}
