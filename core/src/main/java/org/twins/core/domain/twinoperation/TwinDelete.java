package org.twins.core.domain.twinoperation;

import lombok.Getter;
import org.twins.core.dao.twin.TwinEntity;

@Getter
public class TwinDelete extends TwinOperation {
    boolean causeGlobalLock;

    public TwinDelete(TwinEntity twin, boolean causeGlobalLock) {
        this.twinEntity = twin;
        this.causeGlobalLock = causeGlobalLock;
    }
}
