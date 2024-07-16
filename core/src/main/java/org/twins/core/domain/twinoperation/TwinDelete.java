package org.twins.core.domain.twinoperation;

import org.twins.core.dao.twin.TwinEntity;

public class TwinDelete extends TwinOperation {
    public TwinDelete(TwinEntity twin) {
        twinEntity = twin;
    }
}
