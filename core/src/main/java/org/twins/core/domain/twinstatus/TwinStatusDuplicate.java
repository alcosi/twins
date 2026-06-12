package org.twins.core.domain.twinstatus;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.EntityDuplicate;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class TwinStatusDuplicate extends EntityDuplicate<TwinStatusEntity> {
    private UUID newTwinClassId;
    private UUID newTwinStatusId;
    private boolean duplicateTriggers = false;
    private TwinClassEntity newTwinClass;
}
