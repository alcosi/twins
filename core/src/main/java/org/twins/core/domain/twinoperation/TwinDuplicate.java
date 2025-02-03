package org.twins.core.domain.twinoperation;

import lombok.Data;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.TwinChangesCollector;

@Data
public class TwinDuplicate {

    private TwinEntity duplicate;
    private TwinChangesCollector changesCollector;

}
