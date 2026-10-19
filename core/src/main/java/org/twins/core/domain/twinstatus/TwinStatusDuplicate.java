package org.twins.core.domain.twinstatus;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.EntityDuplicate;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class TwinStatusDuplicate extends EntityDuplicate<TwinStatusEntity, TwinClassEntity> {
    private boolean duplicateTriggers = false;
}
