package org.twins.core.domain.twinstatus;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;

import java.util.UUID;

@Data
@Accessors(chain = true)
public class TwinStatusDuplicate {
    private UUID originalTwinStatusId;
    private UUID newTwinClassId;
    private UUID newTwinStatusId;
    private String newKey;
    private boolean duplicateTriggers = false;

    private TwinStatusEntity originalTwinStatus;
    private TwinClassEntity newTwinClass;
    private TwinStatusEntity newTwinStatus;
}
