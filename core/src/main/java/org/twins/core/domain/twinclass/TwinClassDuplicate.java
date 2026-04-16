package org.twins.core.domain.twinclass;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.twinclass.TwinClassEntity;

import java.util.UUID;

@Data
@Accessors(chain = true)
public class TwinClassDuplicate {
    private UUID originalTwinClassId;
    private UUID newTwinClassId;
    private String newKey;
    private boolean duplicateFields = false;
    private boolean duplicateStatuses = false;

    private TwinClassEntity originalTwinClass;
    private TwinClassEntity newTwinClass;
}
