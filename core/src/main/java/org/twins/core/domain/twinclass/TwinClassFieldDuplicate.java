package org.twins.core.domain.twinclass;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.EntityDuplicate;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class TwinClassFieldDuplicate extends EntityDuplicate<TwinClassFieldEntity> {
    private boolean duplicateRules = false;
}
