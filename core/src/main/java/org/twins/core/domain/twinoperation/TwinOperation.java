package org.twins.core.domain.twinoperation;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.twin.TwinEntity;

@Data
@Accessors(chain = true)
public abstract class TwinOperation {
    protected TwinEntity twinEntity; // only for new/updated data
}
